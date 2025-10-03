# Biometric Authentication System

A complete biometric authentication system with fingerprint and face recognition, featuring Node.js backend and Android app using Kotlin.

## Features

- **Fingerprint Registration**: Users can register their fingerprint with a username
- **Face Registration**: Users can register their face for authentication
- **Dual Biometric Authentication**: Support for both fingerprint and face verification
- **Secure Authentication**: Advanced biometric verification for login
- **Server Integration**: Data stored securely on MongoDB server
- **Real-time Verification**: Instant biometric matching and welcome messages
- **Flexible Authentication**: Choose between fingerprint or face login

## Project Structure

```
├── server.js              # Node.js Express server
├── package.json           # Server dependencies
├── android/               # Android app (Kotlin)
│   ├── app/
│   │   ├── build.gradle
│   │   └── src/main/
│   │       ├── AndroidManifest.xml
│   │       ├── java/com/example/fingerprintauth/
│   │       │   ├── MainActivity.kt
│   │       │   ├── RegisterActivity.kt
│   │       │   ├── LoginActivity.kt
│   │       │   ├── FaceRegisterActivity.kt
│   │       │   ├── FaceLoginActivity.kt
│   │       │   ├── ApiService.kt
│   │       │   └── SharedPrefsManager.kt
│   │       └── res/
│   │           ├── layout/
│   │           ├── values/
│   │           └── drawable/
│   ├── build.gradle
│   └── settings.gradle
└── README.md
```

## Setup Instructions

### Server Setup

1. **Install Dependencies**:
   ```bash
   npm install
   ```

2. **Start MongoDB**:
   - Install MongoDB locally or use MongoDB Atlas
   - Default connection: `mongodb://localhost:27017/fingerprint_auth`

3. **Run Server**:
   ```bash
   npm start
   # or for development
   npm run dev
   ```

4. **Server will run on**: `http://localhost:3000`

### Android App Setup

1. **Open in Android Studio**:
   - Open the `android` folder in Android Studio
   - Wait for Gradle sync to complete

2. **Update Server URL**:
   - In `ApiService.kt`, update the `BASE_URL`:
   - For emulator: `http://10.0.2.2:3000/`
   - For real device: `http://YOUR_IP_ADDRESS:3000/`

3. **Build and Run**:
   - Connect your Android device or start an emulator
   - Ensure device has fingerprint sensor and enrolled fingerprints
   - Build and install the APK

## How It Works

### Registration Flow
1. User opens app for the first time
2. Enters username
3. Taps "Register Fingerprint"
4. Places finger on sensor (follows system prompts for all sides)
5. Fingerprint data is encrypted and sent to server
6. Server stores username and hashed fingerprint in MongoDB
7. Optionally register face by tapping "Register Face"
8. Face data is captured and securely stored

### Login Flow
**Fingerprint Login:**
1. User enters registered username
2. Taps "Verify Fingerprint"
3. Places registered finger on sensor
4. App sends fingerprint data to server for verification
5. Server compares with stored data
6. On success: Shows "Welcome [username]!" message

**Face Login:**
1. User enters registered username
2. Taps "Verify Face"
3. Looks at the front camera for face recognition
4. App sends face data to server for verification
5. Server compares with stored face data
6. On success: Shows "Welcome [username]!" message

## API Endpoints

### POST /api/register
Register a new user with fingerprint data.

**Request**:
```json
{
  "username": "john_doe",
  "fingerprintData": "encrypted_fingerprint_hash"
}
```

**Response**:
```json
{
  "success": true,
  "message": "User registered successfully",
  "userId": "user_id"
}
```

### POST /api/verify
Verify user fingerprint for authentication.

**Request**:
```json
{
  "username": "john_doe",
  "fingerprintData": "encrypted_fingerprint_hash"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Welcome john_doe!",
  "username": "john_doe"
}
```

### POST /api/register-face
Register face data for an existing user.

**Request**:
```json
{
  "username": "john_doe",
  "faceData": "encrypted_face_hash"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Face data registered successfully",
  "userId": "user_id"
}
```

### POST /api/verify-face
Verify user face for authentication.

**Request**:
```json
{
  "username": "john_doe",
  "faceData": "encrypted_face_hash"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Welcome john_doe!",
  "username": "john_doe"
}
```

## Security Features

- **Encrypted Storage**: Both fingerprint and face data are hashed using bcrypt
- **Secure Transmission**: HTTPS recommended for production
- **Local Storage**: Minimal data stored locally (username and biometric templates)
- **Biometric Hardware**: Uses Android's BiometricPrompt API for both fingerprint and face
- **Dual Authentication**: Support for multiple biometric modalities

## Requirements

### Server
- Node.js 14+
- MongoDB 4.4+
- npm or yarn

### Android App
- Android 6.0+ (API level 23)
- Device with fingerprint sensor and/or front-facing camera
- Enrolled fingerprints and/or face unlock in device settings
- BiometricPrompt API support

## Production Considerations

1. **HTTPS**: Use SSL certificates for secure communication
2. **Database Security**: Implement proper MongoDB security
3. **Rate Limiting**: Add API rate limiting
4. **Error Handling**: Enhanced error handling and logging
5. **Backup**: Regular database backups
6. **Monitoring**: Server monitoring and alerts

## Troubleshooting

### Common Issues

1. **Network Connection**: Ensure server is running and accessible
2. **Biometric Hardware**: Check device has working fingerprint sensor and/or front camera
3. **Enrolled Biometrics**: Ensure fingerprints and/or face unlock are enrolled in device settings
4. **Permissions**: App has biometric and camera permissions
5. **MongoDB**: Database is running and accessible
6. **Face Recognition**: Ensure good lighting conditions for face authentication
7. **API Compatibility**: Verify device supports BiometricPrompt API

### Testing

1. **Server Testing**: Use Postman to test all API endpoints (/api/register, /api/verify, /api/register-face, /api/verify-face)
2. **App Testing**: Test on real device with both fingerprint and face capabilities
3. **Database**: Check MongoDB for stored user data with both biometric types
4. **Biometric Testing**: Test both authentication methods separately and together

## License

MIT License - Feel free to use and modify as needed.