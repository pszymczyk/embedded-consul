# Embedded Consul

Embedded Consul provides easy way to run Consul (by HashiCorp) in integration tests.

### How to get it?

``` xml
    <dependency>
      <groupId>com.pszymczyk.consul</groupId>
      <artifactId>embedded-consul</artifactId>
      <version>0.1.6</version>
    </dependency>
```

``` groovy
    testCompile 'com.pszymczyk.consul:embedded-consul:0.1.6'
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

The ConsulProcess can be reset at any time, removing all registered services and removes all data from kv store. Invoking `reset` method
is faster than starting new Consul process.

```java

    consulClient.setKVBinaryValue("foo", "bar".getBytes())
    
    // do sth
    
    consul.reset()
    
    assert consulClient.getKVBinaryValue("foo").getValue() == null
```

### Files structure

```
    
    ├─$temp-directory
    │ 
    ├── embedded-consul
    │   ├── consul
    │   └── consul.zip
    ├── embedded-consul-data-dir + randomNumber
    │   ├── config.json   
    │   ├── raft
    │   │   ├── peers.json
    │   │   ├── raft.db
    │   │   └── snapshots
    │   └── serf
    │       ├── local.snapshot
    │       └── remote.snapshot

```

To avoid unnecessary downloads Consul binary is downloaded into static named directory `/$tmp/embedded-consul`. 
Another stuff (ports config, raft, serf) is created in dynamically named temp directories.

At the moment files are not deleted!.
  
### Simultaneous running 

Embedded Consul overrides all default [ports used by Consul](https://www.consul.io/docs/agent/options.html#ports). 
Ports are randomized so it's possible to run multiple Consul Agent instances in single machine. 
Configuration file is stored in `/$tmp/embedded-consul-data-dir$randomNumber/config.json`, sample content:
  
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

[Full example](https://github.com/pszymczyk/embedded-consul/blob/master/src/test/groovy/com/pszymczyk/embedded/consul/ConsulStarterTest.groovy#L41) 