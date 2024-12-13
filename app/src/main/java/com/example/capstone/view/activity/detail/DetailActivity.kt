package com.example.capstone.view.activity.detail

import android.os.Bundle
import android.text.Html
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.capstone.R
import com.example.capstone.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.include.title.text = "Detail"
        binding.include.buttonBack.setOnClickListener{
            finish()
        }
        val category = intent.getStringExtra("CATEGORY")
        val description = intent.getStringExtra("DESCRIPTION")
        val image = intent.getIntExtra("IMAGE", 0) // Get image resource ID

        binding.tvDetailName.text = category
        binding.tvDetailDescription.text = description?.let {
            HtmlCompat.fromHtml(
                it,
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }
        Glide.with(this)
            .load(image)
            .into(binding.ivDetailPhoto)
    }
}