package com.draco.ladb.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.draco.ladb.BuildConfig
import com.draco.ladb.R
import com.draco.ladb.databinding.FragmentScreenshotBinding
import com.draco.ladb.viewmodels.ControlActivityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScreenshotFragment(private val title: String) : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(title: String) = ScreenshotFragment(title)
    }

    private lateinit var viewModel: ControlActivityViewModel
    private lateinit var bind: FragmentScreenshotBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentScreenshotBinding.inflate(inflater)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[ControlActivityViewModel::class.java]

        viewModel.shellMessage.observe(requireActivity()) { msg ->
            bind.output.text = msg
        }

        bind.screenshot.setOnClickListener {
            viewModel.screenshot {
                lifecycleScope.launch(Dispatchers.Main) {
                    bind.preview.setImageURI(null)
                    bind.preview.setImageURI(viewModel.screenshotFile.toUri())
                }
            }
        }

        bind.export.setOnClickListener {
            try {
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    BuildConfig.APPLICATION_ID + ".provider",
                    viewModel.screenshotFile
                )
                val intent = Intent(Intent.ACTION_SEND)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(Intent.EXTRA_STREAM, uri)
                    .setType("image/*")
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), getString(R.string.snackbar_intent_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }
}