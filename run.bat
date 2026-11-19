@echo off
cd /d "%~dp0"
java -javaagent:paperclip.jar -jar paperclip.jar %*
exit /b %errorlevel%
