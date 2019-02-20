package com.pszymczyk.consul

import java.nio.file.Path

class InnerProcessFactory {

    private final Path downloadDir

    InnerProcessFactory(Path downloadDir) {
        this.downloadDir = downloadDir
    }

    Process create(String[] command) {
        return new ProcessBuilder()
                .directory(downloadDir.toFile())
                .command(command)
                .inheritIO()
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .start()
    }
}
