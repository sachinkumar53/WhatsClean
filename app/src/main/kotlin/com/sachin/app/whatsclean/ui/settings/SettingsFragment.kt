package com.sachin.app.whatsclean.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment.getExternalStorageDirectory
import android.os.storage.StorageManager
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.preference.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.files.folderChooser
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mikhaellopez.biometric.BiometricHelper
import com.mikhaellopez.biometric.BiometricPromptInfo
import com.sachin.app.whatsclean.BuildConfig
import com.sachin.app.whatsclean.R
import com.sachin.app.whatsclean.data.model.SortType
import com.sachin.app.whatsclean.util.extension.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {
    private val viewModel: SettingsViewModel by viewModels()
    private val launcher = registerForActivityResult(OpenDocumentTree()) { treeUri ->
        if (treeUri.isValidateWhatsAppTreeUri()) {
            requireContext().contentResolver.takePersistableUriPermission(
                treeUri!!,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            viewModel.setTreeUri(treeUri)
            findNavController().navigate(R.id.action_permissionFragment_to_dashboardFragment)
        } else {
            requireContext().showToast(R.string.invalid_whatsapp_folder)
        }

        Log.i(TAG, "Uri:$treeUri ")
    }

    private var whatsAppFolderPref: Preference? = null
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_main, rootKey)

        whatsAppFolderPref = findPreference(getString(R.string.pref_key_whatsapp_folder))
        findPreference<Preference>(getString(R.string.pref_key_app_version))?.apply {
            summary = BuildConfig.VERSION_NAME
        }

        findPreference<DropDownPreference>(
            getString(R.string.pref_key_theme)
        )?.setOnPreferenceChangeListener { _, newValue ->
            viewModel.setTheme(newValue as String)
            true
        }

        findPreference<ListPreference>(
            getString(R.string.pref_key_sort_by)
        )?.setOnPreferenceChangeListener { _, newValue ->
            viewModel.setSortType(
                SortType.values().getOrNull(
                    (newValue as? String)?.toInt() ?: 0
                ) ?: SortType.NEWEST_FIRST
            )
            true
        }

        val biometricHelper = BiometricHelper(this)
        findPreference<SwitchPreferenceCompat>(getString(R.string.pref_key_fingerprint))?.apply {
            isVisible = arrayOf(
                BiometricManager.BIOMETRIC_SUCCESS,
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
            ).any { viewModel.canAuthenticate(requireContext()) == it }

            setOnPreferenceChangeListener { _, newValue ->
                if (newValue as Boolean) {
                    biometricHelper.showBiometricPrompt(
                        BiometricPromptInfo(
                            title = getString(R.string.fingerprint_prompt_title),
                            negativeButtonText = getString(R.string.cancel),
                            description = getString(R.string.fingerprint_prompt_desc)
                        ),
                        onError = { errorCode, _ ->
                            isChecked = false
                            viewModel.setFingerPrintEnabled(false)
                            if (errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS) {
                                MaterialAlertDialogBuilder(requireActivity())
                                    .setTitle(R.string.fingerprint_not_added)
                                    .setMessage(R.string.enroll_fingerprint_message)
                                    .setNegativeButton(R.string.cancel, null)
                                    .setPositiveButton(R.string.fingerprint_setup) { _, _ ->
                                        viewModel.launchFingerprintSetup(requireContext())
                                    }
                                    .show()
                            }
                        }, onFailed = {
                            isChecked = false
                            viewModel.setFingerPrintEnabled(false)
                            requireContext().showToast(getString(R.string.authentication_failed))
                        }, onSuccess = {
                            isChecked = true
                            viewModel.setFingerPrintEnabled(true)
                        }
                    )
                    false
                } else {
                    viewModel.setFingerPrintEnabled(false)
                    true
                }

            }
        }

        findPreference<Preference>(getString(R.string.pref_key_contact_us))?.setOnPreferenceClickListener {
            requireContext().sendEmail()
            true
        }

        findPreference<Preference>(
            getString(R.string.pref_key_whatsapp_folder)
        )?.apply {

            setOnPreferenceClickListener {
                startFolderChooser()
                true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAndroidRorAbove) {
            viewModel.treeUriLiveData.observe(viewLifecycleOwner) {
                whatsAppFolderPref?.summary = it?.toString()
            }
        } else {
            viewModel.whatsAppFolderLiveData.observe(viewLifecycleOwner) {
                whatsAppFolderPref?.summary = it
            }
        }

        viewModel.sortTypeLiveData.observe(viewLifecycleOwner) { sortType ->
            findPreference<ListPreference>(
                getString(R.string.pref_key_sort_by)
            )?.let {
                if (it.value?.toInt() != sortType.ordinal) {
                    it.setValueIndex(sortType.ordinal)
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun startFolderChooser() {
        if (isAndroidRorAbove) {
            openTreeChooser()
        } else {
            val initialFolder = getExternalStorageDirectory()
            //val context = ContextThemeWrapper(requireActivity(), R.style.FullscreenDialog)
            MaterialDialog(requireActivity()).show {
                /*setOnShowListener {
                    val rv = findViewById<View>(R.id.list)
                    var myParent: ViewParent? = rv.parent

                    rv.updateLayoutParams {
                        width = ViewGroup.LayoutParams.MATCH_PARENT
                        height = ViewGroup.LayoutParams.MATCH_PARENT

                    }
                    while (myParent != null && myParent is View) {
                        myParent.updateLayoutParams {
                            width = ViewGroup.LayoutParams.MATCH_PARENT
                            height = ViewGroup.LayoutParams.MATCH_PARENT

                        }
                        myParent = (myParent as? ViewParent)?.parent
                    }

                    window?.apply {
                        clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                        setLayout(MATCH_PARENT, MATCH_PARENT)
                    }
                }*/
                folderChooser(
                    context = requireContext(),
                    initialDirectory = initialFolder
                ) { _, folder ->
                    // Folder selected
                    viewModel.setWhatsAppFolder(requireContext(), folder.absolutePath)
                    Log.i(TAG, "startFolderChooser: ${folder.toURI()}")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun openTreeChooser() {
        val storageManager =
            requireContext().getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val intent = storageManager.primaryStorageVolume.createOpenDocumentTreeIntent()
        var uri = intent.getParcelableExtra<Uri>("android.provider.extra.INITIAL_URI") as Uri
        var scheme = uri.toString()
        scheme = scheme.replace("/root/", "/document/")
        scheme += "%3A$WHATSAPP_URI"
        uri = Uri.parse(scheme)
        intent.putExtra("android.provider.extra.INITIAL_URI", uri)
        launcher.launch(uri)
    }
}

private const val TAG = "SettingsFragment"