@echo off
echo ========================================
echo    USB Debugging Setup Helper
echo ========================================
echo.

echo Checking ADB (Android Debug Bridge)...
adb version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ ADB not found!
    echo.
    echo Please install Android SDK Platform Tools:
    echo 1. Download from: https://developer.android.com/studio/releases/platform-tools
    echo 2. Extract to a folder
    echo 3. Add the folder to your PATH environment variable
    echo 4. Or install Android Studio which includes ADB
    echo.
    pause
    exit /b 1
)

echo ✅ ADB found!
echo.

echo Checking connected devices...
echo.
adb devices
echo.

echo Device Setup Instructions:
echo.
echo 📱 On your Android phone:
echo 1. Go to Settings → About Phone
echo 2. Tap "Build Number" 7 times to enable Developer Options
echo 3. Go to Settings → Developer Options
echo 4. Enable "USB Debugging"
echo 5. Enable "Install via USB" (if available)
echo 6. Connect phone to computer via USB
echo 7. When prompted, allow USB debugging from this computer
echo.
echo 🔒 Security Settings:
echo 1. Go to Settings → Security
echo 2. Enable "Unknown Sources" or "Install Unknown Apps"
echo 3. Allow installation from your file manager/browser
echo.
echo 👆 Fingerprint Setup:
echo 1. Go to Settings → Security → Fingerprint
echo 2. Add at least one fingerprint
echo 3. Make sure fingerprint unlock is enabled
echo.

echo Press any key when your device is ready...
pause >nul

echo.
echo Testing connection...
adb devices

echo.
echo If you see your device listed above, you're ready!
echo Run 'install-and-run.bat' to install and launch the app.
echo.
pause