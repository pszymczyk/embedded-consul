# Embedded Consul

Embedded Consul provides easy way to run Consul (by HashiCorp) in integration tests.

[![Build Status](https://travis-ci.org/pszymczyk/embedded-consul.svg?branch=master)](https://travis-ci.org/pszymczyk/embedded-consul)


Built on Consul 1.4.2 <br />
Compatible with jdk1.8+. <br />
Working on all operating systems: Mac, Linux, Windows.

### How to get it?

``` xml
    <dependency>
      <groupId>com.pszymczyk.consul</groupId>
      <artifactId>embedded-consul</artifactId>
      <version>2.0.0</version>
      <scope>test</scope>
    </dependency>
```

``` groovy
    testCompile 'com.pszymczyk.consul:embedded-consul:2.0.0'
```

### Usage
#### Spring Boot setup

``` java
@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBootIntegrationTest {

    @BeforeClass
    public static void setup() {
        consul = ConsulStarterBuilder.consulStarter().build().start();

        System.setProperty("spring.cloud.consul.enabled", "true");
        System.setProperty("spring.cloud.consul.host", "localhost");
        System.setProperty("spring.cloud.consul.port", String.valueOf(consul.getHttpPort()));
    }

    @Test
    public void doSomethingWithSpring(){
        //use your spring beans
    }    
}
```

#### JUnit Rule

If JUnit is on classpath, simplest way to use `embedded-consul` is via
[JUnit rules](https://github.com/junit-team/junit4/wiki/Rules).

``` java
public class IntegrationTest {

    @ClassRule
    public static final ConsulResource consul = new ConsulResource();

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

#### JUnit 5 Extension

There is also an [extension](https://junit.org/junit5/docs/current/user-guide/#extensions) for JUnit 5.
Unlike in Junit 4 with `@Rule` and `@ClassRule` there is no option to control the scope of `ConsulProcess`.
`ConsulExtension` starts Consul for a whole class just like `@ClassRule`, but it resets the Consul state between tests.

``` java
class IntegrationTest {

    @RegisterExtension
    ConsulExtension consul = new ConsulExtension();

    private OkHttpClient client = new OkHttpClient();

    @Test
    void shouldStartConsul() throws Throwable {
        await().atMost(30, TimeUnit.SECONDS).until(() -> {
            Request request = new Request.Builder()
                    .url("http://localhost:" + consul.getHttpPort() + "/v1/agent/self")
                    .build();

            return client.newCall(request).execute().code() == 200;
        });
    }
}
```

The extension can also be registered via `@ExtendWith` annotation and provided to test as a method argument

``` java
@ExtendWith(ConsulExtension.class)
class IntegrationTest {

    @Test
    void test(ConsulProcess consul) {
        OkHttpClient client = new OkHttpClient();
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
    ├── embedded-consul-$consul-version
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

To avoid unnecessary downloads Consul binary is downloaded into static named directory `/$tmp/embedded-consul-$consul-version`.
Another stuff (ports config, raft, serf) is created in dynamically named temp directories.

At the moment files are not deleted!.

### Simultaneous running

Embedded Consul overrides all default [ports used by Consul](https://www.consul.io/docs/agent/options.html#ports).
Ports are randomized so it's possible to run multiple Consul Agent instances in single machine.
Configuration file is stored in `/$tmp/embedded-consul-config-dir$randomNumber/basic_config.json`, sample content:

```javascript

    {
        "ports": {
            "dns": 50084,
            "serf_lan": 50085,
            "serf_wan": 50086,
            "server": 50087
        },
        "disable_update_check": true,
        "performance": {
            "raft_multiplier": 1
        }
    }

```

### JDK 1.7.x support

Support for older Java versions has been dropped throughout release `2.0.0`. 
If you'd like to use `embedded-consul` with JDK 1.7 the `1.1.1` is the last  appropriate version.
