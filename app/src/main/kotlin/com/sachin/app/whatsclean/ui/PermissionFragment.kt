package com.sachin.app.whatsclean.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.storage.StorageManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.hoc081098.viewbindingdelegate.viewBinding
import com.sachin.app.whatsclean.R
import com.sachin.app.whatsclean.data.SettingsManager
import com.sachin.app.whatsclean.databinding.FragmentPermissionBinding
import com.sachin.app.whatsclean.util.extension.WHATSAPP_URI
import com.sachin.app.whatsclean.util.extension.isAndroidRorAbove
import com.sachin.app.whatsclean.util.extension.isValidateWhatsAppTreeUri
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PermissionFragment : Fragment(R.layout.fragment_permission) {
    private val binding: FragmentPermissionBinding by viewBinding()

    @Inject
    lateinit var settingsManager: SettingsManager

    override fun onStart() {
        super.onStart()
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }

    private val activityLauncher = registerForActivityResult(StartActivityForResult()) {
        val treeUri = it.data?.data

        if (treeUri.isValidateWhatsAppTreeUri()) {
            requireContext().contentResolver.takePersistableUriPermission(
                treeUri!!,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            settingsManager.setTreeUri(treeUri)
            findNavController().navigate(R.id.action_permissionFragment_to_dashboardFragment)
        } else {
            Snackbar.make(
                binding.root,
                "Selected folder is not a valid WhatsApp folder.",
                Snackbar.LENGTH_INDEFINITE
            ).setAction("Try again") {
                askStoragePermission()
            }.show()
        }

        Log.i(TAG, "Uri:${it.data?.data} ")
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                findNavController().navigate(R.id.action_permissionFragment_to_dashboardFragment)
            } else {
                Snackbar.make(
                    binding.root,
                    "Storage access denied. Allow it to use the app.",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("Try again") {
                    askStoragePermission()
                }.show()
            }

            Log.d(TAG, "iGranted: $isGranted")
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.run {
            exitButton.setOnClickListener {
                findNavController().navigateUp()
                activity?.finish()
            }

            allowButton.setOnClickListener {
                askStoragePermission()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun openTreeChooser() {
        try {
            val storageManager =
                requireContext().getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val intent = storageManager.primaryStorageVolume.createOpenDocumentTreeIntent()
            var uri = intent.getParcelableExtra<Uri>("android.provider.extra.INITIAL_URI") as Uri
            var scheme = uri.toString()
            scheme = scheme.replace("/root/", "/document/")
            scheme += "%3A$WHATSAPP_URI"
            uri = Uri.parse(scheme)
            intent.putExtra("android.provider.extra.INITIAL_URI", uri)
            activityLauncher.launch(intent)
        }catch (e:ActivityNotFoundException){
            Log.e(TAG, "openTreeChooser: Unable to launch tree chooser", e)
            Snackbar.make(
                binding.root,
                "There is no app installed to handle this action",
                Snackbar.LENGTH_INDEFINITE
            ).show()
        }
    }

    private fun askStoragePermission() {
        if (isAndroidRorAbove) {
            openTreeChooser()
        } else {
            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
}

private const val TAG = "PermissionFragment"