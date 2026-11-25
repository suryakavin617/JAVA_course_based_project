@echo off
REM Compile PolarLog (needs JDK 8 or above)
if not exist out mkdir out
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
if %errorlevel% neq 0 (
  echo Compilation failed.
) else (
  echo Compiled successfully. Run run.bat to start.
)
