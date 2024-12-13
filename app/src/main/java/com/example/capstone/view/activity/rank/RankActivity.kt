package com.example.capstone.view.activity.rank

import android.os.Bundle
import android.provider.ContactsContract.Data
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.capstone.R
import com.example.capstone.data.api.response.DataItem
import com.example.capstone.databinding.ActivityRankBinding
import com.example.capstone.di.ViewModelFactory
import com.example.capstone.view.fragment.bottomsheet.BottomSheetBadgeInfo
import com.example.capstone.view.fragment.bottomsheet.BottomSheetCamera
import de.hdodenhof.circleimageview.CircleImageView

class RankActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRankBinding

    private lateinit var adapter: RankAdapter

    private val viewModel by viewModels<RankViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRankBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.fetchLeaderboard()
        observeViewModel()

        val layoutManager = LinearLayoutManager(this)
        binding?.rvAspiringWinner?.layoutManager = layoutManager

        val toolbar = binding.include
        setSupportActionBar(toolbar.toolbar)

        toolbar.title.text = "Rank"
        toolbar.icon
        // Set back icon
        toolbar.icon.apply {
            setImageResource(R.drawable.round_info_outline_24)
            setOnClickListener {
                val bottomSheet = BottomSheetBadgeInfo()
                bottomSheet.show(supportFragmentManager, bottomSheet.tag)

            }
        }
        toolbar.buttonBack.apply {
            setOnClickListener {
                @Suppress("DEPRECATION")
                onBackPressed()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        viewModel.leaderboard.observe(this) { leaderboard ->
            if (leaderboard.isNullOrEmpty()) {
                showEmptyMessage(getString(R.string.leaderboard_not_found))
            } else {
                val topThree = leaderboard.take(3)
                setTopThree(topThree)
                setLeaderboard(leaderboard)
            }
        }
    }

    private fun setTopThree(topThree: List<DataItem>) {
        if (topThree.size > 0) {
            val first = topThree[0]
            binding?.userName1?.text = first.name
            binding?.points1?.text = "${first.points} points"

            if(!first.profil.isNullOrEmpty()) {
                Glide.with(this)
                    .load(first.profil)
                    .into(binding.firstWinner)
            }
            else {
                binding.firstWinner.setImageDrawable(getDrawable(R.drawable.placeholder))
            }
        }

        if (topThree.size > 1) {
            val second = topThree[1]
            binding?.userName2?.text = second.name
            binding?.points2?.text = "${second.points} points"

            if(!second.profil.isNullOrEmpty()) {
                Glide.with(this)
                    .load(second.profil)
                    .into(binding.secondWinner)

            }
            else {
                binding.secondWinner.setImageDrawable(getDrawable(R.drawable.placeholder))
            }
        }

        if (topThree.size > 2) {
            val third = topThree[2]
            binding?.userName3?.text = third.name
            binding?.points3?.text = "${third.points} points"

            if(!third.profil.isNullOrEmpty()) {
                Glide.with(this)
                    .load(third.profil)
                    .into(binding.thirdWinner)
            }
            else {
                binding.thirdWinner.setImageDrawable(getDrawable(R.drawable.placeholder))
            }
        }
    }

    private fun setLeaderboard(leaderboard: List<DataItem>) {
        adapter = RankAdapter()
        val items = leaderboard.drop(3)

        val mappedItems = items.map {
            DataItem(profil = it.profil, name = it.name, points = it.points, id = it.id, email = it.email, status = it.status, username = it.username)
        }

        adapter.submitList(mappedItems)
        binding?.rvAspiringWinner?.adapter = adapter
    }

    private fun showEmptyMessage(message: String) {
        binding?.tvEmptyMessage?.text = message
        binding?.tvEmptyMessage?.visibility = View.VISIBLE
        binding?.rvAspiringWinner?.visibility = View.GONE
    }

    private fun showLoading(isLoading: Boolean) {
        binding?.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

}