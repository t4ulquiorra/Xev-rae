package com.xevrae.data.io

import com.xevrae.common.ContextHolder
import okio.FileSystem

fun fileSystem(): FileSystem = FileSystem.SYSTEM

fun fileDir(): String {
    return ContextHolder.context.filesDir.absolutePath
}