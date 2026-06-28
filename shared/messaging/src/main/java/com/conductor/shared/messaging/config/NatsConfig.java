package com.conductor.shared.messaging.config;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NatsConfig {

  @Value("${nats.url:nats://localhost:4222}")
  private String natsUrl;

  @Bean
  public Connection natsConnection() throws IOException, InterruptedException {
    Options options = new Options.Builder().server(natsUrl).maxReconnects(-1).build();
    return Nats.connect(options);
  }
}
