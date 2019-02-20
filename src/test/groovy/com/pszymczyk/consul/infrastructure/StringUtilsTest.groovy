package com.pszymczyk.consul.infrastructure

import spock.lang.Specification

class StringUtilsTest extends Specification {

    def "should throw exception when null or empty string is given"() {
        given:
        def customMessage = "some custom message"

        when:
        StringUtils.requireNotBlank(givenString, customMessage)

        then:
        def ex = thrown IllegalArgumentException
        ex.message == customMessage

        where:
        givenString << [null, "", "   "]
    }
}
