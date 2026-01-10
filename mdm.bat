@echo off
set JAVACMD="C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot\bin\java.exe"
if not exist %JAVACMD% set JAVACMD=java

%JAVACMD% -jar "%~dp0target\mdm-cli-1.0-SNAPSHOT.jar" %*
