package com.pszymczyk.consul

import com.pszymczyk.consul.infrastructure.AntUnzip
import com.pszymczyk.consul.infrastructure.ConsulWaiter
import com.pszymczyk.consul.infrastructure.HttpBinaryRepository
import com.pszymczyk.consul.infrastructure.OsResolver
import com.pszymczyk.consul.infrastructure.Ports
import org.codehaus.groovy.runtime.IOGroovyMethods
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path

class ConsulStarter {

    private static final Logger logger = LoggerFactory.getLogger(ConsulStarter.class);

    private final Path dataDir
    private final Path downloadDir
    private final File portsConfigFile
    private final LogLevel logLevel
    private final int httpPort

    private boolean started = false

    private HttpBinaryRepository binaryRepository
    private AntUnzip unzip

    ConsulStarter(Path dataDir, Path downloadDir, File portsConfigFile, LogLevel logLevel, int httpPort) {
        this.logLevel = logLevel
        this.portsConfigFile = portsConfigFile
        this.dataDir = dataDir
        this.downloadDir = downloadDir
        this.httpPort = httpPort
        makeDI()
    }

    private void makeDI() {
        binaryRepository = new HttpBinaryRepository()
        unzip = new AntUnzip()
    }

    ConsulProcess start() {
        logger.info("Starting new Consul process.")
        checkInitialState()

        started = true

        if (!isBinaryDownloaded()) {
            downloadAndUnpackBinary()
        }

        ConsulPorts consulPorts = new ConsulPorts(httpPort)
        String portsConfig = createConfigFile(consulPorts).absolutePath
        String downloadDirAsString = downloadDir.toAbsolutePath().toString()

        String pathToConsul = "$downloadDirAsString/consul"
        if ('windows' == OsResolver.resolve()) {
            pathToConsul = "$downloadDirAsString/consul.exe"
        }

        String[] command = [pathToConsul,
                            "agent",
                            "-data-dir=$dataDir",
                            "-dev",
                            "-config-file=$portsConfig",
                            "-advertise=127.0.0.1",
                            "-log-level=$logLevel.value",
                            "-http-port=$httpPort"]

        ConsulProcess process = new ConsulProcess(dataDir, consulPorts,
                new ProcessBuilder()
                        .directory(downloadDir.toFile())
                        .command(command)
                        .inheritIO()
                        .start())

        new ConsulWaiter(httpPort).awaitUntilConsulStarted()

        return process
    }

    private void checkInitialState() {
        if (started) throw new EmbeddedConsulException('This Consul Starter instance already started Consul process. Create new ConsulStarter instance')
        try {
            IOGroovyMethods.withCloseable(new Socket("localhost", httpPort), { it-> throw new EmbeddedConsulException("Port $httpPort is not available, cannot start Consul process.")})
        } catch (IOException ex) {
            // socket is free - everything ok
        }
    }


    private void downloadAndUnpackBinary() {
        File file = new File(downloadDir.toAbsolutePath().toString(), 'consul.zip')
        logger.info("Downloading archives into: {}", file.toString())

        File archive = binaryRepository.getConsulBinaryArchive(file)

        logger.info("Unzipping binaries into: {}", downloadDir.toString())
        unzip.unzip(archive, downloadDir.toFile())
    }

    private File createConfigFile(ConsulPorts consulPorts) {
        logger.info("Creating configuration file: {}", portsConfigFile.toString())

        portsConfigFile << """
            {
                "ports": {
                    "dns": """ + consulPorts.dnsPort + """,
                    "rpc": """ + consulPorts.rpcPort + """,
                    "serf_lan": """ + consulPorts.serfLanPort + """,
                    "serf_wan": """ + consulPorts.serfWanPort + """,
                    "server": """ + consulPorts.serverPort + """
                }
            }
        """
    }

    private boolean isBinaryDownloaded() {
        return new File(downloadDir.toString(), "consul").exists()
    }
}