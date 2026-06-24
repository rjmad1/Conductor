package com.conductor.integrations;

import com.conductor.integrations.framework.ProxyHttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import static org.junit.jupiter.api.Assertions.*;

public class EgressProxyTest {

    @Test
    public void testProxyClientConfiguration() {
        ProxyHttpClient helperEnabled = new ProxyHttpClient(true, "127.0.0.1", 3128);
        RestTemplate templateEnabled = helperEnabled.getRestTemplate();

        assertNotNull(templateEnabled);
        assertTrue(templateEnabled.getRequestFactory() instanceof SimpleClientHttpRequestFactory);

        ProxyHttpClient helperDisabled = new ProxyHttpClient(false, "127.0.0.1", 3128);
        RestTemplate templateDisabled = helperDisabled.getRestTemplate();
        assertNotNull(templateDisabled);
    }
}
