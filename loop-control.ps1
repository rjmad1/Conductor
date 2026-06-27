# loop-control.ps1
#
# PowerShell client utility for interacting with the Loop Engine REST API.
#
# Usage:
#   .\loop-control.ps1 -Command health
#   .\loop-control.ps1 -Command status -LoopId LOOP-002
#   .\loop-control.ps1 -Command transit -LoopId LOOP-002 -SourcePhase DISCOVERY -TargetPhase PLANNING

[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("health", "status", "transit")]
    [string]$Command,

    [Parameter(Mandatory=$false)]
    [string]$LoopId,

    [Parameter(Mandatory=$false)]
    [string]$SourcePhase,

    [Parameter(Mandatory=$false)]
    [string]$TargetPhase,

    [Parameter(Mandatory=$false)]
    [string]$ExecutionLogs = ""
)

$BaseUrl = $env:LOOP_ENGINE_URL
if ([string]::IsNullOrEmpty($BaseUrl)) {
    $BaseUrl = "http://127.0.0.1:8080"
}

switch ($Command) {
    "health" {
        $Uri = "$BaseUrl/health"
        Write-Host "-> Checking health: $Uri" -ForegroundColor Gray
        try {
            $Response = Invoke-RestMethod -Uri $Uri -Method Get
            $Response | ConvertTo-Json -Depth 5
        } catch {
            Write-Error "Failed to connect to Loop Engine: $_"
        }
    }

    "status" {
        if ([string]::IsNullOrEmpty($LoopId)) {
            Write-Error "-LoopId is required for status command"
            return
        }
        $Uri = "$BaseUrl/api/v1/loops/$LoopId/status"
        Write-Host "-> Fetching status for loop '$LoopId': $Uri" -ForegroundColor Gray
        try {
            $Response = Invoke-RestMethod -Uri $Uri -Method Get
            $Response | ConvertTo-Json -Depth 5
        } catch {
            Write-Error "Failed to get status for loop '$LoopId': $_"
        }
    }

    "transit" {
        if ([string]::IsNullOrEmpty($LoopId) -or [string]::IsNullOrEmpty($SourcePhase) -or [string]::IsNullOrEmpty($TargetPhase)) {
            Write-Error "-LoopId, -SourcePhase, and -TargetPhase are required for transit command"
            return
        }
        $Uri = "$BaseUrl/api/v1/loops/transit"
        Write-Host "-> Requesting transition: $SourcePhase -> $TargetPhase for loop '$LoopId'" -ForegroundColor Gray
        $Body = @{
            loop_id = $LoopId
            source_phase = $SourcePhase
            target_phase = $TargetPhase
            execution_logs = $ExecutionLogs
        } | ConvertTo-Json

        try {
            $Response = Invoke-RestMethod -Uri $Uri -Method Post -Body $Body -ContentType "application/json"
            $Response | ConvertTo-Json -Depth 5
        } catch {
            Write-Error "Failed to transit loop: $_"
            if ($null -ne $_.Exception.Response) {
                $Reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
                $ErrBody = $Reader.ReadToEnd()
                Write-Host "Response Body: $ErrBody" -ForegroundColor Red
            }
        }
    }
}
