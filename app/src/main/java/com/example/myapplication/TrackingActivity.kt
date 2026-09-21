package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityTrackingBinding
import com.example.myapplication.ui.applyScreenInsets

class TrackingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Session.userId == null) {
            returnToLogin()
            return
        }
        val binding = ActivityTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyScreenInsets(binding.root)
        // Búsqueda visual únicamente, según el alcance de esta entrega.
        binding.btnSearch.setOnClickListener { }
        binding.btnLogout.setOnClickListener {
            Session.end()
            returnToLogin()
        }
    }

    private fun returnToLogin() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}

