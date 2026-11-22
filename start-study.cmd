@echo off
setlocal
pushd "%~dp0" || (
  echo [ERROR] Failed to enter script directory.
  pause
  exit /b 1
)
where node >nul 2>nul
if errorlevel 1 (
  echo [ERROR] Node.js not found in PATH. Please install Node.js 18+.
  pause
  popd
  exit /b 1
)
node "study.mjs" %*
set "EXIT_CODE=%ERRORLEVEL%"
if not "%EXIT_CODE%"=="0" (
  echo.
  echo [ERROR] study.mjs exited with code %EXIT_CODE%.
  pause
)
popd
exit /b %EXIT_CODE%
