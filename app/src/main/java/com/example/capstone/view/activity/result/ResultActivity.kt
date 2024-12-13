package com.example.capstone.view.activity.result

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.capstone.R
import com.example.capstone.databinding.ActivityResultBinding
import com.example.capstone.view.activity.detail.DetailActivity
import java.io.File
import java.lang.Double.parseDouble

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get data from intent
        val category = intent.getStringExtra("CATEGORY")
        val imageFile = intent.getSerializableExtra("IMAGE") as? File
        val description = intent.getStringExtra("DESCRIPTION")
        val tips = intent.getStringExtra("TIPS")
        val accuracy = intent.getDoubleExtra("ACCURACY",0.00)
        val accuracy2 = accuracy*100
        binding.include.title.text = "Result"
        binding.include.buttonBack.setOnClickListener{
            finish()
        }
        // Display data in UI elements
        binding.TvPredict.text = "$category"
        binding.TvAccurate.text = "$accuracy2%"
        binding.TvPengolahan.text = "Cara Pengolahan Sampah\n$category"

        imageFile?.let {
            Glide.with(this)
                .load(imageFile)
                .into(binding.imageView4)
        }
        var image = R.drawable.placeholder
        if(category == "Battery"){
            image = R.drawable.battery
        }else if(category == "Cardboard"){
            image = R.drawable.cardboard
        }else if(category == "Biological"){
            image = R.drawable.boilogical
        }else if(category == "Metal"){
            image = R.drawable.metal
        }else if(category == "Paper"){
            image = R.drawable.paper
        }else if(category == "Plastic"){
            image = R.drawable.plastic
        }else if(category == "Shoes"){
            image = R.drawable.shoes
        }else if(category == "Glass"){
            image = R.drawable.glas
        }else if(category == "Clothes"){
            image = R.drawable.clothes
        }else if(category == "Trash"){
           binding.cardView2.visibility = View.GONE
        }
        binding.imageViewrekomendasi.setImageResource(image)
        binding.cardView2.setOnClickListener{
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("CATEGORY", category)
            intent.putExtra("DESCRIPTION", description+tips)
            intent.putExtra("IMAGE", image)
            startActivity(intent)
        }
    }
}