package com.rajajeevan.loop.spec;

import java.util.List;
import lombok.Data;

/** Java representation of a Loop Specification parsed from YAML/JSON schema configurations. */
@Data
public class LoopDefinition {
  private String id;
  private String name;
  private String version;
  private String status;
  private String category;
  private List<String> dependsOn;
  private List<String> humanGates;
  private String owner;
  private String maintainer;
  private String purpose;
  private String problemStatement;
  private String whyExists;
  private ScopeDef scope;
  private List<InputDef> inputs;
  private List<OutputDef> outputs;

  @Data
  public static class ScopeDef {
    private List<String> inScope;
    private List<String> outScope;
    private int maxDurationHours;
  }

  @Data
  public static class InputDef {
    private String name;
    private String type;
    private String source;
    private boolean required;
  }

  @Data
  public static class OutputDef {
    private String artifact;
    private String path;
    private String description;
  }
}
