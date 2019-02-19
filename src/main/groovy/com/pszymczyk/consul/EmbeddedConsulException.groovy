package com.pszymczyk.consul


class EmbeddedConsulException extends RuntimeException {

    EmbeddedConsulException(Exception ex) {
        super("Unexpected exception occurred during Consul process startup." , ex)
    }

    EmbeddedConsulException(String message) {
        super(message)
    }
}
