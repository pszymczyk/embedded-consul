package com.pszymczyk.consul


import java.nio.file.Path

class ConsulProcessCommandFactory {
    private static final Random random = new Random()

    private final Path dataDir
    private final Path configDir
    private final String advertise
    private final String client
    private final LogLevel logLevel
    private final String bind
    private final String startJoin
    private final CustomConfig customConfig

    ConsulProcessCommandFactory(Path dataDir, Path configDir, String advertise, String client, LogLevel logLevel, String bind, String startJoin, CustomConfig customConfig) {
        this.dataDir = dataDir
        this.configDir = configDir
        this.advertise = advertise
        this.client = client
        this.logLevel = logLevel
        this.bind = bind
        this.startJoin = startJoin
        this.customConfig = customConfig
    }

    String[] createConsulProcessCommand(String pathToConsul, ports) {
        String[] command = [pathToConsul,
                            "agent",
                            "-data-dir=$dataDir",
                            "-dev",
                            "-config-dir=$configDir",
                            "-advertise=$advertise",
                            "-client=$client",
                            "-log-level=$logLevel.value",
                            "-http-port=${ports.httpPort}",
                            "-grpc-port=${ports.grpcPort}"]

        if (bind != null) {
            command += "-bind=$bind"
        }

        if (startJoin != null) {
            command += "-join=$startJoin"
        }

        if (customConfig.get("node_id") == null) {
            command += ["-node-id=" + randomNodeId()]
        }

        if (customConfig.get("node_name") == null) {
            command += ["-node=" + randomNodeName()]
        }
        command
    }

    private static String randomNodeId() {
        return randomHex(8) + "-" + randomHex(4) + "-" + randomHex(4) + "-" + randomHex(4) + "-" + randomHex(12);
    }

    private static String randomNodeName() {
        return "node-" + randomHex(10)
    }

    private static String randomHex(int len) {
        StringBuilder sb = new StringBuilder()
        for (int i = 0; i < len; i++) {
            sb.append(Long.toHexString(random.nextInt(16)));
        }
        return sb.toString();
    }
}
