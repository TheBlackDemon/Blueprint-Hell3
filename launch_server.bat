@echo off
echo Starting BlueprintHell Server...
java -cp "build/libs/*" server.ServerMain %*
pause
