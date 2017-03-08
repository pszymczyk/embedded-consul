package com.pszymczyk.consul

import spock.lang.Specification
import spock.lang.Unroll

class SpeedupTest extends Specification {

    @Unroll
    def "should time invocation for different raft_multiplier #raftMultiplier"() {
        given:
        def start = System.currentTimeMillis();

        when:
        (1..10).each {
            ConsulPorts consulPorts = ConsulPorts.consulPorts().build()

            def config = String.format(
                    """{
                        "ports": {
                        "dns": %s,
                        "rpc": %s,
                        "serf_lan": %s,
                        "serf_wan": %s,
                        "server": %s
                    },
                        "disable_update_check": true,
                        "performance": {
                        "raft_multiplier": ${raftMultiplier}
                    }
                    }""",
                    consulPorts.dnsPort, consulPorts.rpcPort, consulPorts. serfLanPort, consulPorts.serfWanPort, consulPorts.serverPort)

            ConsulStarterBuilder.consulStarter().withCustomConfig(config).build().start()
        }

        then:
        def total = System.currentTimeMillis() - start
        println "Raft multiplier: ${raftMultiplier} time: ${total / 1000}"

        where:
        raftMultiplier << [1, 5]
    }
}
