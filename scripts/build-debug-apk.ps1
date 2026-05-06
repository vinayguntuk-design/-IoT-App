param(
    [string]$ProjectRoot = (Resolve-Path "$PSScriptRoot\..").Path
)

$ErrorActionPreference = "Stop"
Set-Location $ProjectRoot

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    throw "Java is not available. Install Android Studio, then restart PowerShell so its bundled JDK/Gradle can be used."
}

if (Test-Path ".\gradlew.bat") {
    .\gradlew.bat assembleDebug
} elseif (Get-Command gradle -ErrorAction SilentlyContinue) {
    gradle assembleDebug
} else {
    throw "Gradle is not available. Open this project in Android Studio once, or add a Gradle wrapper."
}

$apk = Join-Path $ProjectRoot "app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apk) {
    Write-Host "APK ready: $apk"
} else {
    throw "Build completed but APK was not found at $apk"
}
