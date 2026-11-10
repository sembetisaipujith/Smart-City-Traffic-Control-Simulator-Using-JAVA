@echo off
setlocal
cd /d "%~dp0"
title Smart City Traffic Control System - Tests

call build.bat
if errorlevel 1 exit /b 1

if not exist test-bin mkdir test-bin
javac -encoding UTF-8 -cp bin -d test-bin tests\com\smartcity\traffic\SmokeTests.java
if errorlevel 1 (
    echo [ERROR] Test compilation failed.
    exit /b 1
)

java -ea -Djava.awt.headless=true -cp bin;test-bin com.smartcity.traffic.SmokeTests
if errorlevel 1 (
    echo [ERROR] One or more smoke tests failed.
    exit /b 1
)

echo [SUCCESS] All smoke tests passed.
