@echo off
echo Starting BlueprintHell Server...
echo.

REM Check if server JAR exists
if not exist "build\libs\server.jar" (
    echo Server JAR not found. Building project...
    call gradlew serverJar
    if errorlevel 1 (
        echo Build failed!
        pause
        exit /b 1
    )
)

echo Starting server...
java -jar build\libs\server.jar

pause
