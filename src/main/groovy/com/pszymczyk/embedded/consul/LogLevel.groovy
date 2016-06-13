package com.pszymczyk.embedded.consul;

enum LogLevel {

    TRACE("trace"),
    DEBUG("debug"),
    INFO("info"),
    WARN("warn"),
    ERR("err")

    final String value

    LogLevel(String value) {
        this.value = value
    }
}
