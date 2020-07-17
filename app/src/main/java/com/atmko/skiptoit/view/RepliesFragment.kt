package com.atmko.skiptoit.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.atmko.skiptoit.databinding.FragmentRepliesBinding

class RepliesFragment: Fragment() {

    private var _binding: FragmentRepliesBinding? = null
    private val binding get() = _binding!!

    private lateinit var commentId: String
    private lateinit var parentId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args: RepliesFragmentArgs by navArgs()
        commentId = args.commentId
        parentId = args.parentId
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRepliesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        configureViews()
        configureValues(savedInstanceState)
        configureViewModel()
    }

    private fun configureViews() {

    }

    private fun configureValues(savedInstanceState: Bundle?) {

    }

    private fun configureViewModel() {

    }
}
