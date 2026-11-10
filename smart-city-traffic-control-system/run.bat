@echo off
setlocal
cd /d "%~dp0"
title Smart City Traffic Control System

if not exist bin\com\smartcity\traffic\Main.class (
    call build.bat
    if errorlevel 1 (
        echo.
        echo [ERROR] Build failed. The application was not started.
        pause
        exit /b 1
    )
)

echo Starting Smart City Traffic Control System...
java -cp bin com.smartcity.traffic.Main
set APP_EXIT=%ERRORLEVEL%
echo.
if not "%APP_EXIT%"=="0" echo [WARNING] The application exited with code %APP_EXIT%.
pause
exit /b %APP_EXIT%
