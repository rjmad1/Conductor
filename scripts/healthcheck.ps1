# Conductor Platform Validation PowerShell Healthcheck Script
# Validates connectivity to all local docker compose service ports on Windows.

$red = [ConsoleColor]::Red
$green = [ConsoleColor]::Green
$yellow = [ConsoleColor]::Yellow
$white = [ConsoleColor]::White

Write-Host "=== Conductor Platform Endpoint Diagnostics (Windows) ===" -ForegroundColor Green

function Check-Port {
    param (
        [string]$Name,
        [string]$HostAddress,
        [int]$Port
    )
    
    Write-Host ("Checking {0,-25} ... " -f $Name) -NoNewline
    
    $connection = Test-NetConnection -ComputerName $HostAddress -Port $Port -WarningAction SilentlyContinue
    
    if ($connection.TcpTestSucceeded) {
        Write-Host "[PASS] (Port listener active)" -ForegroundColor Green
        return $true
    } else {
        Write-Host "[FAIL] (Port unreachable)" -ForegroundColor Red
        return $false
    }
}

function Check-Http {
    param (
        [string]$Name,
        [string]$Url,
        [int]$ExpectedCode
    )
    
    Write-Host ("Checking {0,-25} ... " -f $Name) -NoNewline
    
    try {
        $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 2 -ErrorAction Stop
        $code = $response.StatusCode
    } catch {
        if ($_.Exception.Response) {
            $code = [int]$_.Exception.Response.StatusCode
        } else {
            $code = 0
        }
    }
    
    if ($code -eq $ExpectedCode -or ($ExpectedCode -eq 200 -and $code -eq 302)) {
        Write-Host ("[PASS] (HTTP {0})" -f $code) -ForegroundColor Green
        return $true
    } elseif ($code -eq 0) {
        Write-Host "[FAIL] (Connection refused/timeout)" -ForegroundColor Red
        return $false
    } else {
        Write-Host ("[WARN] (HTTP {0}, expected {1})" -f $code, $ExpectedCode) -ForegroundColor Yellow
        return $true
    }
}

$failedCount = 0

# --- Core Port Diagnostics ---
if (-not (Check-Port "PostgreSQL (Core DB)" "127.0.0.1" 5432)) { $failedCount++ }
if (-not (Check-Port "Redis (Cache Store)" "127.0.0.1" 6379)) { $failedCount++ }
if (-not (Check-Port "NATS (Message Bus)" "127.0.0.1" 4222)) { $failedCount++ }
if (-not (Check-Port "Temporal (gRPC Server)" "127.0.0.1" 7233)) { $failedCount++ }

# --- HTTP API Diagnostics ---
if (-not (Check-Http "Keycloak (Auth Server)" "http://localhost:8080/health/ready" 200)) { $failedCount++ }
if (-not (Check-Http "Kong (API Gateway)" "http://localhost:8000" 404)) { $failedCount++ }
if (-not (Check-Http "ClickHouse (Analytics)" "http://localhost:8123/ping" 200)) { $failedCount++ }
if (-not (Check-Http "Metabase (BI Portal)" "http://localhost:3000/api/health" 200)) { $failedCount++ }
if (-not (Check-Http "Qdrant (Vector DB)" "http://localhost:6333/readyz" 200)) { $failedCount++ }
if (-not (Check-Http "Grafana (UI Dashboards)" "http://localhost:3001/api/health" 200)) { $failedCount++ }
if (-not (Check-Http "Prometheus (Metrics)" "http://localhost:9090/-/healthy" 200)) { $failedCount++ }
if (-not (Check-Http "Loki (Log Store)" "http://localhost:3100/ready" 200)) { $failedCount++ }
if (-not (Check-Http "Tempo (Trace Store)" "http://localhost:3200/ready" 200)) { $failedCount++ }
if (-not (Check-Http "OTel Collector" "http://localhost:13133/" 200)) { $failedCount++ }

Write-Host ""
if ($failedCount -eq 0) {
    Write-Host "=== Validation Results: ALL HEALTHCHECKS PASSED ===" -ForegroundColor Green
    exit 0
} else {
    Write-Host "=== Validation Results: $failedCount HEALTHCHECKS FAILED ===" -ForegroundColor Red
    exit 1
}
