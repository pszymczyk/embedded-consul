package com.pszymczyk.consul.junit

import com.pszymczyk.consul.ConsulProcess
import com.pszymczyk.consul.ConsulStarter
import com.pszymczyk.consul.ConsulStarterBuilder
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolutionException
import org.junit.jupiter.api.extension.ParameterResolver

import java.nio.file.Path

class ConsulExtension implements ParameterResolver, AfterAllCallback, AfterEachCallback {

    private final ConsulProcess process;

    ConsulExtension(ConsulStarter consulStarter) {
        this.process = consulStarter.start();
    }

    ConsulExtension() {
        this(ConsulStarterBuilder.consulStarter().build())
    }

    @Override
    boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(ConsulProcess.class)
    }

    @Override
    Object resolveParameter(ParameterContext parameterContext,
                                   ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return this.process
    }

    @Override
    void afterAll(ExtensionContext context) throws Exception {
        this.process.close()
    }

    @Override
    void afterEach(ExtensionContext context) throws Exception {
        this.process.reset()
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
