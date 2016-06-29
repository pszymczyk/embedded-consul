package com.pszymczyk.consul.infrastructure

class HttpBinaryRepository {

    File getConsulBinaryArchive(File file) {
        String os = OsResolver.resolve()
        String url = "https://releases.hashicorp.com/consul/0.6.4/consul_0.6.4_${os}_amd64.zip"
        OutputStream outputStream = file.newOutputStream()
        outputStream << new URL(url).openStream()
        outputStream.close()
        file
    }
}
