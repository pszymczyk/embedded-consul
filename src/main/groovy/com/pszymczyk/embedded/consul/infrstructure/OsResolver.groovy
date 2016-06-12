package com.pszymczyk.embedded.consul.infrstructure;

class OsResolver {

    static String resolve() {
        String os = System.getProperty('os.name').toLowerCase()
        def binaryVersion = 'linux'
        if (os.contains('mac')) {
            binaryVersion = 'darwin'
        } else if (os.contains('windows')) {
            binaryVersion = 'windows'
        }

        binaryVersion
    }
}
