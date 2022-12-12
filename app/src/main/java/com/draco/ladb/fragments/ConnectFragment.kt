package com.draco.ladb.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.draco.ladb.R
import com.draco.ladb.databinding.FragmentConnectBinding
import com.draco.ladb.utils.Constant
import com.draco.ladb.viewmodels.ControlActivityViewModel
import com.google.android.material.snackbar.Snackbar

class ConnectFragment(private val title: String) : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(title: String) = ConnectFragment(title)
    }

    private lateinit var viewModel: ControlActivityViewModel
    private lateinit var bind: FragmentConnectBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentConnectBinding.inflate(inflater)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[ControlActivityViewModel::class.java]

        viewModel.shellMessage.observe(requireActivity()) { msg ->
            bind.output.text = msg
        }

        bind.connect.setOnClickListener {
            val ip = bind.command.text.toString()
            if (ip.isEmpty()) {
                Toast.makeText(requireContext(), "没有输入 IP 地址", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!ip.matches(Regex(Constant.IP_ADDRESS_REGEX))) {
                Toast.makeText(requireContext(), "IP 地址错误", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.connectDevice(ip)
        }

        bind.disconnect.setOnClickListener {
            viewModel.disconnectAll()
        }
    }
}