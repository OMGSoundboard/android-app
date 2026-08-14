# Local helper: run Gradle with the JDK Android Studio downloaded.
# Usage: .\run-gradle.ps1 --version
#        .\run-gradle.ps1 assembleFossDebug

$JdkHome = "C:\Users\MMalmlu\.jdks\jbr-21.0.11"

if (-not (Test-Path "$JdkHome\bin\java.exe")) {
    Write-Error "Java not found at $JdkHome. In Android Studio: Settings -> Build Tools -> Gradle -> Download JDK (version 17 or 21)."
    exit 1
}

$env:JAVA_HOME = $JdkHome
$env:Path = "$JdkHome\bin;" + $env:Path

Set-Location $PSScriptRoot
& .\gradlew.bat @args
