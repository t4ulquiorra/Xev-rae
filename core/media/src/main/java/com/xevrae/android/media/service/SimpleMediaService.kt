package com.xevrae.android.media.service

import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession

class SimpleMediaService : MediaLibraryService() {
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return null // Stub
    }
}
