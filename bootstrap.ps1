# Conductor Platform Local Environment Bootstrap PowerShell Script
# Enforces system requirements, starts local docker compose stack, and validates health on Windows.

Write-Warning "========================================================================="
Write-Warning "DEPRECATION WARNING: Native Windows local bootstrap is deprecated."
Write-Warning "Please open this repository in a VS Code Dev Container instead."
Write-Warning "The Dev Container provides a unified Linux environment and avoids parity issues."
Write-Warning "========================================================================="

Write-Host "=== Conductor Platform Local Bootstrap (Windows) ===" -ForegroundColor Green

# 1. Dependency Validation
Write-Host "Checking dependencies..."

$docker = Get-Command docker -ErrorAction SilentlyContinue
if (-not $docker) {
    Write-Error "Docker is not installed. Please install Docker Desktop for Windows."
    exit 1
}
Write-Host "  - Docker: OK ($(docker --version))" -ForegroundColor Green

$compose = docker compose version
if (-not $?) {
    Write-Error "Docker Compose v2 is not available. Please update Docker Desktop."
    exit 1
}
Write-Host "  - Docker Compose: OK ($compose)" -ForegroundColor Green

# 2. Resource Checks
Write-Host "Verifying system resources..."
$computerSystem = Get-CimInstance CIM_PhysicalMemory
$totalRamBytes = ($computerSystem | Measure-Object -Property Capacity -Sum).Sum
$totalRamGB = [math]::Round($totalRamBytes / 1GB)

if ($totalRamGB -lt 12) {
    Write-Warning "Total System RAM is $totalRamGB GB. At least 12GB is recommended to run the full OSS stack locally."
} else {
    Write-Host "  - RAM: OK ($totalRamGB GB)" -ForegroundColor Green
}

# 3. Port Availability Checks
Write-Host "Checking port availability..."
$ports = @(5432, 6379, 8080, 7233, 8000, 8123, 3000, 3001, 9090, 3100, 3200, 13133)
$connections = Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue

foreach ($port in $ports) {
    $inUse = $connections | Where-Object { $_.LocalPort -eq $port }
    if ($inUse) {
        Write-Error "Port $port is already in use. Please free this port before starting."
        exit 1
    }
}
Write-Host "  - Ports: OK" -ForegroundColor Green

# 4. Declarative Config Provisioning
Write-Host "Provisioning config placeholders..."
if (-not (Test-Path "./platform/identity/realm-export.json")) {
    New-Item -ItemType Directory -Force -Path "./platform/identity" | Out-Null
    Set-Content -Path "./platform/identity/realm-export.json" -Value '{"realm": "conductor", "enabled": true}'
}
if (-not (Test-Path "./platform/identity/kong.yml")) {
    $kongContent = @"
_format_version: "3.0"
_transform: true
services:
  - name: keycloak-service
    url: http://keycloak:8080
    routes:
      - name: keycloak-route
        paths:
          - /auth
"@
    Set-Content -Path "./platform/identity/kong.yml" -Value $kongContent
}

# 5. Run Compose Stack
Write-Host "Starting local docker-compose services..." -ForegroundColor Green
docker compose -f docker-compose.local.yml up -d

Write-Host "Waiting 20 seconds for databases and services to initialize..." -ForegroundColor Yellow
Start-Sleep -Seconds 20

# 6. Run Health Validation
Write-Host "Executing healthchecks..." -ForegroundColor Green
powershell -File ./scripts/healthcheck.ps1
$res = $LastExitCode

if ($res -eq 0) {
    Write-Host "`nBootstrap completed successfully! Run 'docker compose -f docker-compose.local.yml logs -f' to monitor." -ForegroundColor Green
} else {
    Write-Host "`nBootstrap completed with healthcheck failures. Review logs to diagnose issues." -ForegroundColor Red
}

exit $res
