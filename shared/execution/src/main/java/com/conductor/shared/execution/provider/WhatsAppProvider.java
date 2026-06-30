package com.conductor.shared.execution.provider;

import com.conductor.shared.security.HmacValidator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WhatsAppProvider implements Provider {

  private static final Logger log = LoggerFactory.getLogger(WhatsAppProvider.class);
  private final ProviderClient providerClient;
  private ProviderCredential credential;

  public WhatsAppProvider(ProviderClient providerClient) {
    this.providerClient = providerClient;
  }

  @Override
  public ProviderDefinition getDefinition() {
    return ProviderDefinition.builder()
        .type("WHATSAPP")
        .name("WhatsApp Cloud API Provider")
        .version("1.0")
        .supportedActions(List.of("SEND_MESSAGE"))
        .build();
  }

  @Override
  public void connect(ProviderCredential credential, Map<String, Object> params) {
    this.credential = credential;
    log.info("Connected to WhatsApp Provider");
  }

  @Override
  public void disconnect() {
    log.info("Disconnected from WhatsApp Provider");
  }

  @Override
  public ProviderResponse execute(ProviderRequest request) {
    log.info("Executing WhatsApp Provider request to path: {}", request.getPath());
    ProviderCredential cred = this.credential;
    if (cred == null) {
      cred =
          request.getHeaders() != null && request.getHeaders().containsKey("Authorization")
              ? ProviderCredential.builder()
                  .authType(ProviderCredential.AuthType.BEARER_TOKEN)
                  .token(request.getHeaders().get("Authorization"))
                  .build()
              : null;
    }
    return providerClient.execute("WHATSAPP", request, cred);
  }

  @Override
  public ProviderHealth health() {
    return ProviderHealth.builder().status(ProviderHealth.Status.UP).latencyMs(0).build();
  }

  @Override
  public boolean validateConfiguration(Map<String, Object> config) {
    return true;
  }

  @Override
  public boolean verifyWebhook(Map<String, String> headers, String body, String secret) {
    if (headers == null || body == null || secret == null) {
      return false;
    }
    String signature = headers.get("X-Hub-Signature-256");
    if (signature == null) {
      signature = headers.get("x-hub-signature-256");
    }
    if (signature == null) {
      return false;
    }
    if (signature.startsWith("sha256=")) {
      signature = signature.substring(7);
    }
    return HmacValidator.isValidSignature(
        body.getBytes(java.nio.charset.StandardCharsets.UTF_8), signature, secret);
  }

  @Override
  public ProviderCredential refreshCredentials(ProviderCredential credential) {
    return credential;
  }
}
