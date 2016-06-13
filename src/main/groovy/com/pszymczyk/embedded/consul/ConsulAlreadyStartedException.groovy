package com.pszymczyk.embedded.consul


class ConsulAlreadyStartedException extends RuntimeException {

    private static String MESSAGE = 'This Consul Starter instance already started Consul process. Create new ConsulStarter instance';

    ConsulAlreadyStartedException() {
        super(MESSAGE)
    }
}
