# Embedded Consul

Embedded Consul provides easy way to run Consul (by HashiCorp) in integration tests.

### Usage
``` java

    import com.pszymczyk.embedded.consul.ConsulProcess;
    import com.pszymczyk.embedded.consul.ConsulStarter;
    import com.pszymczyk.embedded.consul.ConsulStarterBuilder;
    
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