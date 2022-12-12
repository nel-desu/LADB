package com.draco.ladb.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.draco.ladb.databinding.FragmentLogBinding
import com.draco.ladb.viewmodels.ControlActivityViewModel

class LogFragment(private val title: String) : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(title: String) = LogFragment(title)
    }

    private lateinit var viewModel: ControlActivityViewModel
    private lateinit var bind: FragmentLogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentLogBinding.inflate(inflater)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[ControlActivityViewModel::class.java]

        viewModel.shellMessage.observe(requireActivity()) { msg ->
            bind.output.text = msg
        }

        bind.logcatStart.setOnClickListener {
            viewModel.logcatStart()
        }

        bind.logcatStop.setOnClickListener {
            viewModel.logcatStop()
        }
    }
}