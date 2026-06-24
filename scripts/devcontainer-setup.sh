#!/usr/bin/env bash

# Devcontainer setup script for additional CLI tools
set -euo pipefail

echo "=== Devcontainer Environment CLI Setup ==="

# 1. Install Redis Client Tools
echo "Installing redis-tools..."
sudo apt-get update && sudo apt-get install -y redis-tools

# 2. Install NATS CLI
echo "Installing NATS CLI..."
go install github.com/nats-io/natscli/nats@latest

# 3. Install Temporal CLI
echo "Installing Temporal CLI..."
curl -sSf https://temporal.download/cli.sh | sh
# Symlink or move it to a standard binary directory
sudo ln -sf "$HOME/.temporalio/bin/temporal" /usr/local/bin/temporal

# 4. OpenTelemetry Tooling (otelcol, otel-cli)
echo "Installing OpenTelemetry CLI tool..."
go install github.com/equinix-labs/otel-cli@latest

echo "=== CLI Tool Setup Completed successfully ==="
