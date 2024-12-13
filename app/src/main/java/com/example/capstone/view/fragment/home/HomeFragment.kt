package com.example.capstone.view.fragment.home

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.capstone.R
import com.example.capstone.data.api.ApiConfig
import com.example.capstone.data.api.ApiService
import com.example.capstone.data.pref.UserPreference
import com.example.capstone.data.pref.dataStore
import com.example.capstone.databinding.FragmentHomeBinding
import com.example.capstone.di.Injection
import com.example.capstone.di.ViewModelFactory
import com.example.capstone.view.activity.detail.DetailActivity
import com.example.capstone.view.activity.login.LoginViewModel
import com.example.capstone.view.fragment.bottomsheet.BottomSheetCamera
import com.example.capstone.view.fragment.setting.SettingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        val repository = Injection.provideRepository(requireContext()) // Create Repository instance
        ViewModelFactory(repository) // Pass Repository to ViewModelFactory
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val cardboard = """
    <p>Cardboard can be recycled more than 20 times before the fibers become too weak. Recycling cardboard has many environmental benefits.</p>
    <ul>
        <li>To recycle cardboard effectively, ensure it is clean and dry, as wet or greasy cardboard can contaminate other recyclables and jam sorting equipment.</li>
        <li>Empty and flatten the boxes, removing packing materials like Styrofoam, but tape and labels can remain as they are removed at recycling centers.</li>
        <li>For oversized boxes, drop them off at a local recycling center, following safety procedures. Consider reusing cardboard for storage, crafts, packing, or donating boxes in good condition.</li>
        <li>If only part of the cardboard is clean, such as the top of a pizza box, separate it from the greasy parts to maximize recyclability.</li>
    </ul>
""".trimIndent() // Use trimIndent() to remove leading whitespace

        val battery = """
    <p>There are different types of batteries, including alkaline, lithium-ion, lead-acid, and rechargeable batteries.</p>
    <p>To manage battery waste responsibly, it's important to avoid throwing them in regular trash and instead recycle them at designated collection points or return them to specialized retailers or recycling centers to ensure safe disposal and recycling.</p>
    <ul>
        <li>To safely handle batteries, tape the terminals to reduce fire risks, and place similar batteries individually or side by side in clear plastic bags to prevent contact between ends.</li>
        <li>Leaking batteries should be sealed in clear plastic bags.</li>
        <li>Avoid disposing of lead-acid batteries in the trash or municipal recycling bins; instead, return them to a battery retailer or a local household hazardous waste collection program.</li>
    </ul>
""".trimIndent()

        val biological = """
    <p>Biological recycling of food waste can be done through a number of methods, including composting, anaerobic digestion, and animal feeding.</p>
    <ul>
        <li>To manage food scraps effectively, use a dedicated bin with a lid to contain odors and deter scavengers, lining it with bags for easy disposal.</li>
        <li>Collect organic waste such as fruit and vegetable peels, eggshells, coffee grounds, and plate scrapings, avoiding contaminants like plastics, glass, or non-organic materials.</li>
        <li>Store the bin in a cool, dry place, preferably in the kitchen, and empty it regularly to prevent odors and ensure space for new scraps.</li>
    </ul>
""".trimIndent()
       binding.textHome.text = "Lets Identify The Trash"

        viewModel.points.observe(viewLifecycleOwner) { points ->
            binding.tvPointHome.text = "$points Point" // Update TextView with points
        }
        viewModel.profileImageUrl.observe(viewLifecycleOwner) { profileImageUrl ->
            Glide.with(this)
                .load(profileImageUrl)
                .into(binding.ivProfileHome)
        }
        val userPreference = UserPreference.getInstance(requireContext().dataStore)
        lifecycleScope.launch {
            userPreference.getSession().collect { user ->
                withContext(Dispatchers.Main) { // Switch to UI thread
                    binding.tvNameHome.text  = "Hi, Welcome "+user.name
                }
            }
        }
        binding.cardView2.setOnClickListener {
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("CATEGORY", "Battery")
            intent.putExtra("DESCRIPTION", battery)
            intent.putExtra("IMAGE", R.drawable.battery)
            startActivity(intent)
        }
        binding.cardView3.setOnClickListener{
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("CATEGORY", "Biological")
            intent.putExtra("DESCRIPTION", biological)
            intent.putExtra("IMAGE", R.drawable.boilogical)
            startActivity(intent)
        }
        binding.cardView4.setOnClickListener{
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("CATEGORY", "Cardboard")
            intent.putExtra("DESCRIPTION", cardboard)
            intent.putExtra("IMAGE", R.drawable.cardboard)
            startActivity(intent)
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.Scan.setOnClickListener {
            val bottomSheet = BottomSheetCamera()
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)

        }
    }

    override fun onResume() {
        super.onResume()
        val userPreference = UserPreference.getInstance(requireContext().dataStore)
        lifecycleScope.launch {
            val token = userPreference.getSession().first().token // Get token from UserPreference
            viewModel.fetchPoints(token) // Call a function in your ViewModel to fetch points
            viewModel.fetchUserProfile(token) // Call a function in your ViewModel to fetch points
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}