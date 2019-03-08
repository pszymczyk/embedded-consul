package com.pszymczyk.consul.starter

import com.pszymczyk.consul.ConsulStarter
import com.pszymczyk.consul.ConsulStarterBuilder
import spock.lang.Specification

class ConsulDownloaderTest extends Specification {
    private final ConsulStarter starter = ConsulStarterBuilder.consulStarter().build()


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
