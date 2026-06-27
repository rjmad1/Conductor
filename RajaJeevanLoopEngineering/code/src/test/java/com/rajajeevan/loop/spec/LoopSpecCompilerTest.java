package com.rajajeevan.loop.spec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LoopSpecCompilerTest {

  private LoopSpecCompiler compiler;

  @TempDir File tempDir;

  @BeforeEach
  void setUp() {
    compiler = new LoopSpecCompiler();
  }

  @Test
  @DisplayName("Compiler formats and generates loop Markdown from JSON spec file")
  void testSuccessfulCompilation() throws IOException {
    File jsonSpec = new File(tempDir, "loop-999.json");
    File outputMd = new File(tempDir, "LOOP-999.md");

    // Write sample json spec
    String jsonContent =
        "{\n"
            + "  \"id\": \"LOOP-999\",\n"
            + "  \"name\": \"Test Execution Gate\",\n"
            + "  \"version\": \"1.0\",\n"
            + "  \"status\": \"Active\",\n"
            + "  \"category\": \"Core\",\n"
            + "  \"owner\": \"Architecture Lead\",\n"
            + "  \"purpose\": \"Test Purpose Description\",\n"
            + "  \"problemStatement\": \"Test Problem Statement\",\n"
            + "  \"whyExists\": \"Test Why Exists\",\n"
            + "  \"scope\": {\n"
            + "    \"inScope\": [\"scanning source files\"],\n"
            + "    \"outScope\": [\"making changes\"],\n"
            + "    \"maxDurationHours\": 2\n"
            + "  },\n"
            + "  \"inputs\": [\n"
            + "    { \"name\": \"repoPath\", \"type\": \"string\", \"source\": \"env\", \"required\": true }\n"
            + "  ],\n"
            + "  \"outputs\": [\n"
            + "    { \"artifact\": \"repoMap\", \"path\": \"docs/map.md\", \"description\": \"description\" }\n"
            + "  ]\n"
            + "}";
    Files.writeString(jsonSpec.toPath(), jsonContent);

    compiler.compile(jsonSpec, outputMd);

    assertThat(outputMd).exists();
    String renderedContent = Files.readString(outputMd.toPath());
    assertThat(renderedContent).contains("# LOOP-999 — Test Execution Gate");
    assertThat(renderedContent).contains("**Loop ID:** LOOP-999");
    assertThat(renderedContent).contains("Test Purpose Description");
    assertThat(renderedContent).contains("repoPath | string | env | Required");
    assertThat(renderedContent).contains("repoMap | docs/map.md | description");
  }

  @Test
  @DisplayName("Compiler validates loop ID properties")
  void testInvalidIdThrows() throws IOException {
    File badJson = new File(tempDir, "bad-loop.json");
    File output = new File(tempDir, "bad.md");

    String jsonContent = "{\"id\":\"BAD-1234\",\"name\":\"Broken ID Loop\",\"purpose\":\"test\"}";
    Files.writeString(badJson.toPath(), jsonContent);

    assertThatThrownBy(() -> compiler.compile(badJson, output))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("invalid loop ID layout");
  }
}
