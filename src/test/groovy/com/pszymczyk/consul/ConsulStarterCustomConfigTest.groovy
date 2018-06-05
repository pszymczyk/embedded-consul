package com.pszymczyk.consul

import com.ecwid.consul.v1.ConsulClient
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class ConsulStarterCustomConfigTest extends Specification {

    def "should apply new custom config when restart with same config dir"() {
        given:
        String beforeDc = "before"
        String afterDc = "after"

        String conf = """{"datacenter": "${beforeDc}"}"""
        String anotherConf = """{"datacenter": "${afterDc}"}"""

        String host = "localhost"

        Path tempDir = Files.createTempDirectory("for_test_extra_config");

        when:
        ConsulStarter starter = ConsulStarterBuilder.consulStarter()
            .withConfigDir(tempDir)
            .withCustomConfig(conf)
            .build()
        ConsulProcess consul = starter.start()
        String datacenter = new ConsulClient(host, consul.getHttpPort())
                                .getDatacenters().getValue().first().getDatacenter();
        consul.close()

        ConsulStarter anotherStarter = ConsulStarterBuilder.consulStarter()
            .withConfigDir(tempDir)
            .withCustomConfig(anotherConf)
            .build()
        ConsulProcess anotherConsul = anotherStarter.start()
        String anotherDatacenter = new ConsulClient(host, anotherConsul.getHttpPort())
                                        .getDatacenters().getValue().first().getDatacenter();
        anotherConsul.close()

        then:
        datacenter != anotherDatacenter
        anotherDatacenter == afterDc
    }
}
