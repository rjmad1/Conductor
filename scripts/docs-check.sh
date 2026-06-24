#!/usr/bin/env bash

# Docs and Threat Model Integrity Check Script
set -euo pipefail

echo "=== Running Threat Model & Trust Boundaries Verification ==="

# 1. Check required files
for file in "THREAT_MODEL.md" "TRUST_BOUNDARIES.md" "ARCHITECTURE_GUARDRAILS.md"; do
  if [ ! -f "$file" ]; then
    echo "Error: Required document $file is missing!" >&2
    exit 1
  fi
  echo "  - $file: Present"
done

# 2. Extract and validate file:// links
echo "Validating workspace file links..."
errors=0

# Find all file:// links in markdown files in root and docs directory
# Pattern handles both upper/lowercase drive letters.
# Grep output format is file.md:file:///...
mapfile -t links < <(grep -o -h -r -E 'file:///[cC]:/Users/rajaj/Projects/Conductor/[a-zA-Z0-9_\/\.\-#]+' *.md docs/ 2>/dev/null || true)

for link in "${links[@]}"; do
  # Remove trailing anchors like #L10-L20
  clean_link=$(echo "$link" | cut -d'#' -f1)
  
  # Convert file URL to relative path in git repo
  # Strip "file:///c:/Users/rajaj/Projects/Conductor/" or "file:///C:/Users/rajaj/Projects/Conductor/"
  rel_path=$(echo "$clean_link" | sed -E 's|file:///[cC]:/Users/rajaj/Projects/Conductor/||g')
  
  # Ensure we ignore empty paths that might happen due to stripping the root path itself
  if [ -n "$rel_path" ] && [ "$rel_path" != "file:///c:/Users/rajaj/Projects/Conductor" ] && [ "$rel_path" != "file:///C:/Users/rajaj/Projects/Conductor" ]; then
    if [ ! -e "$rel_path" ]; then
      echo "Error: Link to '$clean_link' is broken (path '$rel_path' does not exist)" >&2
      errors=$((errors + 1))
    fi
  fi
done

if [ $errors -gt 0 ]; then
  echo "=== Validation FAILED with $errors broken links ===" >&2
  exit 1
fi

echo "=== Verification Completed Successfully! ==="
exit 0
