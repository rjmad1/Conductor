package com.rajajeevan.loop.spec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Compiler to transform YAML/JSON loop specifications into production Markdown documentation
 * templates. Enforces rigid validation before code compile.
 */
public class LoopSpecCompiler {

  private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
  private final ObjectMapper jsonMapper = new ObjectMapper();

  /**
   * Compiles the specification file to a Markdown documentation file.
   *
   * @param specFile Input YAML or JSON file
   * @param outputFile Target Markdown path
   * @throws IOException If parsing or writing fails
   */
  public void compile(File specFile, File outputFile) throws IOException {
    LoopDefinition def = parseSpec(specFile);
    validate(def);

    // Create output directories if needed
    File parent = outputFile.getParentFile();
    if (parent != null && !parent.exists()) {
      parent.mkdirs();
    }

    try (PrintWriter writer =
        new PrintWriter(
            new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(outputFile),
                java.nio.charset.StandardCharsets.UTF_8))) {
      writer.println("---");
      writer.println("# GENERATED METADATA - DO NOT EDIT DIRECTLY");
      writer.println("Loop ID: " + def.getId());
      writer.println("Name: " + def.getName());
      writer.println("Version: " + def.getVersion());
      writer.println("Status: " + def.getStatus());
      writer.println("Category: " + def.getCategory());
      writer.println("---");
      writer.println();

      writer.println("# " + def.getId() + " — " + def.getName());
      writer.println();
      writer.println("**Loop ID:** " + def.getId() + "  ");
      writer.println("**Name:** " + def.getName() + "  ");
      writer.println("**Version:** " + def.getVersion() + "  ");
      writer.println("**Status:** " + def.getStatus() + "  ");
      writer.println("**Category:** " + def.getCategory() + "  ");

      String depends =
          (def.getDependsOn() == null || def.getDependsOn().isEmpty())
              ? "None"
              : String.join(", ", def.getDependsOn());
      writer.println("**Depends On:** " + depends + "  ");

      String gates =
          (def.getHumanGates() == null || def.getHumanGates().isEmpty())
              ? "None"
              : String.join(", ", def.getHumanGates());
      writer.println("**Human Gates:** " + gates + "  ");
      writer.println("**Owner:** " + def.getOwner() + "  ");
      if (def.getMaintainer() != null) {
        writer.println("**Maintainer:** " + def.getMaintainer() + "  ");
      }

      writer.println();
      writer.println("---");
      writer.println();
      writer.println("## Purpose");
      writer.println();
      writer.println(def.getPurpose());
      writer.println();

      writer.println("---");
      writer.println();
      writer.println("## Problem Statement");
      writer.println();
      writer.println(def.getProblemStatement());
      writer.println();

      writer.println("---");
      writer.println();
      writer.println("## Why This Loop Exists");
      writer.println();
      writer.println(def.getWhyExists());
      writer.println();

      writer.println("---");
      writer.println();
      writer.println("## Scope");
      writer.println();
      if (def.getScope() != null) {
        writer.println("**In scope:**");
        if (def.getScope().getInScope() != null) {
          for (String in : def.getScope().getInScope()) {
            writer.println("- " + in);
          }
        }
        writer.println();
        writer.println("**Out of scope:**");
        if (def.getScope().getOutScope() != null) {
          for (String out : def.getScope().getOutScope()) {
            writer.println("- " + out);
          }
        }
        writer.println();
        writer.println(
            "**Maximum run duration:** " + def.getScope().getMaxDurationHours() + " hours.");
      }
      writer.println();

      writer.println("---");
      writer.println();
      writer.println("## Inputs");
      writer.println();
      writer.println("| Input | Type | Source | Required |");
      writer.println("|-------|------|--------|----------|");
      if (def.getInputs() != null) {
        for (LoopDefinition.InputDef input : def.getInputs()) {
          writer.printf(
              "| %s | %s | %s | %s |\n",
              input.getName(),
              input.getType(),
              input.getSource(),
              input.isRequired() ? "Required" : "Optional");
        }
      }
      writer.println();

      writer.println("---");
      writer.println();
      writer.println("## Outputs");
      writer.println();
      writer.println("| Artifact | Path | Description |");
      writer.println("|----------|------|-------------|");
      if (def.getOutputs() != null) {
        for (LoopDefinition.OutputDef output : def.getOutputs()) {
          writer.printf(
              "| %s | %s | %s |\n",
              output.getArtifact(), output.getPath(), output.getDescription());
        }
      }
      writer.println();
    }
  }

  private LoopDefinition parseSpec(File file) throws IOException {
    String name = file.getName().toLowerCase();
    if (name.endsWith(".json")) {
      return jsonMapper.readValue(file, LoopDefinition.class);
    } else {
      return yamlMapper.readValue(file, LoopDefinition.class);
    }
  }

  private void validate(LoopDefinition def) {
    if (def == null) {
      throw new IllegalArgumentException("Loop specification cannot be null or empty");
    }
    if (def.getId() == null || !def.getId().matches("LOOP-[0-9]{3}")) {
      throw new IllegalArgumentException(
          "Loop specification validation failure: invalid loop ID layout (must be LOOP-XXX)");
    }
    if (def.getName() == null || def.getName().isBlank()) {
      throw new IllegalArgumentException("Loop specification validation failure: missing name");
    }
    if (def.getPurpose() == null || def.getPurpose().isBlank()) {
      throw new IllegalArgumentException(
          "Loop specification validation failure: missing purpose description");
    }
  }

  /** CLI Entry Point. */
  public static void main(String[] args) {
    if (args.length < 2) {
      System.err.println(
          "Usage: java com.rajajeevan.loop.spec.LoopSpecCompiler <input-spec-file> <output-markdown-file>");
      System.exit(1);
    }
    try {
      LoopSpecCompiler compiler = new LoopSpecCompiler();
      compiler.compile(new File(args[0]), new File(args[1]));
      System.out.println("Loop compiled successfully: " + args[1]);
    } catch (Exception e) {
      System.err.println("Compilation failed: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }
}
