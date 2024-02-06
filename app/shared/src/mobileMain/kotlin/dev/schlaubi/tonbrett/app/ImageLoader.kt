@file:JvmName("ImageLoaderMobile")

package dev.schlaubi.tonbrett.app

import coil3.disk.DiskCache
import okio.FileSystem
import kotlin.jvm.JvmName

actual fun newDiskCache(): DiskCache? = fileSystemDiskCache(FileSystem.SYSTEM_TEMPORARY_DIRECTORY)
