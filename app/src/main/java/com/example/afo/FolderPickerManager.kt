package com.example.afo

object FolderPickerManager {
    var openFolderPickerCallback: (() -> Unit)? = null
    var onRefreshCallback: (() -> Unit)? = null
}

