# Embedded Consul

Embedded Consul provides easy way to run Consul (by HashiCorp) in integration tests.

### Usage
``` java

    import com.pszymczyk.consul.ConsulProcess;
    import com.pszymczyk.consul.ConsulStarter;
    import com.pszymczyk.consul.ConsulStarterBuilder;
    
    public class IntegrationTest {
    
        private OkHttpClient client = new OkHttpClient();
        private ConsulProcess consul;
    
        @Before
        public void setup() {
            consul = ConsulStarterBuilder.consulStarter().build().start();
        }
    
        @After
        public void cleanup() throws Exception {
            consul.close();
        }
    
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