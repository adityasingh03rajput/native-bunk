@echo off
echo Building Ungli Meri APK...
echo.

cd android

echo Checking Gradle wrapper...
if not exist gradlew.bat (
    echo Creating Gradle wrapper...
    gradle wrapper
)

echo.
echo Building debug APK...
gradlew.bat assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ BUILD SUCCESSFUL!
    echo.
    echo APK created at: android\app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo Copying APK to root folder...
    copy "app\build\outputs\apk\debug\app-debug.apk" "..\Ungli-Meri-Debug.apk"
    echo.
    echo ✅ APK copied to: Ungli-Meri-Debug.apk
    echo.
    echo You can now install this APK on your Android device!
) else (
    echo.
    echo ❌ BUILD FAILED!
    echo Please check the error messages above.
    echo.
    echo Common solutions:
    echo 1. Make sure Android SDK is installed
    echo 2. Open the project in Android Studio first
    echo 3. Accept any license agreements
)

echo.
pause