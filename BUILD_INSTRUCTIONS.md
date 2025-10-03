# How to Build "Ungli Meri" APK

## Prerequisites
1. **Download and Install Android Studio**: https://developer.android.com/studio
2. **Install Java JDK 17 or higher**
3. **Enable Developer Options** on your Android device

## Step-by-Step Build Instructions

### Method 1: Using Android Studio (Easiest)

1. **Open Android Studio**
2. **Click "Open an Existing Project"**
3. **Navigate to and select the `android` folder** (not the root folder)
4. **Wait for Gradle sync to complete** (this may take several minutes)
5. **Once sync is complete, you'll see:**
   - Build menu in the top menu bar
   - Gradle panel on the right side
   - Project structure in the left panel

6. **To build APK:**
   - Go to **Build → Build Bundle(s) / APK(s) → Build APK(s)**
   - Or use the Gradle panel: **app → Tasks → build → assembleDebug**

7. **APK Location:**
   - The APK will be created at: `android/app/build/outputs/apk/debug/app-debug.apk`

### Method 2: Using Command Line

1. **Open Command Prompt/PowerShell**
2. **Navigate to the android folder:**
   ```cmd
   cd android
   ```

3. **Run the build command:**
   ```cmd
   gradlew.bat assembleDebug
   ```

4. **APK will be created at:**
   ```
   android\app\build\outputs\apk\debug\app-debug.apk
   ```

## Troubleshooting

### If Gradle is not recognized:
1. Make sure you're in the `android` folder
2. Use `.\gradlew.bat` instead of `gradlew`
3. Or use the full path: `android\gradlew.bat assembleDebug`

### If Android Studio doesn't recognize the project:
1. Make sure you opened the `android` folder, not the root folder
2. Look for the `build.gradle` file in the android folder
3. If prompted, choose "Import Gradle Project"

### If build fails:
1. Check that Android SDK is installed
2. Accept any license agreements in Android Studio
3. Update Android SDK if prompted

## Server Setup (Run this first)

1. **Install Node.js dependencies:**
   ```cmd
   npm install
   ```

2. **Start the server:**
   ```cmd
   npm start
   ```

## Testing the App

1. **Install the APK** on your Android device
2. **Make sure your device is connected to the same network** as the server
3. **Enable fingerprint/biometric authentication and face unlock** on your device
4. **Open "Ungli Meri" app**
5. **Register with a username and fingerprint**
6. **Optionally register face authentication**
7. **Test login with either fingerprint or face authentication***

## App Features

- **First time:** Register username + fingerprint (required)
- **Face Registration:** Optionally add face authentication
- **Dual Login Options:** Choose between fingerprint or face login
- **Secure Storage:** Both biometric templates encrypted and stored securely
- **Success message:** "Welcome [username]!"
- **Data storage:** All data saved to MongoDB on server

## Network Configuration

If you need to change this:
1. Edit `android/app/src/main/java/com/example/fingerprintauth/ApiService.kt`
2. Change the `BASE_URL` constant
3. Rebuild the APK