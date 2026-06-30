package com.conductor.shared.execution.provider;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

/** Metadata definition for a provider, detailing type, version, and capabilities. */
@Getter
@Builder
public class ProviderDefinition {
  private final String type;
  private final String name;
  private final String version;
  private final List<String> supportedActions;
}
