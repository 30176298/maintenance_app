package com.example.theseus.presentation.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.theseus.data.sync.SyncManager
import com.example.theseus.data.sync.SyncResult
import com.example.theseus.data.sync.SyncState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SyncViewModel(
    private val syncManager: SyncManager
) : ViewModel() {

    val syncState: StateFlow<SyncState> = syncManager.syncState
    val lastSyncTime: StateFlow<Long?> = syncManager.lastSyncTime

    init {
        syncManager.loadLastSyncTime()
    }

    fun triggerSync() {
        viewModelScope.launch {
            syncManager.sync()
        }
    }
}