package com.conductor.integrations.framework;

import java.net.InetSocketAddress;
import java.net.Proxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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

  /** Returns a RestTemplate for general use (no explicit timeout). */
  public RestTemplate getRestTemplate() {
    return buildRestTemplate(-1, -1);
  }

  /**
   * Returns a RestTemplate configured with explicit connect and read timeouts. Use for health
   * checks and any bounded-latency operations.
   */
  public RestTemplate getRestTemplate(int connectTimeoutMs, int readTimeoutMs) {
    return buildRestTemplate(connectTimeoutMs, readTimeoutMs);
  }

  private RestTemplate buildRestTemplate(int connectTimeoutMs, int readTimeoutMs) {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    if (connectTimeoutMs > 0) {
      factory.setConnectTimeout(connectTimeoutMs);
    }
    if (readTimeoutMs > 0) {
      factory.setReadTimeout(readTimeoutMs);
    }
    if (proxyEnabled) {
      log.debug("Building HTTP client via proxy {}:{}", proxyHost, proxyPort);
      factory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
    }
    return new RestTemplate(factory);
  }
}
