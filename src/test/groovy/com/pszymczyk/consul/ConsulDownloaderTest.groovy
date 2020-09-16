package com.pszymczyk.consul

import spock.lang.Specification

class ConsulDownloaderTest extends Specification {

    def osName = System.getProperty("os.name")

    ConsulStarter starter

    def setup() {
        starter = ConsulStarterBuilder.consulStarter().build()
    }

    def cleanup() {
        System.setProperty("os.name", osName)
    }


    def "should provide proper consul binary for windows"() {
        given:
            System.setProperty("os.name", "windows")

        when:
            File binaryPath = this.starter.getConsulBinary()

        then:
            binaryPath.getName() == "consul.exe"
    }

    def "should provide proper consul binary for linux"() {
        given:
            System.setProperty("os.name", "linux")

        when:
            File binaryPath = starter.getConsulBinary()

        then:
            binaryPath.getName() == "consul"
    }
}
