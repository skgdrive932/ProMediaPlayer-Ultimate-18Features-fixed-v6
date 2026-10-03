package com.skkaushal.promediaplayer.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.skkaushal.promediaplayer.R
import com.skkaushal.promediaplayer.data.MediaStoreRepository
import com.skkaushal.promediaplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: MediaStoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = MediaStoreRepository(this)

        // Setup Symbian Bottom Softkeys (Options & Exit)
        setupSymbianSoftkeys()

        checkPermissionsAndLoad()
    }

    private fun setupSymbianSoftkeys() {
        // Exit Softkey: App close karne ke liye
        binding.btnExit.setOnClickListener {
            finish()
        }

        // Options Softkey: Classic Symbian Menu Open karne ke liye
        binding.btnOptions.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menu.add("Refresh Media")
            popup.menu.add("About Symbian Player")
            popup.menu.add("Exit")

            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Refresh Media" -> {
                        checkPermissionsAndLoad()
                        Toast.makeText(this, "Refreshing media...", Toast.LENGTH_SHORT).show()
                        true
                    }
                    "About Symbian Player" -> {
                        showAboutDialog()
                        true
                    }
                    "Exit" -> {
                        finish()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("ProMediaPlayer - S60 Edition")
            .setMessage("Copyright (c) 2026 Sk. Kaushal\nAll Rights Reserved.\n\nClassic Symbian OS Style Media Player.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun checkPermissionsAndLoad() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            loadFolders()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 101)
        }
    }

    private fun loadFolders() {
        val folders = repository.fetchFoldersWithMedia()
        val adapter = FolderAdapter(folders) { folder ->
            val intent = Intent(this, FolderDetailActivity::class.java).apply {
                putExtra("FOLDER_NAME", folder.name)
            }
            startActivity(intent)
        }

        // Using Symbian Layout RecyclerView ID (rvFolders)
        binding.rvFolders.layoutManager = LinearLayoutManager(this)
        binding.rvFolders.adapter = adapter
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadFolders()
        } else {
            Toast.makeText(this, "Permission required to display videos", Toast.LENGTH_SHORT).show()
        }
    }
}
