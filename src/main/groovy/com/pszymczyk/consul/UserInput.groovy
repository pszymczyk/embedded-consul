package com.pszymczyk.consul

import groovy.transform.PackageScope
import org.slf4j.Logger

import java.nio.file.Path

@PackageScope
class UserInput {

    final Path dataDir
    final Path downloadDir
    final Path configDir
    final String consulVersion
    final CustomConfig customConfig
    final LogLevel logLevel
    final Logger customLogger
    final ConsulPorts consulPorts
    final String startJoin
    final String advertise
    final String client
    final String bind
    final String token
    final Integer waitTimeout
    final Set<String> services

    UserInput(Path dataDir, Path downloadDir, Path configDir, String consulVersion, CustomConfig customConfig, LogLevel logLevel, Logger customLogger, ConsulPorts.ConsulPortsBuilder consulPorts, String startJoin, String advertise, String client, String bind, String token, Integer waitTimeout, Set<String> services) {
        this.dataDir = dataDir
        this.downloadDir = downloadDir
        this.configDir = configDir
        this.consulVersion = consulVersion
        this.customConfig = customConfig
        this.logLevel = logLevel
        this.customLogger = customLogger
        this.consulPorts = mergePorts(consulPorts)
        this.startJoin = startJoin
        this.advertise = advertise
        this.client = client
        this.bind = bind
        this.token = token
        this.waitTimeout = waitTimeout
        this.services = services
    }

    private ConsulPorts mergePorts(ConsulPorts.ConsulPortsBuilder ports) {
        def extraPorts = customConfig.get("ports")

        extraPorts.collect { it ->
            switch (it.key) {
                case "dns": ports = ports.withDnsPort(it.value); break
                case "http": ports = ports.withHttpPort(it.value); break
                case "serf_lan": ports = ports.withSerfLanPort(it.value); break
                case "serf_wan": ports = ports.withSerfWanPort(it.value); break
                case "server": ports = ports.withServerPort(it.value); break
                case "grpc": ports = ports.withGRpcPort(it.value); break
            }
        }
        ports.build()
    }
}
