package com.pszymczyk.consul

import groovy.transform.PackageScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path

@PackageScope
class ConfigFilesFactory {

    private static final Logger logger = LoggerFactory.getLogger(ConfigFilesFactory.class)

    private final ConsulPorts consulPorts
    private final CustomConfig customConfig
    private final Path configDir

    ConfigFilesFactory(ConsulPorts consulPorts, CustomConfig customConfig, Path configDir) {
        this.consulPorts = consulPorts
        this.customConfig = customConfig
        this.configDir = configDir
    }

    void createConfigFiles() {
        createBasicConfigFile(consulPorts)
        if (!customConfig.isEmpty()) {
            createExtraConfigFile()
        }
    }

    private void createBasicConfigFile(ConsulPorts consulPorts) {
        File portsConfigFile = new File(configDir.toFile(), "basic_config.json")
        logger.info("Creating ports configuration file: {}", portsConfigFile.toString())

        portsConfigFile.text = """
            {
                "ports": {
                    "dns": ${consulPorts.dnsPort},
                    "serf_lan": ${consulPorts.serfLanPort},
                    "serf_wan": ${consulPorts.serfWanPort},
                    "server": ${consulPorts.serverPort}
                },
                "disable_update_check": true,
                "performance": {
                    "raft_multiplier": 1
                }
            }
        """
    }

    private void createExtraConfigFile() {
        File customConfigFile = new File(configDir.toFile(), "extra_config.json")
        logger.info("Creating custom configuration file: {}", customConfigFile.toString())
        customConfigFile.text = customConfig.asString()
    }
}
