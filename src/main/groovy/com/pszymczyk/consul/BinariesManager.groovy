package com.pszymczyk.consul

import com.pszymczyk.consul.infrastructure.AntUnzip
import com.pszymczyk.consul.infrastructure.HttpBinaryRepository
import com.pszymczyk.consul.infrastructure.OsResolver
import groovy.transform.PackageScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path

@PackageScope
class BinariesManager {

    private static final Logger logger = LoggerFactory.getLogger(BinariesManager.class)

    private final Path downloadDir
    private final String consulVersion
    private final HttpBinaryRepository binaryRepository

    BinariesManager(Path downloadDir, String consulVersion) {
        this.consulVersion = consulVersion
        this.downloadDir = downloadDir
        this.binaryRepository = new HttpBinaryRepository()
    }

    String getBinaryPath() {
        String downloadDirAsString = downloadDir.toAbsolutePath().toString()
        String pathToConsul = "$downloadDirAsString/consul"
        if ('windows' == OsResolver.resolve()) {
            pathToConsul = "$downloadDirAsString/consul.exe"
        }

        return pathToConsul
    }

    void ensureConsulBinaries() {
        if (!isBinaryDownloaded()) {
            downloadAndUnpackBinary()
        }
    }

    private boolean isBinaryDownloaded() {
        return getConsulBinary().exists()
    }

    private File getConsulBinary() {
        String consulBinaryName = OsResolver.resolve().equals("windows") ? "consul.exe" : "consul"
        return new File(downloadDir.toString(), consulBinaryName)
    }

    private void downloadAndUnpackBinary() {
        File file = new File(downloadDir.toAbsolutePath().toString(), 'consul.zip')
        logger.info("Downloading archives into: {}", file.toString())

        File archive = binaryRepository.getConsulBinaryArchive(consulVersion, file)

        logger.info("Unzipping binaries into: {}", downloadDir.toString())
        AntUnzip.unzip(archive, downloadDir.toFile())
    }
}
