package com.xevrae.viewModel

import androidx.lifecycle.viewModelScope
import com.xevrae.domain.data.entities.NotificationEntity
import com.xevrae.domain.repository.CommonRepository
import com.xevrae.viewModel.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.xevrae.domain.mediaservice.handler.MediaPlayerHandler

@HiltViewModel
class NotificationViewModel @Inject constructor(
    mediaPlayerHandler: MediaPlayerHandler,
    commonRepository: CommonRepository,
) : BaseViewModel(mediaPlayerHandler) {
    private var _listNotification: MutableStateFlow<List<NotificationEntity>?> =
        MutableStateFlow(null)
    val listNotification: StateFlow<List<NotificationEntity>?> = _listNotification

    init {
        viewModelScope.launch {
            commonRepository.getAllNotifications().collect { notificationEntities ->
                _listNotification.value =
                    notificationEntities?.sortedByDescending {
                        it.time
                    }
            }
        }
    }
}