@echo off
REM Setup script for Agriwise Application with Gemini API

echo.
echo ====================================
echo  Agriwise Application Startup
echo ====================================
echo.

REM Check if .env file exists
if not exist ".env" (
    echo ERROR: .env file not found!
    echo Please create .env file with: GEMINI_API_KEY=your_api_key_here
    echo.
    echo You can use: copy .env.example .env
    echo Then edit .env and add your Gemini API key.
    pause
    exit /b 1
)

REM Load .env file
for /f "tokens=*" %%a in (.env) do (
    set "%%a"
)

REM Check if GEMINI_API_KEY is set
if "!GEMINI_API_KEY!"=="" (
    echo ERROR: GEMINI_API_KEY not set in .env file!
    pause
    exit /b 1
)

echo Starting Agriwise Application...
echo API Key: !GEMINI_API_KEY:~0,10!...
echo.

REM Run the application
java -jar target/agriwise-project-1.0-SNAPSHOT.jar

pause
