package com.draco.ladb.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.draco.ladb.databinding.FragmentInstallBinding
import com.draco.ladb.viewmodels.ControlActivityViewModel

class InstallFragment(private val title: String) : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(title: String) = InstallFragment(title)
    }

    private lateinit var viewModel: ControlActivityViewModel
    private lateinit var bind: FragmentInstallBinding

    private val launcher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) {
            return@registerForActivityResult
        }
        val inputStream = requireContext().contentResolver.openInputStream(uri)
            ?: return@registerForActivityResult

        viewModel.copyAndInstall(inputStream)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        bind = FragmentInstallBinding.inflate(inflater)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[ControlActivityViewModel::class.java]

        viewModel.shellMessage.observe(requireActivity()) { msg ->
            bind.output.text = msg
        }

        bind.uninstall.setOnClickListener {
            val packageName = bind.command.text.toString()
            if (packageName.isEmpty()) {
                Toast.makeText(requireContext(), "没有输入内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.uninstall(packageName)
        }

        bind.install.setOnClickListener {
            launcher.launch(listOf("application/vnd.android.package-archive").toTypedArray())
        }
    }
}