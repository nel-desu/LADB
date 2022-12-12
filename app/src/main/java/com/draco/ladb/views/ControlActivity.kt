package com.draco.ladb.views

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.draco.ladb.BuildConfig
import com.draco.ladb.R
import com.draco.ladb.databinding.ActivityControlBinding
import com.draco.ladb.fragments.ConnectFragment
import com.draco.ladb.fragments.InstallFragment
import com.draco.ladb.fragments.LogFragment
import com.draco.ladb.fragments.ScreenshotFragment
import com.draco.ladb.viewmodels.ControlActivityViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ControlActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ControlActivity"

        private val titles = listOf("连接设备", "安装卸载", "屏幕截图", "查看日志")
    }

    class ViewPagerAdapter(
        activity: FragmentActivity,
        private val count: Int
    ): FragmentStateAdapter(activity) {

        override fun getItemCount() = count

        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> ConnectFragment.newInstance(titles[position])
                1 -> InstallFragment.newInstance(titles[position])
                2 -> ScreenshotFragment.newInstance(titles[position])
                3 -> LogFragment.newInstance(titles[position])
                else -> ConnectFragment.newInstance(titles[0])
            }
        }
    }

    private val viewModel: ControlActivityViewModel by viewModels()
    private lateinit var binding: ActivityControlBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityControlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.progressVisibility.observe(this) { visibility ->
            binding.progress.visibility = if (visibility) View.VISIBLE else View.GONE
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
        })


        binding.viewPager.adapter = ViewPagerAdapter(this, titles.size)
        val mediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titles[position]
        }
        mediator.attach()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.shell -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.clear -> {
                viewModel.clearMessage()
                true
            }
            R.id.share -> {
                try {
                    val uri = FileProvider.getUriForFile(
                        this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        viewModel.logFile
                    )
                    val intent = Intent(Intent.ACTION_SEND)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(Intent.EXTRA_STREAM, uri)
                        .setType("application/txt")
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, getString(R.string.snackbar_intent_failed), Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.control, menu)
        return true
    }
}