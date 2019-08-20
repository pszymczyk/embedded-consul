package com.pszymczyk.consul.infrastructure;

import static java.util.Optional.ofNullable;

class StringUtils {

    static String requireNotBlank(String string, String message) {
        ofNullable(string)
                .map({it.trim()})
                .filter({!it.isEmpty()})
                .orElseThrow({new IllegalArgumentException(message)})
    }
}
