# Embedded Consul

Embedded Consul provides easy way to run Consul (by HashiCorp) in integration tests.

[![Build Status](https://travis-ci.org/pszymczyk/embedded-consul.svg?branch=master)](https://travis-ci.org/pszymczyk/embedded-consul)


Built on Consul 0.7.5 <br />
Compatible with jdk1.7+. <br />
Working on all operating systems: Mac, Linux, Windows.

### How to get it?

``` xml
    <dependency>
      <groupId>com.pszymczyk.consul</groupId>
      <artifactId>embedded-consul</artifactId>
      <version>0.2.3</version>
    </dependency>
```

``` groovy
    testCompile 'com.pszymczyk.consul:embedded-consul:0.2.3'
```

### Usage

#### JUnit Rule

If JUnit is on classpath, simplest way to use `embedded-consul` is via
[JUnit rules](https://github.com/junit-team/junit4/wiki/Rules).

``` java
public class IntegrationTest {

    @ClassRule
    private static final ConsulResource consul = new ConsulResource();

    private OkHttpClient client = new OkHttpClient();

    @Test
    public void shouldStartConsul() throws Throwable {
        await().atMost(30, TimeUnit.SECONDS).until(() -> {
            Request request = new Request.Builder()
                    .url("http://localhost:" + consul.getHttpPort() + "/v1/agent/self")
                    .build();

            return client.newCall(request).execute().code() == 200;
        });
    }
}
```

#### Manual
``` java

public class IntegrationTest {

    private ConsulProcess consul;

    @Before
    public void setup() {
        consul = ConsulStarterBuilder.consulStarter().build().start();
    }

    @After
    public void cleanup() throws Exception {
        consul.close();
    }

    /* tests as in example above */
```

### Reset Consul state

The ConsulProcess can be reset at any time. Reset method do few operations:
- removing all registered services
- removes all registered checks
- removes all data from kv store
- destroy all active sessions

Invoking `reset` method is faster than starting new Consul process.

```java

    consulClient.setKVBinaryValue("foo", "bar".getBytes())

    // do sth

    consul.reset()

    assert consulClient.getKVBinaryValue("foo").getValue() == null
```

### Passing custom configuration

If you want to pass custom property which is not covered by ConsulBuilder you can pass JSON configuration:

```java

String customConfiguration =
                "{" +
                    "\"datacenter\": \"test-dc\"," +                    
                    "\"log_level\": \"INFO\"," +
                    "\"node_name\": \"foobar\"" +
                "}";

ConsulProcess consul = ConsulStarterBuilder.consulStarter().withCustomConfig(customConfiguration).build().start();    

```

Given JSON configuration will be saved in addition configuration file `extra_config.json` and processed after base
configuration (with highest priority).

### Changing where to download Consul

An environment variable can be set to change the consul CDN:

```bash
# default
export CONSUL_BINARY_CDN=https://releases.hashicorp.com/consul/
```

### Files structure

```

    ├─$temp-directory
    │ 
    ├── embedded-consul
    │   ├── consul
    │   └── consul.zip
    ├── embedded-consul-data-dir + randomNumber
    │   ├── raft
    │   │   ├── peers.json
    │   │   ├── raft.db
    │   │   └── snapshots
    │   └── serf
    │       ├── local.snapshot
    │       └── remote.snapshot
    ├── embedded-consul-config-dir + randomNumber
    │   ├── basic_config.json   
    │   └── extra_config.json
```

To avoid unnecessary downloads Consul binary is downloaded into static named directory `/$tmp/embedded-consul`.
Another stuff (ports config, raft, serf) is created in dynamically named temp directories.

At the moment files are not deleted!.

### Simultaneous running

Embedded Consul overrides all default [ports used by Consul](https://www.consul.io/docs/agent/options.html#ports).
Ports are randomized so it's possible to run multiple Consul Agent instances in single machine.
Configuration file is stored in `/$tmp/embedded-consul-config-dir$randomNumber/basic_config.json`, sample content:

```javascript

    {
        "ports": {
            "dns": 64294,
            "rpc": 64295,
            "serf_lan": 64296,
            "serf_wan": 64297,
            "server": 64298
        }
    }

```
