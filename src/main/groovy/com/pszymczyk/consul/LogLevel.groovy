package com.pszymczyk.consul;

enum LogLevel {

    TRACE("trace"),
    DEBUG("debug"),
    INFO("info"),
    WARN("warn"),
    ERR("err"),
    ERROR("error")

    final String value

    LogLevel(String value) {
        this.value = value
    }
}
