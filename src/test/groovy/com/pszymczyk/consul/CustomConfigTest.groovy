package com.pszymczyk.consul

import groovy.json.JsonException
import spock.lang.Specification

class CustomConfigTest extends Specification {

    def "should parse given custom config"() {
        given:
        CustomConfig customConfig = new CustomConfig("""
            {
                "xyz": $givenValue
            }
        """)

        when:
        Object result = customConfig.get("xyz")

        then:
        result == expectedValue

        where:
        givenValue  || expectedValue
        "\"value\"" || "value"
        2           || 2
    }

    def "should return raw configuration"() {
        given:
        CustomConfig customConfig = new CustomConfig(givenJson)

        when:
        String result = customConfig.asString()

        then:
        result == givenJson.replaceAll(" ", "")

        where:
        givenJson << [
                """{ "xyz": "abc" }""",
                """{ "xyz": 2 }""",
                """{ "xyz": true }""",
        ]
    }

    def "should create empty custom config"() {
        expect:
        CustomConfig.empty().asString() == "{}"
        CustomConfig.empty().isEmpty()
    }

    def "should return empty json when null or empty string"() {

        when:
        CustomConfig customConfig = new CustomConfig(emptyValue)

        then:
        customConfig.asString() == "{}"

        where:
        emptyValue << [null, ""]
    }

    def "should throw exception when cannot parse json"() {

        when:
        new CustomConfig(incorrectJson)

        then:
        thrown JsonException

        where:
        incorrectJson << [
                """{ "xyz: 2}""",
                """{ xyz": 2}""",
        ]
    }
}
