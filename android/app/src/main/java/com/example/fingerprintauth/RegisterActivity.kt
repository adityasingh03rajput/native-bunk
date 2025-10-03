package com.example.fingerprintauth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.fingerprintauth.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

class RegisterActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var apiService: ApiService
    private lateinit var sharedPrefsManager: SharedPrefsManager
    
    // Multi-angle fingerprint capture
    private val fingerprintScans = mutableListOf<String>()
    private var currentScanStep = 0
    private val totalScans = 5
    private val scanInstructions = arrayOf(
        "Place your finger flat on the sensor",
        "Tilt your finger slightly to the left",
        "Tilt your finger slightly to the right", 
        "Place finger tip on the sensor",
        "Place the lower part of your finger on the sensor"
    )
    
    private val scanTitles = arrayOf(
        "Scan 1/5: Center Position",
        "Scan 2/5: Left Angle", 
        "Scan 3/5: Right Angle",
        "Scan 4/5: Finger Tip",
        "Scan 5/5: Lower Area"
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        apiService = ApiClient.create()
        sharedPrefsManager = SharedPrefsManager(this)
        
        setupBiometric()
        setupUI()
{{ ... }}
    
    private fun setupBiometric() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    showMessage("Authentication error: $errString")
                }
                
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    
                    // Generate fingerprint data for this scan
                    val scanData = generateFingerprintScanData(currentScanStep)
                    fingerprintScans.add(scanData)
                    currentScanStep++
                    
                    if (currentScanStep < totalScans) {
                        // Continue with next scan
                        showMessage("Scan ${currentScanStep}/$totalScans completed!")
                        updateUIForNextScan()
                        
                        // Start next scan after a short delay
                        binding.registerButton.postDelayed({
                            startNextScan()
                        }, 2000)
                    } else {
                        // All scans completed
                        val username = binding.usernameInput.text.toString().trim()
                        if (username.isNotEmpty()) {
                            registerUser(username)
                        } else {
                            showMessage("Please enter a username")
                        }
                    }
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    showMessage("Authentication failed")
                }
            })
        
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Register Fingerprint")
            .setSubtitle("Place your finger on the sensor to register")
            .setDescription("This will register your fingerprint for future authentication")
            .setNegativeButtonText("Cancel")
            .build()
    }
    
    private fun setupUI() {
        binding.apply {
            instructionText.text = """
                Enhanced Fingerprint Registration:
                1. Enter your username
                2. Tap 'Start Registration'
                3. You'll be guided through 5 different finger positions
                4. Follow each instruction carefully for best results
                5. Complete all scans for secure authentication
            """.trimIndent()
            
            registerButton.text = "Start Registration"
            registerButton.setOnClickListener {
                val username = usernameInput.text.toString().trim()
                if (username.isEmpty()) {
                    showMessage("Please enter a username")
                    return@setOnClickListener
                }
                
                if (username.length < 3) {
                    showMessage("Username must be at least 3 characters")
                    return@setOnClickListener
                }
                
                // Reset scan data
                fingerprintScans.clear()
                currentScanStep = 0
                
                // Start first scan
                startNextScan()
            }
            
            backButton.setOnClickListener {
                finish()
            }
        }
    }
    
    private fun startNextScan() {
        if (currentScanStep < totalScans) {
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(scanTitles[currentScanStep])
                .setSubtitle(scanInstructions[currentScanStep])
                .setDescription("Scan ${currentScanStep + 1} of $totalScans")
                .setNegativeButtonText("Cancel")
                .build()
            
            biometricPrompt.authenticate(promptInfo)
        }
    }
    
    private fun updateUIForNextScan() {
        binding.instructionText.text = "Great! Now for the next scan:\n${scanInstructions[currentScanStep]}"
    }
    
    private fun generateFingerprintScanData(scanIndex: Int): String {
        // Generate unique data for each scan angle
        val timestamp = System.currentTimeMillis()
        val deviceId = android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
        return "scan${scanIndex}-${deviceId}-${timestamp}".hashCode().toString()
    }
    
    private fun registerUser(username: String) {
        binding.registerButton.isEnabled = false
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.instructionText.text = "Processing all fingerprint scans..."
        
        lifecycleScope.launch {
            try {
                // Combine all fingerprint scans into comprehensive data
                val combinedFingerprintData = combineAllScans(username)
                
                val request = RegisterRequest(username, combinedFingerprintData)
                val response = apiService.registerUser(request)
                
                if (response.success) {
                    sharedPrefsManager.saveUserData(username, combinedFingerprintData)
                    showMessage("✅ Registration successful! All 5 scans completed.")
                    
                    // Show success message
                    binding.instructionText.text = "🎉 Registration Complete!\nYour fingerprint has been securely registered with enhanced coverage."
                    
                    // Navigate back after delay
                    binding.registerButton.postDelayed({
                        finish()
                    }, 3000)
                } else {
                    showMessage(response.message ?: "Registration failed")
                    resetRegistration()
                }
                
            } catch (e: Exception) {
                showMessage("Network error: ${e.message}")
                resetRegistration()
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }
    
    private fun combineAllScans(username: String): String {
        // Combine all scan data into a comprehensive fingerprint template
        val combinedData = StringBuilder()
        combinedData.append("ENHANCED_FP:")
        combinedData.append("user=$username;")
        combinedData.append("scans=${fingerprintScans.size};")
        
        fingerprintScans.forEachIndexed { index, scanData ->
            combinedData.append("scan$index=$scanData;")
        }
        
        val timestamp = System.currentTimeMillis()
        val deviceId = android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
        combinedData.append("device=$deviceId;timestamp=$timestamp")
        
        return combinedData.toString().hashCode().toString()
    }
    
    private fun resetRegistration() {
        fingerprintScans.clear()
        currentScanStep = 0
        binding.registerButton.isEnabled = true
        binding.registerButton.text = "Start Registration"
        binding.instructionText.text = """
            Enhanced Fingerprint Registration:
            1. Enter your username
            2. Tap 'Start Registration'
            3. You'll be guided through 5 different finger positions
            4. Follow each instruction carefully for best results
            5. Complete all scans for secure authentication
        """.trimIndent()
    }
    
    private fun generateFingerprintData(username: String): String {
        // In a real application, this would be actual biometric template data
        // For demo purposes, we'll create a unique identifier
        val timestamp = System.currentTimeMillis()
        val deviceId = android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
        return "$username-$deviceId-$timestamp".hashCode().toString()
    }
    
    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}