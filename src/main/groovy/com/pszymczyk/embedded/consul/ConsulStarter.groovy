package com.pszymczyk.embedded.consul

import com.pszymczyk.embedded.consul.infrstructure.AntUnzip
import com.pszymczyk.embedded.consul.infrstructure.HttpBinaryRepository
import com.pszymczyk.embedded.consul.infrstructure.Ports

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ConsulStarter {

    private static final Path tmpPath = Files.createTempDirectory('embedded-consul')

    private HttpBinaryRepository binaryRepository
    private AntUnzip unzip

    ConsulStarter() {
        binaryRepository = new HttpBinaryRepository()
        unzip = new AntUnzip()
    }

    public ConsulProcess start() {
        int port = Ports.nextAvailable()
        Path dataDir = Paths.get(tmpPath.toAbsolutePath().toString(), "/data-dir")
        return start(dataDir, port)
    }

    public ConsulProcess start(Path dataDir, int httpPort) {
        String tmpPathAsString = tmpPath.toAbsolutePath().toString()
        File file = new File(tmpPath.toAbsolutePath().toString(), 'consul.zip')

        File archive = binaryRepository.getConsulBinaryArchive(file)
        unzip.unzip(archive, tmpPath.toFile())

        return new ConsulProcess(dataDir: dataDir, httpPort: httpPort,
                process: new ProcessBuilder()
                        .directory(tmpPath.toFile())
                        .command("$tmpPathAsString/consul agent -data-dir=$dataDir -server -bootstrap -node=consul-test-node -advertise=127.0.0.1 -http-port $httpPort".split(' '))
                        .inheritIO()
                        .start())
    }
}