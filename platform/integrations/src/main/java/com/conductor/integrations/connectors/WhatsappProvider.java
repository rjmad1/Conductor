package com.conductor.integrations.connectors;

import com.conductor.shared.execution.provider.*;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WhatsappProvider implements Provider {

  private static final Logger log = LoggerFactory.getLogger(WhatsappProvider.class);

  private final RestTemplate restTemplate;
  private final String baseUrl;
  private final String phoneNumberId;
  private final String accessToken;

  public WhatsappProvider(
      @Value("${connector.whatsapp.base-url:https://graph.facebook.com/v19.0}") String baseUrl,
      @Value("${connector.whatsapp.phone-number-id:default}") String phoneNumberId,
      @Value("${connector.whatsapp.access-token:default}") String accessToken) {
    this.restTemplate = new RestTemplate();
    this.baseUrl = baseUrl;
    this.phoneNumberId = phoneNumberId;
    this.accessToken = accessToken;
  }

  @Override
  public ProviderDefinition getDefinition() {
    return ProviderDefinition.builder()
        .type("WHATSAPP")
        .name("WhatsApp Cloud API")
        .version("v19.0")
        .supportedActions(List.of("SEND_MESSAGE"))
        .build();
  }

  @Override
  public void connect(ProviderCredential credential, Map<String, Object> params) {
    log.debug("Connecting to WhatsApp Provider (no-op for stateless HTTP)");
  }

  @Override
  public void disconnect() {
    log.debug("Disconnecting WhatsApp Provider (no-op for stateless HTTP)");
  }

  @Override
  @SuppressWarnings("null")
  public ProviderResponse execute(ProviderRequest request) {
    String url =
        baseUrl
            + "/"
            + phoneNumberId
            + "/messages"
            + (request.getPath() != null ? request.getPath() : "");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(accessToken);

    if (request.getHeaders() != null) {
      request.getHeaders().forEach(headers::add);
    }

    HttpEntity<Object> entity = new HttpEntity<>(request.getBody(), headers);
    long startTime = System.currentTimeMillis();

    try {
      ResponseEntity<String> response =
          restTemplate.exchange(url, HttpMethod.valueOf(request.getMethod()), entity, String.class);
      long latency = System.currentTimeMillis() - startTime;

      return ProviderResponse.builder()
          .statusCode(response.getStatusCode().value())
          .body(response.getBody())
          .success(response.getStatusCode().is2xxSuccessful())
          .latencyMs(latency)
          .build();
    } catch (org.springframework.web.client.HttpStatusCodeException e) {
      long latency = System.currentTimeMillis() - startTime;
      log.error("WhatsApp API Error: {}", e.getResponseBodyAsString());
      return ProviderResponse.builder()
          .statusCode(e.getStatusCode().value())
          .body(e.getResponseBodyAsString())
          .success(false)
          .latencyMs(latency)
          .build();
    } catch (Exception e) {
      long latency = System.currentTimeMillis() - startTime;
      log.error("Failed to execute WhatsApp API request", e);
      return ProviderResponse.builder()
          .statusCode(500)
          .body(e.getMessage())
          .success(false)
          .latencyMs(latency)
          .build();
    }
  }

  @Override
  public ProviderHealth health() {
    return null; // Not implemented for this MVP
  }

  @Override
  public boolean validateConfiguration(Map<String, Object> config) {
    return true;
  }

  @Override
  public boolean verifyWebhook(Map<String, String> headers, String body, String secret) {
    return false;
  }

  @Override
  public ProviderCredential refreshCredentials(ProviderCredential credential) {
    return credential;
  }
}
