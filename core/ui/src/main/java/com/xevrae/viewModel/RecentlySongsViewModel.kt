package com.xevrae.viewModel

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.xevrae.domain.repository.SongRepository
import com.xevrae.pagination.RecentPagingSource
import com.xevrae.viewModel.base.BaseViewModel

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.xevrae.domain.mediaservice.handler.MediaPlayerHandler

@HiltViewModel
class RecentlySongsViewModel @Inject constructor(
    mediaPlayerHandler: MediaPlayerHandler,
    private val songRepository: SongRepository,
) : BaseViewModel(mediaPlayerHandler) {
    val recentlySongs =
        Pager(
            PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20,
            ),
        ) {
            RecentPagingSource(songRepository)
        }.flow.cachedIn(viewModelScope)
}