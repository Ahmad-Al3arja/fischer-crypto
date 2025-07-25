@echo off
echo 🚀 Starting Quick API Test...
echo.
powershell -ExecutionPolicy Bypass -File "test-all-endpoints.ps1"
echo.
echo ✅ Test completed! Press any key to exit...
pause >nul 