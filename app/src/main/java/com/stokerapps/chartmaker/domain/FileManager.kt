/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.domain

import java.io.InputStream
import java.io.OutputStream

interface FileManager {
    fun getMetaData(uriString: String): MetaData
    fun getMimeType(uriString: String): String?
    fun read(uriString: String): InputStream
    fun write(uriString: String): OutputStream
    fun write(directoryUriString: String, filename: String, mimeType: String): OutputStream

    data class MetaData(
        val displayName: String? = null,
        val size: Int? = null
    )
}