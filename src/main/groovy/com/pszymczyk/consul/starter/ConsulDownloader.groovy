package com.pszymczyk.consul.starter

import com.pszymczyk.consul.infrastructure.AntUnzip
import com.pszymczyk.consul.infrastructure.HttpBinaryRepository
import com.pszymczyk.consul.infrastructure.OsResolver
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path

class ConsulDownloader {

    private static final Logger logger = LoggerFactory.getLogger(ConsulDownloader.class)

    private AntUnzip unzip
    private HttpBinaryRepository binaryRepository
    private final String consulVersion
    private final Path downloadDir

    ConsulDownloader(Path downloadDir, String consulVersion) {
        unzip = new AntUnzip()
        binaryRepository = new HttpBinaryRepository()
        this.downloadDir = downloadDir
        this.consulVersion = consulVersion
    }

    void downloadConsul() {
        if (!isBinaryDownloaded()) {
            downloadAndUnpackBinary()
        }
    }

    private void downloadAndUnpackBinary() {
        File file = new File(downloadDir.toAbsolutePath().toString(), 'consul.zip')
        logger.info("Downloading archives into: {}", file.toString())

        File archive = binaryRepository.getConsulBinaryArchive(consulVersion, file)

        logger.info("Unzipping binaries into: {}", downloadDir.toString())
        unzip.unzip(archive, downloadDir.toFile())
    }


    private boolean isBinaryDownloaded() {
        return getConsulBinary().exists()
    }

    File getConsulBinary() {
        String consulBinaryName = OsResolver.resolve().equals("windows") ? "consul.exe" : "consul"
        return new File(downloadDir.toString(), consulBinaryName)
    }
}
