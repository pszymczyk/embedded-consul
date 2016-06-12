package com.pszymczyk.embedded.consul;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import com.pszymczyk.embedded.consul.ConsulProcess;
import com.pszymczyk.embedded.consul.ConsulStarter;

public class IntegrationTest {

    private OkHttpClient client = new OkHttpClient();
    private ConsulProcess consul;

    @Before
    public void setup() {
        consul = new ConsulStarter().start();
    }

    @After
    public void cleanup() throws Exception {
        consul.close();
    }

    @Test
    public void shouldStartConsul() throws Throwable {
        await().atMost(30, TimeUnit.SECONDS).until(() -> {
            Request request = new Request.Builder()
                    .url("http://localhost:$consul.httpPort/v1/agent/self")
                    .build();

            return client.newCall(request).execute().code() == 200;
        });
    }
}
