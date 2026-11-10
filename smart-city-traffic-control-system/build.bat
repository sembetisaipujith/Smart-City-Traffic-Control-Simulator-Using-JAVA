@echo off
setlocal
cd /d "%~dp0"
title Smart City Traffic Control System - Build

echo ========================================================
echo   SMART CITY TRAFFIC CONTROL SYSTEM - BUILD
echo ========================================================
echo.

where javac >nul 2>nul
if errorlevel 1 (
    echo [ERROR] javac was not found. Install JDK 17 or newer and add its bin folder to PATH.
    exit /b 1
)

if not exist bin mkdir bin
if exist bin\com rmdir /s /q bin\com

echo Compiling Java source files...
javac -encoding UTF-8 -d bin src\com\smartcity\traffic\*.java
if errorlevel 1 (
    echo.
    echo [ERROR] Compilation failed. See the messages above.
    exit /b 1
)

if not exist bin\com\smartcity\traffic\Main.class (
    echo [ERROR] Main.class was not generated.
    exit /b 1
)

echo.
echo [SUCCESS] All Java source files compiled successfully.
echo Run run.bat to launch the application.
exit /b 0
