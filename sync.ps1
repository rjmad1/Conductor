# sync.ps1
# AntiGravity Auto-Sync Script for Conductor Documentation

Write-Host "Starting documentation sync..." -ForegroundColor Cyan

# 1. Pull latest changes from remote to stay in sync
Write-Host "Fetching latest changes from GitHub..." -ForegroundColor Yellow
git pull --rebase origin main
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error: Failed to pull remote changes. Please resolve any conflicts manually." -ForegroundColor Red
    exit $LASTEXITCODE
}

# 2. Check for local modifications
$status = git status --porcelain
if ([string]::IsNullOrEmpty($status)) {
    Write-Host "No local changes detected. Workspace is in sync with GitHub." -ForegroundColor Green
    exit 0
}

Write-Host "Detected local changes:" -ForegroundColor Yellow
Write-Host $status

# 3. Stage changes
Write-Host "Staging changes..." -ForegroundColor Yellow
git add .

# 4. Commit changes
$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
$commitMsg = "docs: automatic sync updates ($timestamp)"
Write-Host "Committing changes with message: '$commitMsg'..." -ForegroundColor Yellow
git commit -m $commitMsg

# 5. Push to GitHub
Write-Host "Pushing changes to GitHub..." -ForegroundColor Yellow
git push origin main
if ($LASTEXITCODE -eq 0) {
    Write-Host "Success! Documentation is successfully synchronized with GitHub." -ForegroundColor Green
} else {
    Write-Host "Error: Failed to push changes to GitHub." -ForegroundColor Red
}
