package com.pszymczyk.consul.infrastructure

class HttpBinaryRepository {


    public static final String CONSUL_BINARY_CDN = "CONSUL_BINARY_CDN"
    public static final String CONSUL_DEFAULT_CDN = "https://releases.hashicorp.com/consul/"

    File getConsulBinaryArchive(String version, File file) {
        String os = OsResolver.resolve()
        String cdn = System.getenv(CONSUL_BINARY_CDN) != null ? System.getenv(CONSUL_BINARY_CDN) : CONSUL_DEFAULT_CDN;
        String url = "${cdn}${version}/consul_${version}_${os}_amd64.zip"
        OutputStream outputStream = file.newOutputStream()
        outputStream << new URL(url).openStream()
        outputStream.close()
        file
    }
}
