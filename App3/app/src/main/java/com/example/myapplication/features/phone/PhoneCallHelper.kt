package com.example.myapplication.features.phone

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat

class PhoneCallHelper(private val context: Context) {

    fun hasCallPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun makeCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phoneNumber")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun openDialer(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    companion object {
        val PREDEFINED_CONTACTS = listOf(
            Contact("Urgences", "15", "🚑"),
            Contact("Police", "17", "🚔"),
            Contact("Pompiers", "18", "🚒"),
            Contact("Numéro d'urgence européen", "112", "🆘"),
            Contact("Support technique", "+33123456789", "🛠️")
        )
    }
}

data class Contact(
    val name: String,
    val number: String,
    val emoji: String
)
