@echo off
echo Building Order Management Service...
call mvn spotless:apply clean install
if %errorlevel% neq 0 (
    echo Build Failed!
    exit /b %errorlevel%
)
echo Build Successful!
pause