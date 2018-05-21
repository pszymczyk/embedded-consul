package com.pszymczyk.consul

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

import java.util.concurrent.Future

class ConsulLogHandlerTest extends Specification {

    private static final Logger logger = LoggerFactory.getLogger(ConsulLogHandlerTest.class)

    ConsulLogHandler handler
    Logger mockLogger
    PipedOutputStream output
    Future<?> loggingHandle

    def setup() {
        this.mockLogger = Mock(Logger.class)
        this.handler = new ConsulLogHandler(mockLogger)
        this.output = new PipedOutputStream()

        InputStream input = new PipedInputStream(this.output)
        this.loggingHandle = handler.handleStream(input)
    }

    def "should create instance successfully with null"() {
        when:
        final ConsulLogHandler handler = new ConsulLogHandler(null)

        then:
        handler != null
    }

    def "should handle consul log with slf4j logger"() {
        given:
        final PrintWriter writer = output.newPrintWriter()
        final String testCase = "2018/05/21 14:35:47 [DEBUG] Skipping remote check \"serfHealth\" since it is managed automatically"

        when:
        logger.debug("testCase: {}", testCase)
        writer.println(testCase)
        writer.flush()
        Thread.sleep(1_000L)

        then:
        1 * mockLogger.debug("Skipping remote check \"serfHealth\" since it is managed automatically")
    }

    def "should handle consul startup log with slf4j logger"() {
        given:
        final PrintWriter writer = output.newPrintWriter()
        final String testCase = "==> Starting Consul agent..."

        when:
        logger.debug("testCase: {}", testCase)
        writer.println(testCase)
        writer.flush()
        Thread.sleep(1_000L)

        then:
        1 * mockLogger.info("==> Starting Consul agent...")
    }

    def "should handle logs"() {
        given:
        final PrintWriter writer = output.newPrintWriter()
        final String testCase = "==> Starting Consul agent...\n" +
            "==> Consul agent running!\n" +
            "           Version: 'v1.0.1'\n" +
            "           Node ID: 'c47ec383-bceb-1013-14f2-d3fd095c5dbe'\n" +
            "         Node name: 'testNode'\n" +
            "        Datacenter: 'dc1' (Segment: '<all>')\n" +
            "            Server: true (Bootstrap: false)\n" +
            "       Client Addr: [127.0.0.1] (HTTP: 8500, HTTPS: -1, DNS: 8600)\n" +
            "      Cluster Addr: 127.0.0.1 (LAN: 8301, WAN: 8302)\n" +
            "           Encrypt: Gossip: false, TLS-Outgoing: false, TLS-Incoming: false\n" +
            "\n" +
            "==> Log data will now stream in as it occurs:\n" +
            "\n" +
            "    2018/05/21 14:35:46 [DEBUG] Using random ID \"c47ec383-bceb-1013-14f2-d3fd095c5dbe\" as node ID\n" +
            "    2018/05/21 14:35:46 [INFO] raft: Initial configuration (index=1): [{Suffrage:Voter ID:c47ec383-bceb-1013-14f2-d3fd095c5dbe Address:127.0.0.1:8300}]\n" +
            "    2018/05/21 14:35:46 [INFO] raft: Node at 127.0.0.1:8300 [Follower] entering Follower state (Leader: \"\")\n" +
            "    2018/05/21 14:35:46 [INFO] serf: EventMemberJoin: testNode.dc1 127.0.0.1\n" +
            "    2018/05/21 14:35:46 [INFO] serf: EventMemberJoin: testNode 127.0.0.1\n" +
            "    2018/05/21 14:35:46 [INFO] consul: Adding LAN server testNode (Addr: tcp/127.0.0.1:8300) (DC: dc1)\n" +
            "    2018/05/21 14:35:46 [INFO] consul: Handled member-join event for server \"testNode.dc1\" in area \"wan\"\n"

        when:
        logger.debug("testCase: {}", testCase)
        writer.print(testCase)
        writer.flush()
        Thread.sleep(1_000L)

        then:
        13 * mockLogger.info(_ as String)
        1 * mockLogger.debug(_ as String)
        6 * mockLogger.info(_ as String)
    }
}
