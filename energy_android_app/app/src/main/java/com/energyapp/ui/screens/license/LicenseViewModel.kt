package com.energyapp.ui.screens.license

import androidx.lifecycle.ViewModel
import com.energyapp.util.LicenseManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel to provide LicenseManager to LicenseManagementScreen
 * Used for navigation from dashboard
 */
@HiltViewModel
class LicenseViewModel @Inject constructor(
    val licenseManager: LicenseManager
) : ViewModel()
