package com.pszymczyk.consul.infrastructure

class HttpBinaryRepository {

    File getConsulBinaryArchive(String version, File file) {
        String os = OsResolver.resolve()
        String url = "https://releases.hashicorp.com/consul/${version}/consul_${version}_${os}_amd64.zip"
        OutputStream outputStream = file.newOutputStream()
        outputStream << new URL(url).openStream()
        outputStream.close()
        file
    }
}
