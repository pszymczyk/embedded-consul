package com.pszymczyk.embedded.consul

import com.pszymczyk.embedded.consul.infrstructure.AntUnzip
import com.pszymczyk.embedded.consul.infrstructure.HttpBinaryRepository
import com.pszymczyk.embedded.consul.infrstructure.Ports
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

    ConsulStarter(Path dataDir, Path downloadDir, File  portsConfigFile, LogLevel logLevel, int httpPort) {
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

    public ConsulProcess start() {
        logger.info("Starting new Consul process.")

        if (started) {
            logger.error("Consul process already started!")
            throw new ConsulAlreadyStartedException()
        }

        started = true

        if (!isBinaryDownloaded()) {
            downloadAndUnpackBinary()
        }

        String portsConfig = createConfigFile().absolutePath
        String downloadDirAsString = downloadDir.toAbsolutePath().toString()

        String[] command = ["$downloadDirAsString/consul",
                            "agent",
                            "-data-dir=$dataDir",
                            "-dev",
                            "-config-file=$portsConfig",
                            "-advertise=127.0.0.1",
                            "-log-level=$logLevel.value",
                            "-http-port=$httpPort"]

        return new ConsulProcess(dataDir: dataDir, httpPort: httpPort,
                process: new ProcessBuilder()
                        .directory(downloadDir.toFile())
                        .command(command)
                        .inheritIO()
                        .start())
    }

    private void downloadAndUnpackBinary() {
        File file = new File(downloadDir.toAbsolutePath().toString(), 'consul.zip')
        logger.info("Downloading archives into: {}", file.toString())

        File archive = binaryRepository.getConsulBinaryArchive(file)

        logger.info("Unzipping binaries into: {}", downloadDir.toString())
        unzip.unzip(archive, downloadDir.toFile())
    }

    private File createConfigFile() {
        logger.info("Creating configuration file: {}", portsConfigFile.toString())
        int[] ports = Ports.nextAvailable(5)

        portsConfigFile << """
            {
                "ports": {
                    "dns": """ + ports[0] + """,
                    "rpc": """ + ports[1] + """,
                    "serf_lan": """ + ports[2] + """,
                    "serf_wan": """ + ports[3] + """,
                    "server": """ + ports[4] + """
                }
            }
        """
    }

    private boolean isBinaryDownloaded() {
        return new File(downloadDir.toString(), "consul").exists()
    }
}