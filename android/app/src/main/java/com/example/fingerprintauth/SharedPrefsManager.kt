package com.example.fingerprintauth

import android.content.Context
import android.content.SharedPreferences

class SharedPrefsManager(context: Context) {
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        "fingerprint_auth_prefs", 
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_USERNAME = "username"
        private const val KEY_FINGERPRINT_DATA = "fingerprint_data"
        private const val KEY_FACE_DATA = "face_data"
        private const val KEY_IS_REGISTERED = "is_registered"
        private const val KEY_FACE_REGISTERED = "face_registered"
    }
    
    fun saveUserData(username: String, fingerprintData: String) {
        sharedPrefs.edit().apply {
            putString(KEY_USERNAME, username)
            putString(KEY_FINGERPRINT_DATA, fingerprintData)
            putBoolean(KEY_IS_REGISTERED, true)
            apply()
        }
    }
    
    fun getUsername(): String {
        return sharedPrefs.getString(KEY_USERNAME, "") ?: ""
    }
    
    fun getFingerprintData(): String {
        return sharedPrefs.getString(KEY_FINGERPRINT_DATA, "") ?: ""
    }
    
    fun isUserRegistered(): Boolean {
        return sharedPrefs.getBoolean(KEY_IS_REGISTERED, false)
    }
    
    fun saveFaceData(faceData: String) {
        sharedPrefs.edit().apply {
            putString(KEY_FACE_DATA, faceData)
            putBoolean(KEY_FACE_REGISTERED, true)
            apply()
        }
    }
    
    fun getFaceData(): String {
        return sharedPrefs.getString(KEY_FACE_DATA, "") ?: ""
    }
    
    fun isFaceRegistered(): Boolean {
        return sharedPrefs.getBoolean(KEY_FACE_REGISTERED, false)
    }
    
    fun clearUserData() {
        sharedPrefs.edit().clear().apply()
    }
}