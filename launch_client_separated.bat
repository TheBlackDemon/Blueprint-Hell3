@echo off
echo Starting BlueprintHell Client...
echo.

REM Check if client JAR exists
if not exist "build\libs\client.jar" (
    echo Client JAR not found. Building project...
    call gradlew clientJar
    if errorlevel 1 (
        echo Build failed!
        pause
        exit /b 1
    )
)

echo Starting client...
java -jar build\libs\client.jar

pause
