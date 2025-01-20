package com.proxyrack.control.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proxyrack.control.domain.updates.UpdateDetails
import com.proxyrack.control.domain.updates.UpdateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class UpdatesViewModel @Inject constructor(private val updateManager: UpdateManager): ViewModel() {

    private var update: UpdateDetails = UpdateDetails(
        available = false,
        // Should never be an empty value to protect against crashing the version parsing library.
        // Our code as is would never have an issue, but a change could introduce an issue if this
        // was empty.
        version = "0",
        url = "",
    )

    private val _updateDialogShowing = MutableStateFlow<Boolean>(false)
    val updateDialogShowing = _updateDialogShowing.asStateFlow()

    fun checkForUpdate() {
        viewModelScope.launch(Dispatchers.IO) {
            update = updateManager.checkForUpdate()
            if (update.available) {
                setDialogShowing(true)
            }
        }
    }

    fun installUpdate() {
        setDialogShowing(false)

        if (!update.available) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                updateManager.installUpdate(update.url, update.version)
            } catch (e: IOException) {
                Log.e(javaClass.simpleName, "IOException downloading update")
            }
        }
    }

    fun ignoreUpdate() {
        setDialogShowing(false)
        updateManager.ignoreUpdate(update.version)
    }

    private fun setDialogShowing(showing: Boolean) {
        _updateDialogShowing.value = showing
    }
}