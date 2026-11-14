package com.batmancpu.player.ui.library

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

/** Application context only: never retain an Activity or its composition. */
internal class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    val session = LibrarySession(
        scope = viewModelScope,
        load = { tree -> OfflineLibraryIndex.activateAndLoad(application, Uri.parse(tree)) },
        scan = { tree -> SafLocalMediaSource(application, Uri.parse(tree)).scan() },
        save = { tree, media ->
            OfflineLibraryIndex.saveSuccessfulScan(application, Uri.parse(tree), media)
        },
    )
}
