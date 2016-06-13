package com.pszymczyk.embedded.consul

import com.pszymczyk.embedded.consul.infrstructure.AntUnzip
import com.pszymczyk.embedded.consul.infrstructure.HttpBinaryRepository

import java.nio.file.Path

class ConsulStarter {

    private final Path dataDir
    private final Path downloadDir
    private final int httpPort

    private boolean started = false

    private HttpBinaryRepository binaryRepository
    private AntUnzip unzip

    ConsulStarter(Path dataDir, Path downloadDir, int httpPort) {
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
        if (started) {
            throw new ConsulAlreadyStartedException()
        }
        started = true
        String downloadDirAsString = downloadDir.toAbsolutePath().toString()
        File file = new File(downloadDir.toAbsolutePath().toString(), 'consul.zip')

        File archive = binaryRepository.getConsulBinaryArchive(file)
        unzip.unzip(archive, downloadDir.toFile())

        return new ConsulProcess(dataDir: dataDir, httpPort: httpPort,
                process: new ProcessBuilder()
                        .directory(downloadDir.toFile())
                        .command("$downloadDirAsString/consul agent -data-dir=$dataDir -server -bootstrap -node=consul-test-node -advertise=127.0.0.1 -http-port $httpPort".split(' '))
                        .inheritIO()
                        .start())
    }
}