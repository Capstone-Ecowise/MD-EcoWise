package com.example.capstone.view.fragment.setting

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.capstone.R
import com.example.capstone.databinding.FragmentSettingBinding
import com.example.capstone.di.ViewModelFactory
import com.example.capstone.view.activity.login.LoginActivity
import com.example.capstone.view.activity.profile.ProfileActivity
import com.example.capstone.view.activity.rank.RankActivity

class SettingFragment : Fragment() {

    private val viewModel by viewModels<SettingViewModel> {
        ViewModelFactory.getInstance(requireActivity())
    }

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAction()
        observeViewModel()

        viewModel.getThemeSettings().observe(viewLifecycleOwner) {isDarkModeActive ->
            if (isDarkModeActive) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                binding.switchTheme.isChecked = true
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                binding.switchTheme.isChecked = false
            }
        }
        binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            viewModel.saveThemeSetting(isChecked)
        }
        binding.changeProfil.setOnClickListener{
            val intent = Intent(activity, ProfileActivity::class.java)
            startActivity(intent)
        }
        viewModel.profileImageUrl.observe(viewLifecycleOwner) { profileImageUrl ->
            Glide.with(this)
                .load(profileImageUrl)
                .into(binding.profilSetting)
        }

    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }

        viewModel.getSession().observe(viewLifecycleOwner) { user ->
            if (user.isLogin) {
                binding.logoutButton.visibility = View.VISIBLE
                binding.loginButton.visibility = View.GONE
                binding.userName.text = user.name
                viewModel.fetchUserProfile(user.token)
            } else {
                binding.logoutButton.visibility = View.GONE
                binding.loginButton.visibility = View.VISIBLE
                binding.userName.text = "Login untuk dapat poin"
                binding.changeProfil.visibility = View.GONE
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.logoutButton.isEnabled = !isLoading
        binding.logoutButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        binding.logoutButton.text = if (isLoading) getString(R.string.loading) else getString(R.string.logout)
    }

    private fun setupAction() {
        binding.rankMenu.setOnClickListener {
            viewModel.getSession().observe(viewLifecycleOwner) { user ->
                if (user.isLogin) {
                    val intent = Intent(requireContext(), RankActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                } else {
                    showLoginRequiredDialog()
                }
            }
        }

        binding.loginButton.setOnClickListener {
            showLoginScreen()
        }

        binding.logoutButton.setOnClickListener {
            viewModel.logout()
        }
    }

    private fun showLoginRequiredDialog() {
        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.login_required))
            .setMessage(getString(R.string.please_login_to_continue))
            .setPositiveButton(getString(R.string.login)) { dialog, _ ->
                dialog.dismiss()
                showLoginScreen()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }

    private fun showLoginScreen() {
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}