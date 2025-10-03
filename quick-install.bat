@echo off
echo Installing Ungli Meri on connected device...
echo.

if not exist "Ungli-Meri-Debug.apk" (
    echo APK not found. Building first...
    call build-apk.bat
    if %ERRORLEVEL% NEQ 0 (
        echo Build failed. Please check errors above.
        pause
        exit /b 1
    )
)

echo Checking device connection...
adb devices

echo.
echo Installing APK...
adb install -r "Ungli-Meri-Debug.apk"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Installation successful!
    echo.
    echo Launching app...
    adb shell am start -n com.example.fingerprintauth/.MainActivity
    echo.
    echo ✅ App launched! Check your phone.
) else (
    echo.
    echo ❌ Installation failed!
    echo.
    echo Common solutions:
    echo 1. Enable USB debugging on your phone
    echo 2. Allow installation from unknown sources
    echo 3. Accept the USB debugging prompt on your phone
    echo 4. Try disconnecting and reconnecting USB cable
)

echo.
pause