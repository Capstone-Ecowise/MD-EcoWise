package com.example.capstone.view.fragment.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.capstone.databinding.BottomSheetBadgeInfoBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetBadgeInfo: BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetBadgeInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetBadgeInfoBinding.inflate(inflater, container, false)
        return binding.root
    }
}