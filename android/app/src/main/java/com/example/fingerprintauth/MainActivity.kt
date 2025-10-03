package com.example.fingerprintauth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import com.example.fingerprintauth.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPrefsManager: SharedPrefsManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sharedPrefsManager = SharedPrefsManager(this)
        
        checkBiometricSupport()
        setupUI()
    }
    
    private fun checkBiometricSupport() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // Biometric authentication is available
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                showError("No biometric hardware available")
                return
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                showError("Biometric hardware unavailable")
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                showError("No biometric credentials enrolled")
                return
            }
        }
    }
    
    private fun setupUI() {
        binding.apply {
            welcomeText.text = "Biometric Authentication"
            instructionText.text = "Choose your preferred authentication method"
            
            registerButton.text = "🔐 Fingerprint Authentication"
            loginButton.text = "📷 Face Authentication"
            
            registerButton.setOnClickListener {
                showFingerprintOptions()
            }
            
            loginButton.setOnClickListener {
                showFaceOptions()
            }
            
            // Hide face register button initially
            val faceRegisterButton = findViewById<android.widget.Button>(R.id.faceRegisterButton)
            faceRegisterButton?.visibility = android.view.View.GONE
        }
    }
    
    private fun showFingerprintOptions() {
        val options = if (sharedPrefsManager.isUserRegistered()) {
            arrayOf("Login with Fingerprint", "Register New Fingerprint")
        } else {
            arrayOf("Register Fingerprint")
        }
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Fingerprint Authentication")
            .setIcon(R.drawable.ic_fingerprint)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        if (sharedPrefsManager.isUserRegistered()) {
                            startActivity(Intent(this, LoginActivity::class.java))
                        } else {
                            startActivity(Intent(this, RegisterActivity::class.java))
                        }
                    }
                    1 -> {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showFaceOptions() {
        val options = if (sharedPrefsManager.isFaceRegistered()) {
            arrayOf("Login with Face", "Register New Face")
        } else {
            arrayOf("Register Face")
        }
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Face Authentication")
            .setIcon(R.drawable.ic_face)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        if (sharedPrefsManager.isFaceRegistered()) {
                            startActivity(Intent(this, FaceLoginActivity::class.java))
                        } else {
                            if (!sharedPrefsManager.isUserRegistered()) {
                                showMessage("Please register with fingerprint first, then add face authentication.")
                                return@setItems
                            }
                            startActivity(Intent(this, FaceRegisterActivity::class.java))
                        }
                    }
                    1 -> {
                        if (!sharedPrefsManager.isUserRegistered()) {
                            showMessage("Please register with fingerprint first, then add face authentication.")
                            return@setItems
                        }
                        startActivity(Intent(this, FaceRegisterActivity::class.java))
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showError(message: String) {
        binding.instructionText.text = message
        binding.registerButton.isEnabled = false
        binding.loginButton.isEnabled = false
    }
    
    private fun showMessage(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
    }
}