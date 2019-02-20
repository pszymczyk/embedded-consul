package com.pszymczyk.consul.infrastructure

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission

class AntUnzip {

    static void unzip(File zip, File destDir) {
        new AntBuilder().unzip(src: zip.absolutePath, dest: destDir.absolutePath, overwrite: 'false')
        if ('windows' != OsResolver.resolve()) {
            Files.setPosixFilePermissions(Paths.get(destDir.absolutePath, 'consul'), [PosixFilePermission.OWNER_EXECUTE] as Set)
        }
    }
}
