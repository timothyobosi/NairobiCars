package com.code.nairobicars

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.code.nairobicars.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var welcomeTextView: TextView
    private lateinit var listCarsButton: Button
    private lateinit var viewTheCars: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        welcomeTextView = findViewById(R.id.welcomeTextView)
        listCarsButton = findViewById(R.id.listCarsButton)

        viewTheCars = findViewById(R.id.viewCars)

        // Handle car listing
        listCarsButton.setOnClickListener {
            // Navigate to Car Listing Activity
            startActivity(Intent(this, ListCarActivity::class.java))
        }

        viewTheCars.setOnClickListener {
            startActivity(Intent(this, ViewCarsActivity::class.java))
        }



    }
}
