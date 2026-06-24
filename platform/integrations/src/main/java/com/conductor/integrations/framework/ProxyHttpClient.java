package com.conductor.integrations.framework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.net.InetSocketAddress;
import java.net.Proxy;

@Component
public class ProxyHttpClient {

    private static final Logger log = LoggerFactory.getLogger(ProxyHttpClient.class);

    private final boolean proxyEnabled;
    private final String proxyHost;
    private final int proxyPort;

    public ProxyHttpClient(
            @Value("${integration.proxy.enabled:false}") boolean proxyEnabled,
            @Value("${integration.proxy.host:localhost}") String proxyHost,
            @Value("${integration.proxy.port:3128}") int proxyPort) {
        this.proxyEnabled = proxyEnabled;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    public RestTemplate getRestTemplate() {
        if (proxyEnabled) {
            log.info("Creating HTTP client routing through Squid proxy {}:{}", proxyHost, proxyPort);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setProxy(proxy);
            return new RestTemplate(factory);
        } else {
            log.debug("Proxy egress is disabled, returning direct-route RestTemplate");
            return new RestTemplate();
        }
    }
}
