package com.atmko.skiptoit.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.atmko.skiptoit.databinding.FragmentSubscriptionsBinding
import com.atmko.skiptoit.viewmodel.SubscriptionsViewModel

class SubscriptionsFragment : Fragment() {
    private var _binding: FragmentSubscriptionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var subscriptionsViewModel: SubscriptionsViewModel

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentSubscriptionsBinding.inflate(inflater, container, false)
        return binding.root
    }
}
