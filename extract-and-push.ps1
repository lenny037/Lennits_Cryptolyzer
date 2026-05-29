#!/usr/bin/env bash

################################################################################
# LEMME'S CRYPTOLYZER — POWERSHELL EXECUTION WRAPPER (Windows Alternative)
# For Windows systems without batch compatibility
#
# Usage: powershell -ExecutionPolicy Bypass -File extract-and-push.ps1
################################################################################

# Force stop on errors
$ErrorActionPreference = "Stop"

# Colors
$Colors = @{
    'Info' = 'Cyan'
    'Success' = 'Green'
    'Error' = 'Red'
    'Warning' = 'Yellow'
}

function Write-Log {
    param(
        [Parameter(Mandatory=$true)]
        [string]$Message,
        [Parameter(Mandatory=$false)]
        [string]$Type = 'Info'
    )
    
    $color = $Colors[$Type]
    $prefix = @{
        'Info' = '[INFO]'
        'Success' = '[✓]'
        'Error' = '[✗]'
        'Warning' = '[!]'
    }[$Type]
    
    Write-Host "$prefix $Message" -ForegroundColor $color
}

# Configuration
$BranchName = "extracted-content"
$OutputDir = "extracted_source"

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║    LENNIT'S CRYPTOLYZER — EXTRACTION & GITHUB PUSH (PowerShell)        ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check prerequisites
Write-Log "Checking prerequisites..."

try {
    git --version | Out-Null
    Write-Log "Git found" -Type Success
} catch {
    Write-Log "Git not found! Download: https://git-scm.com/download/win" -Type Error
    exit 1
}

# Check for 7-Zip
$7z = $null
if (Test-Path "C:\Program Files\7-Zip\7z.exe") {
    $7z = "C:\Program Files\7-Zip\7z.exe"
} elseif (Test-Path "C:\Program Files (x86)\7-Zip\7z.exe") {
    $7z = "C:\Program Files (x86)\7-Zip\7z.exe"
} else {
    Write-Log "7-Zip not found! Download: https://www.7-zip.org/download.html" -Type Warning
    Write-Log "Attempting to use PowerShell built-in Expand-Archive..." -Type Info
}

if ($7z) {
    Write-Log "7-Zip found: $7z" -Type Success
}

# Step 2: Detect split archives
Write-Log "Detecting split archives..."

$archives = @()
for ($i = 1; $i -le 10; $i++) {
    $num = "{0:D3}" -f $i
    $archiveName = "Lennits_Cryotolyzer archives.zip.$num"
    if (Test-Path $archiveName) {
        $archives += $archiveName
        Write-Log "Found: $archiveName" -Type Success
    }
}

if ($archives.Count -eq 0) {
    Write-Log "No split archives found!" -Type Error
    Write-Log "Expected: Lennits_Cryotolyzer archives.zip.001, .002, etc." -Type Info
    Write-Log "Current directory: $(Get-Location)" -Type Info
    exit 1
}

Write-Host ""
Write-Log "Found $($archives.Count) archive parts. Starting extraction..." -Type Info
Write-Host ""

# Step 3: Extract archives
Write-Log "Extracting archives..."

if ($7z) {
    & $7z x $archives[0] -aoa
} else {
    Expand-Archive -Path $archives[0] -DestinationPath . -Force
}

Write-Log "Extraction complete!" -Type Success

# Step 4: Create output directory structure
Write-Log "Organizing files by type..."

$subdirs = @('python', 'typescript', 'kotlin', 'rust', 'config', 'docs', 'assets', 'web', 'android', 'other')

if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
}

foreach ($subdir in $subdirs) {
    $path = Join-Path $OutputDir $subdir
    if (-not (Test-Path $path)) {
        New-Item -ItemType Directory -Path $path -Force | Out-Null
    }
}

# Step 5: Organize files
Write-Log "Moving files to organized directories..."

$fileMap = @{
    'python' = @('*.py')
    'typescript' = @('*.ts', '*.tsx', '*.js', '*.jsx')
    'kotlin' = @('*.kt')
    'rust' = @('*.rs')
    'config' = @('*.json', '*.yaml', '*.yml', '*.toml', '*.gradle', '*.properties')
    'docs' = @('*.md', '*.txt', '*.html', '*.css')
    'assets' = @('*.png', '*.jpg', '*.jpeg', '*.svg', '*.gif')
}

$totalFiles = 0

foreach ($category in $fileMap.Keys) {
    $patterns = $fileMap[$category]
    foreach ($pattern in $patterns) {
        $files = Get-ChildItem -Path . -Filter $pattern -Recurse -ErrorAction SilentlyContinue
        foreach ($file in $files) {
            $destPath = Join-Path $OutputDir $category
            Copy-Item -Path $file.FullName -Destination $destPath -Force -ErrorAction SilentlyContinue | Out-Null
            $totalFiles++
        }
    }
}

Write-Log "Files organized successfully" -Type Success

# Step 6: Count files
Write-Host ""
Write-Log "File summary:" -Type Info
Write-Host ""

foreach ($subdir in $subdirs) {
    $path = Join-Path $OutputDir $subdir
    $count = @(Get-ChildItem -Path $path -Recurse -File -ErrorAction SilentlyContinue).Count
    if ($count -gt 0) {
        Write-Host ("  {0,-15} {1,3} files" -f "${subdir}:", $count)
    }
}

Write-Host ""
Write-Log "Total files organized: $totalFiles" -Type Success

# Step 7: Git operations
Write-Host ""
Write-Log "Preparing git operations..." -Type Info

# Check if branch exists
$branchExists = & git branch | Select-String $BranchName

if (-not $branchExists) {
    Write-Log "Creating new branch: $BranchName" -Type Info
    & git checkout -b $BranchName
} else {
    Write-Log "Branch $BranchName already exists. Switching to it..." -Type Warning
    & git checkout $BranchName
}

# Add files
Write-Log "Adding files to git..." -Type Info
& git add "$OutputDir/"

# Commit
$commitMsg = @"
Extract: All source files organized and ready for processing

- Extracted split archives ($($archives.Count) parts)
- Organized by file type (python, typescript, kotlin, rust, etc.)
- Total files: $totalFiles
- Ready for Copilot platform reconstruction
"@

Write-Log "Creating commit..." -Type Info
$commitOutput = & git commit -m $commitMsg 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Log "Commit created" -Type Success
    
    # Push
    Write-Log "Pushing to GitHub..." -Type Info
    $pushOutput = & git push -u origin $BranchName 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Log "Successfully pushed to GitHub!" -Type Success
    } else {
        Write-Log "Push failed. Trying alternative method..." -Type Warning
        & git push origin $BranchName --force 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Log "Push failed completely" -Type Error
            Write-Log "Please try manually: git push -u origin $BranchName" -Type Info
            exit 1
        }
    }
} else {
    Write-Log "Nothing to commit (branch up to date)" -Type Warning
}

# Step 8: Success summary
Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║                     [✓] EXTRACTION COMPLETE                            ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""

$repoUrl = & git config --get remote.origin.url | ForEach-Object { $_ -replace '\.git$', '' }

Write-Host "📦 Extracted: $totalFiles files"
Write-Host "📁 Location: $OutputDir\"
Write-Host "🔗 Branch: $BranchName"
Write-Host ""
Write-Host "View your files:"
Write-Host "  $repoUrl/tree/$BranchName" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:"
Write-Host "  1. Tell Copilot: ""Branch '$BranchName' is ready for processing"""
Write-Host "  2. Copilot will analyze all files and build the elite platform"
Write-Host "  3. Expected time: 3-4 hours for complete reconstruction"
Write-Host ""

Read-Host "Press Enter to close"
