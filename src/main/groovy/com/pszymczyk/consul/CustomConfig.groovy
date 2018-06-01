package com.pszymczyk.consul

import groovy.json.JsonOutput
import groovy.json.JsonParserType
import groovy.json.JsonSlurper
import groovy.transform.PackageScope

@PackageScope
class CustomConfig {

    private final Map config
    private final String rawValue

    static CustomConfig empty() {
        new CustomConfig("")
    }

    CustomConfig(String customConfig) {
        config = parseCustomConfig(customConfig)
        rawValue = JsonOutput.toJson(config)
    }

    String asString() {
        rawValue
    }

    Object get(String propertyName) {
        return config[propertyName]
    }

    boolean isEmpty() {
        return config.isEmpty()
    }

    private def parseCustomConfig(String customConfig) {
        if (customConfig == null || customConfig.isEmpty()) {
            return [:]
        }

        def parser = new JsonSlurper().setType(JsonParserType.LAX)
        return parser.parseText(customConfig)
    }
}
