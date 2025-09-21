@echo off
echo Starting BlueprintHell Offline Mode...
echo.

REM Check if offline JAR exists
if not exist "build\libs\BlueprintHell-5-1.0-SNAPSHOT.jar" (
    echo Offline JAR not found. Building project...
    call gradlew offlineJar
    if errorlevel 1 (
        echo Build failed!
        pause
        exit /b 1
    )
)

echo Starting offline game...
java -jar build\libs\BlueprintHell-5-1.0-SNAPSHOT.jar

pause
