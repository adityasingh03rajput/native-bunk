const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const bcrypt = require('bcrypt');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json({ limit: '10mb' }));

mongoose.connect('mongodb://localhost:27017/fingerprint_auth', {
  useNewUrlParser: true,
  useUnifiedTopology: true
});

// User Schema
const userSchema = new mongoose.Schema({
  username: {
    type: String,
    required: true,
    unique: true
  },
  fingerprintData: {
    type: String,
    required: false
  },
  faceData: {
    type: String,
    required: false
  },
  createdAt: {
    type: Date,
    default: Date.now
  }
});

const User = mongoose.model('User', userSchema);

// Utility functions
function isValidBase64(str) {
  try {
    // More lenient Base64 validation for image data
    if (!str || typeof str !== 'string') return false;
    
    // Remove whitespace and check basic format
    const cleanStr = str.replace(/\s/g, '');
    
    // Basic Base64 pattern check
    const base64Pattern = /^[A-Za-z0-9+/]*={0,2}$/;
    if (!base64Pattern.test(cleanStr)) return false;
    
    // Length should be multiple of 4 (after padding)
    if (cleanStr.length % 4 !== 0) return false;
    
    // Try to decode - if it works, it's valid
    atob(cleanStr);
    return true;
  } catch (err) {
    return false;
  }
}

// Routes
// Register new user with fingerprint
app.post('/api/register', async (req, res) => {
  try {
    const { username, fingerprintData } = req.body;
    
    // Enhanced validation
    if (!username || !fingerprintData) {
      console.log(`❌ Fingerprint registration failed: Missing data - username: ${!!username}, fingerprintData: ${!!fingerprintData}`);
      return res.status(400).json({ 
        success: false, 
        message: 'Username and fingerprint data are required' 
      });
    }

    // Validate username format
    if (username.length < 3 || username.length > 50) {
      console.log(`❌ Fingerprint registration failed: Invalid username length: ${username}`);
      return res.status(400).json({ 
        success: false, 
        message: 'Username must be between 3 and 50 characters' 
      });
    }

    // Check if user already exists
    const existingUser = await User.findOne({ username });
    if (existingUser) {
      console.log(`❌ Fingerprint registration failed: Username already exists: ${username}`);
      return res.status(400).json({ 
        success: false, 
        message: 'Username already exists. Please choose a different username.' 
      });
    }

    // Hash fingerprint data for security
    const hashedFingerprint = await bcrypt.hash(fingerprintData, 12);

    // Create new user
    const newUser = new User({
      username,
      fingerprintData: hashedFingerprint
    });

    await newUser.save();

    console.log(`✅ Fingerprint registration successful for user: ${username}`);
    res.status(201).json({
      success: true,
      message: 'User registered successfully',
      userId: newUser._id,
      username: newUser.username,
      timestamp: new Date().toISOString()
    });

  } catch (error) {
    console.error('❌ Fingerprint registration error:', error);
    res.status(500).json({ 
      success: false, 
      message: 'Server error during registration' 
    });
  }
});

// Verify fingerprint
app.post('/api/verify', async (req, res) => {
  try {
    const { username, fingerprintData } = req.body;

    // Enhanced validation
    if (!username || !fingerprintData) {
      console.log(`❌ Fingerprint verification failed: Missing data - username: ${!!username}, fingerprintData: ${!!fingerprintData}`);
      return res.status(400).json({ 
        success: false, 
        message: 'Username and fingerprint data are required' 
      });
    }

    // Find user by username
    const user = await User.findOne({ username });
    if (!user) {
      console.log(`❌ Fingerprint verification failed: User not found: ${username}`);
      return res.status(404).json({ 
        success: false, 
        message: 'User not found' 
      });
    }

    if (!user.fingerprintData) {
      console.log(`❌ Fingerprint verification failed: No fingerprint data for user: ${username}`);
      return res.status(404).json({ 
        success: false, 
        message: 'No fingerprint data registered for this user' 
      });
    }

    // Verify fingerprint
    const startTime = Date.now();
    const isMatch = await bcrypt.compare(fingerprintData, user.fingerprintData);
    const verificationTime = Date.now() - startTime;
    
    if (isMatch) {
      console.log(`✅ Fingerprint verification successful for user: ${username} (${verificationTime}ms)`);
      res.json({
        success: true,
        message: `Welcome ${username}!`,
        username: user.username,
        verificationTime: verificationTime,
        timestamp: new Date().toISOString()
      });
    } else {
      console.log(`❌ Fingerprint verification failed: Invalid fingerprint for user: ${username} (${verificationTime}ms)`);
      res.status(401).json({
        success: false,
        message: 'Fingerprint verification failed - fingerprint does not match'
      });
    }

  } catch (error) {
    console.error('❌ Fingerprint verification error:', error);
    res.status(500).json({ 
      success: false, 
      message: 'Server error during verification' 
    });
  }
});

// Register face data
app.post('/api/register-face', async (req, res) => {
  try {
    const { username, faceData } = req.body;
    
    // Enhanced validation
    if (!username || !faceData) {
      console.log(`❌ Face registration failed: Missing data - username: ${!!username}, faceData: ${!!faceData}`);
      return res.status(400).json({ 
        success: false, 
        message: 'Username and face data are required' 
      });
    }

    // Basic face data validation (check if it's not empty and has reasonable length)
    if (faceData.length < 100) {
      console.log(`❌ Face registration failed: Face data too short for user: ${username}`);
      return res.status(400).json({ 
        success: false, 
        message: 'Invalid face data - data too short' 
      });
    }

    // Find user by username
    const user = await User.findOne({ username });
    if (!user) {
      console.log(`❌ Face registration failed: User not found: ${username}`);
      return res.status(404).json({ 
        success: false, 
        message: 'User not found. Please register with fingerprint first.' 
      });
    }

    // Check if face already registered
    if (user.faceData) {
      console.log(`⚠️ Face registration: Updating existing face data for user: ${username}`);
    }

    // Hash face data for security
    const hashedFace = await bcrypt.hash(faceData, 12);
    
    // Update user with face data
    user.faceData = hashedFace;
    await user.save();

    console.log(`✅ Face registration successful for user: ${username}`);
    res.json({
      success: true,
      message: 'Face data registered successfully',
      userId: user._id,
      timestamp: new Date().toISOString()
    });

  } catch (error) {
    console.error('❌ Face registration error:', error);
    res.status(500).json({ 
      success: false, 
      message: 'Server error during face registration' 
    });
  }
});

// Verify face
app.post('/api/verify-face', async (req, res) => {
  try {
    const { username, faceData } = req.body;

    // Enhanced validation
    if (!username || !faceData) {
      console.log(`❌ Face verification failed: Missing data - username: ${!!username}, faceData: ${!!faceData}`);
      return res.status(400).json({ 
        success: false, 
        message: 'Username and face data are required' 
      });
    }

    // Basic face data validation (check if it's not empty and has reasonable length)
    if (faceData.length < 100) {
      console.log(`❌ Face verification failed: Face data too short for user: ${username}`);
      return res.status(400).json({ 
        success: false, 
        message: 'Invalid face data - data too short' 
      });
    }

    // Find user by username
    const user = await User.findOne({ username });
    if (!user) {
      console.log(`❌ Face verification failed: User not found: ${username}`);
      return res.status(404).json({ 
        success: false, 
        message: 'User not found' 
      });
    }

    if (!user.faceData) {
      console.log(`❌ Face verification failed: No face data registered for user: ${username}`);
      return res.status(404).json({ 
        success: false, 
        message: 'No face data registered for this user. Please register face first.' 
      });
    }

    // Verify face
    const startTime = Date.now();
    const isMatch = await bcrypt.compare(faceData, user.faceData);
    const verificationTime = Date.now() - startTime;
    
    if (isMatch) {
      console.log(`✅ Face verification successful for user: ${username} (${verificationTime}ms)`);
      res.json({
        success: true,
        message: `Welcome ${username}!`,
        username: user.username,
        verificationTime: verificationTime,
        timestamp: new Date().toISOString()
      });
    } else {
      console.log(`❌ Face verification failed: Invalid face data for user: ${username} (${verificationTime}ms)`);
      res.status(401).json({
        success: false,
        message: 'Face verification failed - face does not match'
      });
    }

  } catch (error) {
    console.error('❌ Face verification error:', error);
    res.status(500).json({ 
      success: false, 
      message: 'Server error during face verification' 
    });
  }
});

// Get all users (for testing)
app.get('/api/users', async (req, res) => {
  try {
    const users = await User.find({}, { fingerprintData: 0, faceData: 0 }); // Exclude biometric data
    res.json({ success: true, users });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Server error' });
  }
});

// Reset database (delete all users)
app.delete('/api/reset', async (req, res) => {
  try {
    const result = await User.deleteMany({});
    res.json({ 
      success: true, 
      message: `Database reset successfully. Deleted ${result.deletedCount} users.`,
      deletedCount: result.deletedCount
    });
    console.log(`Database reset: Deleted ${result.deletedCount} users`);
  } catch (error) {
    console.error('Database reset error:', error);
    res.status(500).json({ 
      success: false, 
      message: 'Server error during database reset' 
    });
  }
});

// Enhanced logging middleware
app.use((req, res, next) => {
  const timestamp = new Date().toISOString();
  console.log(`[${timestamp}] ${req.method} ${req.path} - ${req.ip}`);
  next();
});

// Start server
app.listen(PORT, () => {
  console.log('='.repeat(50));
  console.log('🚀 BIOMETRIC AUTHENTICATION SERVER STARTED');
  console.log('='.repeat(50));
  console.log(`📡 Server running on: http://localhost:${PORT}`);
  console.log(`🌐 Network IP: http://192.168.246.31:${PORT}`);
  console.log(`📊 MongoDB: ${mongoose.connection.readyState === 1 ? '✅ Connected' : '❌ Disconnected'}`);
  console.log('');
  console.log('📋 Available Endpoints:');
  console.log('  POST /api/register      - Fingerprint registration');
  console.log('  POST /api/verify        - Fingerprint verification');
  console.log('  POST /api/register-face - Face registration');
  console.log('  POST /api/verify-face   - Face verification');
  console.log('  GET  /api/users         - List users');
  console.log('  DELETE /api/reset       - Reset database');
  console.log('='.repeat(50));
});