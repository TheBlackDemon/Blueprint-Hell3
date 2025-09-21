@echo off
echo Starting BlueprintHell Multiplayer Server...
echo.
echo Server will start on port 8888
echo Press Ctrl+C to stop the server
echo.

cd /d "%~dp0"

java -cp "build/classes/java/main;lib/*" main.java.server.ServerMain

pause
