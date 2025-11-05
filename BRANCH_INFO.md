# 🔖 Production Localhost Savepoint Branch

## Branch Purpose

This branch serves as a **production-ready savepoint** of the application configured for **localhost/local network deployment**.

---

## 📍 What This Branch Contains

### Configuration
- **Server URL**: `http://192.168.9.31:3000` (Local network IP)
- **Environment**: Development/Local Network
- **Database**: MongoDB (Local or Atlas)
- **Deployment**: Manual (localhost server)

### Features Included
✅ Complete teacher interface with bottom navigation  
✅ Student attendance tracking with face verification  
✅ Real-time attendance monitoring via Socket.IO  
✅ Timetable management with edit capability  
✅ Server time synchronization (anti-cheat)  
✅ Admin panel for user management  
✅ Comprehensive documentation  

---

## 🎯 Use Cases

### 1. **Restore Point**
If deployment to Render or other cloud services causes issues, you can restore to this branch:
```bash
git checkout production-localhost-savepoint
```

### 2. **Local Development**
Use this branch for local testing and development:
```bash
git checkout production-localhost-savepoint
npm install
node server/index.js
```

### 3. **Reference Configuration**
Compare localhost vs cloud configurations:
```bash
git diff production-localhost-savepoint main
```

---

## 🔄 Branch Workflow

```
main (development)
  ↓
production-localhost-savepoint (this branch - localhost config)
  ↓
production-render (future - cloud deployment)
```

---

## 📦 What's Next

From this savepoint, the next steps are:

1. **Create `production-render` branch** from this branch
2. **Update configurations** for Render deployment:
   - Change IP addresses to environment variables
   - Update CORS settings
   - Configure MongoDB Atlas
   - Set up environment variables
3. **Deploy to Render**
4. **Keep this branch** as localhost backup

---

## 🚀 Quick Start (Localhost)

### Prerequisites
- Node.js installed
- MongoDB running (local or Atlas)
- Android Studio (for APK build)
- Same WiFi network for phone and PC

### Setup Steps

1. **Clone and checkout this branch**:
```bash
git clone https://github.com/adityasingh03rajput/native-bunk.git
cd native-bunk
git checkout production-localhost-savepoint
```

2. **Install dependencies**:
```bash
npm install
```

3. **Update IP address** (if needed):
   - Find your IP: `ipconfig` (Windows) or `ifconfig` (Mac/Linux)
   - Update in these files:
     - `App.js` (lines 22-23)
     - `admin-panel/renderer.js` (line 5)
     - `server/index.js` (line 1105)
     - `FaceVerificationScreen.js` (line 71)
     - `OfflineFaceVerification.js` (line 10)

4. **Configure firewall** (Windows):
```powershell
# Run as Administrator
New-NetFirewallRule -DisplayName "Node.js Server" -Direction Inbound -Program "C:\Program Files\nodejs\node.exe" -Action Allow
New-NetFirewallRule -DisplayName "Port 3000" -Direction Inbound -LocalPort 3000 -Protocol TCP -Action Allow
```

5. **Start server**:
```bash
node server/index.js
```

6. **Seed database** (first time only):
```bash
node server/seed-data.js
```

7. **Build APK**:
```bash
cd android
.\gradlew.bat assembleRelease
```

8. **Install APK**:
```bash
adb install -r android\app\build\outputs\apk\release\app-release.apk
```

---

## 📁 Key Files & Configurations

### Server Configuration
- **Entry Point**: `server/index.js`
- **Port**: 3000
- **Database**: MongoDB (connection string in code)
- **CORS**: Enabled for all origins (development)

### Mobile App Configuration
- **API URL**: `http://192.168.9.31:3000/api/config`
- **Socket URL**: `http://192.168.9.31:3000`
- **Platform**: Android (React Native + Expo)

### Admin Panel Configuration
- **Server URL**: `http://192.168.9.31:3000`
- **Platform**: Electron (Desktop app)

---

## 🔐 Default Credentials

### Students
- **Enrollment**: `2024001`, `2024002`, `2024003`
- **Password**: `password123`

### Teachers
- **Employee ID**: `T001`, `T002`
- **Password**: `teacher123`

See `LOGIN_CREDENTIALS.md` for complete list.

---

## 📚 Documentation Files

- `README.md` - Main project documentation
- `DEPLOYMENT_GUIDE.md` - Deployment instructions
- `TEACHER_FEATURES.md` - Teacher interface guide
- `NETWORK_TROUBLESHOOTING.md` - Connection issues guide
- `LOGIN_CREDENTIALS.md` - Login credentials
- `SECURITY_AUDIT_TIME_MANIPULATION.md` - Security features

---

## ⚠️ Important Notes

### DO NOT Modify This Branch
This branch is a **savepoint**. Do not make changes here. Instead:
1. Create a new branch from this one
2. Make your changes
3. Merge back to `main` if needed

### Localhost Limitations
- ❌ Only works on local network
- ❌ Requires manual IP configuration
- ❌ Not accessible from internet
- ❌ Requires firewall configuration

### For Production Deployment
Use the `production-render` branch (to be created) which will have:
- ✅ Environment variables for configuration
- ✅ Cloud database (MongoDB Atlas)
- ✅ HTTPS support
- ✅ Proper CORS configuration
- ✅ Production-ready security

---

## 🔧 Troubleshooting

### Can't Connect to Server
1. Check firewall settings
2. Verify IP address is correct
3. Ensure same WiFi network
4. See `NETWORK_TROUBLESHOOTING.md`

### Server Won't Start
1. Check if port 3000 is in use
2. Verify MongoDB is running
3. Check Node.js version (v14+ required)

### APK Build Fails
1. Clean build: `.\gradlew.bat clean`
2. Check Android SDK is installed
3. Verify Java JDK is configured

---

## 📊 Branch Statistics

- **Created**: October 30, 2024
- **Purpose**: Localhost production savepoint
- **Status**: Stable ✅
- **Last Tested**: October 30, 2024
- **Total Files**: 100+
- **Total Lines**: 15,000+

---

## 🔗 Related Branches

- **main**: Active development branch
- **production-localhost-savepoint**: This branch (localhost config)
- **production-render**: Future branch (cloud deployment)

---

## 📞 Support

For issues or questions:
1. Check documentation files
2. Review `NETWORK_TROUBLESHOOTING.md`
3. Check GitHub issues
4. Contact repository maintainer

---

## ✅ Verification Checklist

Before using this branch, verify:
- [ ] Node.js installed (v14+)
- [ ] MongoDB accessible
- [ ] Android Studio installed (for APK build)
- [ ] Firewall configured
- [ ] IP address updated (if different network)
- [ ] Dependencies installed (`npm install`)
- [ ] Server starts successfully
- [ ] APK builds without errors
- [ ] App connects to server

---

**Branch**: `production-localhost-savepoint`  
**Repository**: https://github.com/adityasingh03rajput/native-bunk  
**Last Updated**: October 30, 2024  
**Version**: 1.0.0-localhost
