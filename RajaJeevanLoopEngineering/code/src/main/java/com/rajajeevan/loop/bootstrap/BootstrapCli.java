package com.rajajeevan.loop.bootstrap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Native Java implementation of the Loop Engineering Framework Bootstrapper CLI. Standardizes
 * workspace provisioning cross-platform without PowerShell dependencies.
 */
public class BootstrapCli {

  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println(
          "Usage: java com.rajajeevan.loop.bootstrap.BootstrapCli <target-project-path> <project-type> [overwrite-devcontainer: true/false]");
      System.out.println(
          "Project types: Greenfield, Brownfield, Modernization, IncidentResponse, All");
      System.exit(1);
    }

    String targetPath = args[0];
    String projectType = args[1];
    boolean overwriteDevcontainer = args.length > 2 && Boolean.parseBoolean(args[2]);

    try {
      BootstrapCli cli = new BootstrapCli();
      cli.run(targetPath, projectType, overwriteDevcontainer);
    } catch (Exception e) {
      System.err.println("Bootstrapping failed: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }

  public void run(String targetProjectPath, String projectType, boolean overwriteDevcontainer)
      throws Exception {
    System.out.println("=== RajaJeevanLoopEngineering Java Bootstrap CLI ===");

    File targetDir = new File(targetProjectPath);
    if (!targetDir.exists()) {
      System.out.println("[+] Creating target project folder: " + targetProjectPath);
      targetDir.mkdirs();
    }

    // Determine current source library root (parent of code directory or current working directory)
    File srcLibRoot = new File(".").getAbsoluteFile();
    if (new File(srcLibRoot, "code").exists()) {
      // Running from workspace root
    } else if (new File(srcLibRoot, "src").exists() && srcLibRoot.getParentFile() != null) {
      // Running from inside code directory
      srcLibRoot = srcLibRoot.getParentFile();
    }

    System.out.println("Source Root: " + srcLibRoot.getAbsolutePath());
    System.out.println("Target Path: " + targetDir.getAbsolutePath());
    System.out.println("Project Type: " + projectType);

    // Setup Target Directories
    File targetAgentsDir = new File(targetDir, ".agents");
    File targetDocsDir = new File(targetDir, "docs/loops");
    File targetCodeDir = new File(targetDir, "RajaJeevanLoopEngineering/code");
    File targetDevContainerDir = new File(targetDir, ".devcontainer");

    createDirectory(targetAgentsDir);
    createDirectory(targetDocsDir);
    createDirectory(targetCodeDir);
    createDirectory(targetDevContainerDir);

    // 1. Copy Shared Standards
    File sharedSrc = new File(srcLibRoot, "shared");
    File sharedDest = new File(targetDocsDir, "shared");
    if (sharedSrc.exists()) {
      System.out.println("[+] Provisioning shared standards docs...");
      copyDirectory(sharedSrc.toPath(), sharedDest.toPath());
    }

    // 2. Contextual Loop Provisioning
    System.out.println("[+] Copying contextual loop process definitions...");
    List<String> loopsToPort = getLoopsForType(srcLibRoot, projectType);

    if (projectType.equalsIgnoreCase("All")) {
      File loopsSrc = new File(srcLibRoot, "loops");
      if (loopsSrc.exists()) {
        copyDirectory(loopsSrc.toPath(), targetDocsDir.toPath());
      }
    } else {
      for (String loopRelPath : loopsToPort) {
        File fullSrcFile = new File(new File(srcLibRoot, "loops"), loopRelPath);
        if (fullSrcFile.exists()) {
          File categoryDestDir = new File(targetDocsDir, fullSrcFile.getParentFile().getName());
          categoryDestDir.mkdirs();
          Files.copy(
              fullSrcFile.toPath(),
              new File(categoryDestDir, fullSrcFile.getName()).toPath(),
              StandardCopyOption.REPLACE_EXISTING);
          System.out.println("    - Imported loop: " + loopRelPath);
        } else {
          System.err.println(
              "Warning: Source loop file not found: " + fullSrcFile.getAbsolutePath());
        }
      }
    }

    // 3. Port Templates and base agent rules
    System.out.println("[+] Copying agent templates...");
    File templateSrc = new File(srcLibRoot, "templates");
    File templateDest = new File(targetAgentsDir, "templates");
    if (templateSrc.exists()) {
      copyDirectory(templateSrc.toPath(), templateDest.toPath());

      File agentsDestFile = new File(targetAgentsDir, "AGENTS.md");
      if (!agentsDestFile.exists()) {
        File templateBase = new File(templateSrc, "AGENTS-TEMPLATE.md");
        if (templateBase.exists()) {
          Files.copy(templateBase.toPath(), agentsDestFile.toPath());
          System.out.println("    - Provisioned base .agents/AGENTS.md");
        }
      }
    }

    // 4. Port Rule Engine and standalone execution wrapper
    System.out.println("[+] Copying core Java rule execution engine...");
    File codeSrc = new File(srcLibRoot, "code");
    if (codeSrc.exists()) {
      copyDirectory(codeSrc.toPath(), targetCodeDir.toPath());

      // Copy Gradle wrappers
      copyWrapperFile(srcLibRoot, targetCodeDir, "gradlew");
      copyWrapperFile(srcLibRoot, targetCodeDir, "gradlew.bat");
      File gradleFolderSrc = new File(srcLibRoot, "gradle");
      File gradleFolderDest = new File(targetCodeDir, "gradle");
      if (gradleFolderSrc.exists()) {
        copyDirectory(gradleFolderSrc.toPath(), gradleFolderDest.toPath());
      }
    }

    // 5. Generate secure, sandboxed Dev Container config
    System.out.println("[+] Creating secure Dev Container sandboxing...");
    File devContainerFile = new File(targetDevContainerDir, "devcontainer.json");
    File setupScriptFile = new File(targetDevContainerDir, "devcontainer-setup.sh");

    if (!devContainerFile.exists() || overwriteDevcontainer) {
      writeStringToFile(devContainerFile, getDevContainerJson());
      writeStringToFile(setupScriptFile, getSetupScriptSh());
      setupScriptFile.setExecutable(true);
      System.out.println("    - Dev Container files written and execution limits enforced.");
    } else {
      System.out.println(
          "    - Dev Container files already exist. Skipping. (Use overwrite flag to force)");
    }

    System.out.println("=== Java Bootstrapping Completed Successfully ===");
  }

  private void createDirectory(File dir) {
    if (!dir.exists()) {
      dir.mkdirs();
    }
  }

  private void copyDirectory(Path source, Path target) throws IOException {
    Files.walkFileTree(
        source,
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
              throws IOException {
            Path targetDir = target.resolve(source.relativize(dir));
            Files.createDirectories(targetDir);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            Files.copy(
                file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
            return FileVisitResult.CONTINUE;
          }
        });
  }

  private void copyWrapperFile(File srcRoot, File targetDir, String name) {
    File srcFile = new File(srcRoot, name);
    if (srcFile.exists()) {
      try {
        Path dest = new File(targetDir, name).toPath();
        Files.copy(srcFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
        dest.toFile().setExecutable(true);
      } catch (Exception e) {
        System.err.println("Warning: Failed to copy wrapper file " + name + ": " + e.getMessage());
      }
    }
  }

  private void writeStringToFile(File file, String content) throws IOException {
    try (FileWriter writer = new FileWriter(file)) {
      writer.write(content);
    }
  }

  private List<String> getLoopsForType(File srcLibRoot, String type) {
    File manifestFile = new File(srcLibRoot, "shared/loops-manifest.json");
    if (!manifestFile.exists()) {
      System.err.println(
          "Warning: Loops manifest file not found at " + manifestFile.getAbsolutePath());
      return Collections.emptyList();
    }
    try {
      ObjectMapper mapper = new ObjectMapper();
      Map<String, List<String>> manifest =
          mapper.readValue(manifestFile, new TypeReference<Map<String, List<String>>>() {});

      for (Map.Entry<String, List<String>> entry : manifest.entrySet()) {
        if (entry.getKey().equalsIgnoreCase(type)) {
          return entry.getValue();
        }
      }
      System.err.println("Warning: Project type '" + type + "' not found in manifest.");
    } catch (Exception e) {
      System.err.println("Error reading loops manifest: " + e.getMessage());
    }
    return Collections.emptyList();
  }

  private String getDevContainerJson() {
    return "{\n"
        + "  \"name\": \"Loop Engineering Dev Environment\",\n"
        + "  \"image\": \"mcr.microsoft.com/devcontainers/universal:2\",\n"
        + "  \"features\": {\n"
        + "    \"ghcr.io/devcontainers/features/docker-outside-of-docker:1\": {\n"
        + "      \"moby\": true,\n"
        + "      \"version\": \"latest\"\n"
        + "    },\n"
        + "    \"ghcr.io/devcontainers/features/java:1\": {\n"
        + "      \"version\": \"21\"\n"
        + "    }\n"
        + "  },\n"
        + "  \"remoteUser\": \"vscode\",\n"
        + "  \"runArgs\": [\n"
        + "    \"--cap-drop=all\",\n"
        + "    \"--security-opt=no-new-privileges\"\n"
        + "  ],\n"
        + "  \"customizations\": {\n"
        + "    \"vscode\": {\n"
        + "      \"extensions\": [\n"
        + "        \"redhat.vscode-yaml\",\n"
        + "        \"vscjava.vscode-java-pack\"\n"
        + "      ]\n"
        + "    }\n"
        + "  },\n"
        + "  \"postCreateCommand\": \"bash .devcontainer/devcontainer-setup.sh\"\n"
        + "}\n";
  }

  private String getSetupScriptSh() {
    return "#!/usr/bin/env bash\n"
        + "set -euo pipefail\n"
        + "echo \"=== Initializing Loop Engineering Dev Container ===\"\n"
        + "if [ -d \"RajaJeevanLoopEngineering/code\" ]; then\n"
        + "    echo \"Bootstrapping loop Java rule engine...\"\n"
        + "    chmod +x RajaJeevanLoopEngineering/code/gradlew || true\n"
        + "    (cd RajaJeevanLoopEngineering/code && ./gradlew test) || echo \"Warning: Initial tests failed.\"\n"
        + "fi\n"
        + "echo \"=== Dev Container Loop Setup Completed Successfully ===\"\n";
  }
}
