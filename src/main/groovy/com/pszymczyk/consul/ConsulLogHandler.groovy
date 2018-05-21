package com.pszymczyk.consul

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class ConsulLogHandler {

    private static final String CONSUL_LOG_FORMAT = /^(.+?)\[(\w+?)\]\s*(.+)$/
    private static final String LOGGING_THREAD_NAME = "embedded-consul"

    private final Logger consulLogger
    private final ExecutorService logExecutor

    private Future<?> logHandle

    ConsulLogHandler(final Logger customLogger) {
        this.consulLogger = customLogger ?: LoggerFactory.getLogger(ConsulStarter.class)
        this.logExecutor = Executors.newSingleThreadExecutor({ runnable ->
            Thread thread = new Thread(runnable)
            thread.setName(LOGGING_THREAD_NAME)
            return thread
        })
    }

    private static def parseConsulLog(String line) {
        LogLevel currentLevel = LogLevel.INFO
        String message = line

        def lineGroup = (line =~ CONSUL_LOG_FORMAT)
        if (lineGroup.matches() && lineGroup.hasGroup()) {
            def token = lineGroup[0]
            if (token.size() >= 4) {
                String levelToken = token[2]
                String messageToken = token[3]

                currentLevel = LogLevel.valueOf(levelToken) ?: LogLevel.INFO
                message = messageToken
            }
        }

        return [currentLevel, message]
    }

    def handleStream(final InputStream logStream) {
        def reader = new InputStreamReader(logStream)
        logHandle = logExecutor.submit({ ->
            reader.eachLine { line ->
                try {
                    def (currentLevel, message) = parseConsulLog(line)

                    switch (currentLevel) {
                        case LogLevel.TRACE:
                            consulLogger.trace(message)
                            break
                        case LogLevel.DEBUG:
                            consulLogger.debug(message)
                            break
                        case LogLevel.WARN:
                            consulLogger.warn(message)
                            break
                        case LogLevel.ERR:
                            consulLogger.error(message)
                            break
                        default:
                            consulLogger.info(message)
                    }
                } catch (Exception e) {
                    consulLogger.error("logging error: ", e)
                    return
                }
            }
            consulLogger.debug("log stream encounter EOF")
        })

        return logHandle
    }

    void close() {
        if (logHandle != null) {
            logHandle.cancel(false)
        }

        logExecutor.shutdown()
        final boolean done = logExecutor.awaitTermination(10, TimeUnit.SECONDS)
        if (!done) {
            logExecutor.shutdownNow()
        }
    }
}
