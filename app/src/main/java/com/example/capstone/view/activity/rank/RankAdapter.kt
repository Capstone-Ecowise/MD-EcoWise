package com.example.capstone.view.activity.rank

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.capstone.R
import com.example.capstone.data.api.response.DataItem
import com.example.capstone.databinding.AspiringWinnerItemBinding

class RankAdapter : ListAdapter<DataItem, RankAdapter.RankViewHolder>(
    DIFF_CALLBACK
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankViewHolder {
        val binding = AspiringWinnerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RankViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RankViewHolder, position: Int) {
        val storyItem = getItem(position)
        holder.bind(storyItem, position+4)
    }

    inner class RankViewHolder(private val binding: AspiringWinnerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(rank: DataItem, order: Int) {
            binding.apply {

                numbering.text = order.toString()

                if(!rank.profil.isNullOrEmpty()) {
                    Glide.with(itemView.context)
                        .load(rank.profil)
                        .into(aspiringPhoto)
                }
                else {
                    aspiringPhoto.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.placeholder))
                }
                userName.text = rank.name
                points.text = "${rank.points} Points"
                when {
                    rank.points in 0..99 -> tierBadge.setImageResource(R.drawable.badge_seed_tier)
                    rank.points in 100..499 -> tierBadge.setImageResource(R.drawable.badge_sprout_tier)
                    rank.points in 500..999 -> tierBadge.setImageResource(R.drawable.badge_sapling_tier)
                    rank.points in 1000..4999 -> tierBadge.setImageResource(R.drawable.badge_tree_tier)
                    rank.points >= 5000 -> tierBadge.setImageResource(R.drawable.badge_forestguardian_tier)
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DataItem>() {
            override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}