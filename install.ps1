$ErrorActionPreference = "Stop"

Write-Host "Installing MDM-CLI (Native)..." -ForegroundColor Cyan

# 1. Define install location
$InstallDir = Join-Path $HOME ".mdm"
$NativeDir = Join-Path $InstallDir "native"
$BinDir = Join-Path $InstallDir "bin"

# 2. Check for Native Build
$NativeSource = ".\dist\mdm"
if (-not (Test-Path $NativeSource)) {
    Write-Host "Error: Native build 'dist/mdm' not found. Run 'mvn package -Pnative' first." -ForegroundColor Red
    exit 1
}

# 3. Create directories
if (-not (Test-Path $BinDir)) { New-Item -ItemType Directory -Force -Path $BinDir | Out-Null }
if (Test-Path $NativeDir) { Remove-Item -Recurse -Force $NativeDir } # Clean Clean

# 4. Copy Native App
Write-Host "Copying Native Runtime..." -ForegroundColor Yellow
Copy-Item -Recurse -Path $NativeSource -Destination $NativeDir
Write-Host "Copied to $NativeDir" -ForegroundColor Green

# 5. Create Shim (mdm.cmd) pointing to the EXE
$ShimPath = Join-Path $BinDir "mdm.cmd"
$ExePath = Join-Path $NativeDir "mdm.exe"

$ShimContent = "@echo off`r`n`"$ExePath`" %*"
Set-Content -Path $ShimPath -Value $ShimContent
Write-Host "Created Shim at $ShimPath" -ForegroundColor Green

# 6. Add to PATH (User Scope)
$UserPath = [Environment]::GetEnvironmentVariable("Path", "User")
if ($UserPath -notlike "*$BinDir*") {
    Write-Host "Adding $BinDir to User PATH..." -ForegroundColor Yellow
    [Environment]::SetEnvironmentVariable("Path", "$UserPath;$BinDir", "User")
    Write-Host "Success! Restart your terminal to use 'mdm' globally." -ForegroundColor Green
} else {
    Write-Host "PATH already configured." -ForegroundColor Green
}

Write-Host "`nMDM Installed Successfully (Native Mode)! Try running 'mdm --version'" -ForegroundColor Cyan
