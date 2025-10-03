@echo off
echo ========================================
echo    Ungli Meri - Install and Run
echo ========================================
echo.

echo Step 1: Starting the server...
echo.
start "Ungli Meri Server" cmd /k "echo Starting server on http://192.168.246.31:3000 && npm install && npm start"

echo Waiting for server to start...
timeout /t 5 /nobreak >nul

echo.
echo Step 2: Installing APK on connected device...
echo.

echo Checking if device is connected...
adb devices

echo.
echo Installing Ungli Meri APK...
if exist "Ungli-Meri-Debug.apk" (
    adb install -r "Ungli-Meri-Debug.apk"
    if %ERRORLEVEL% EQU 0 (
        echo.
        echo ✅ APK installed successfully!
        echo.
        echo Step 3: Launching the app...
        adb shell am start -n com.example.fingerprintauth/.MainActivity
        echo.
        echo ✅ App launched on your device!
        echo.
        echo 📱 Your phone should now show "Ungli Meri" app
        echo 🖥️  Server is running in the background
        echo.
        echo Instructions:
        echo 1. On your phone, register with a username and fingerprint
        echo 2. Test login with the same username and fingerprint
        echo 3. You should see "Welcome [username]!" message
        echo.
        echo Press any key to close this window...
        pause >nul
    ) else (
        echo.
        echo ❌ Failed to install APK
        echo.
        echo Troubleshooting:
        echo 1. Make sure USB debugging is enabled
        echo 2. Allow installation from unknown sources
        echo 3. Check if device is properly connected
        echo.
        pause
    )
) else (
    echo.
    echo ❌ APK file not found!
    echo Building APK first...
    call build-apk.bat
    echo.
    echo Now run this script again to install.
    pause
)

echo.
echo Server will continue running in the background.
echo Close the server window when you're done testing.