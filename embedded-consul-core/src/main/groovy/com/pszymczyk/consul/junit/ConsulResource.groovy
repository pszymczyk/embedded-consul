package com.pszymczyk.consul.junit

import com.pszymczyk.consul.ConsulProcess
import com.pszymczyk.consul.ConsulStarter
import com.pszymczyk.consul.ConsulStarterBuilder
import org.junit.rules.ExternalResource

import java.nio.file.Path

class ConsulResource extends ExternalResource {

    private final ConsulStarter consul

    private ConsulProcess process

    ConsulResource(ConsulStarter consul) {
        this.consul = consul
    }

    ConsulResource() {
        this(ConsulStarterBuilder.consulStarter().build())
    }

    ConsulResource(int httpPort) {
        this(ConsulStarterBuilder.consulStarter().withHttpPort(httpPort).build())
    }

    @Override
    protected void before() throws Throwable {
        process = consul.start()
    }

    @Override
    protected void after() {
        if (process != null) {
            process.close()
        }
    }

    void reset() {
        process.reset()
    }

    int getHttpPort() {
        process.httpPort
    }

    int getDnsPort() {
        process.dnsPort
    }

    int getRpcPort() {
        process.rpcPort
    }

    int getSerfLanPort() {
        process.serfLanPort
    }

    int getSerfWanPort() {
        process.serfWanPort
    }

    int getServerPort() {
        process.serverPort
    }

    Path getDataDir() {
        return process.dataDir
    }
}
