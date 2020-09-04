/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import androidx.documentfile.provider.DocumentFile
import com.stokerapps.chartmaker.domain.FileManager
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class AndroidFileManager(private val context: Context) : FileManager {

    override fun getMetaData(uriString: String): FileManager.MetaData {
        val uri = Uri.parse(uriString)
        val cursor: Cursor? = context.contentResolver.query(
            uri, null, null, null, null, null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val displayName =
                    it.getStringOrNull(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                val size = it.getIntOrNull(it.getColumnIndex(OpenableColumns.SIZE))

                return FileManager.MetaData(displayName, size)
            }
        }
        return FileManager.MetaData()
    }

    override fun getMimeType(uriString: String): String? {
        val uri = Uri.parse(uriString)
        return if (uri.scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(context.contentResolver.getType(uri))
        } else {
            return uri.path?.let { path ->
                MimeTypeMap.getFileExtensionFromUrl(
                    Uri.fromFile(File(path)).toString()
                )
            }
        }
    }

    override fun read(uriString: String): InputStream {
        val uri = Uri.parse(uriString)
        return context.contentResolver.openInputStream(uri)
            ?: throw FileIOException(uri)
    }

    override fun write(uriString: String): OutputStream {
        val uri = Uri.parse(uriString)
        return context.contentResolver.openOutputStream(uri)
            ?: throw FileIOException(uri)
    }

    override fun write(
        directoryUriString: String,
        filename: String,
        mimeType: String
    ): OutputStream {
        val directoryUri = Uri.parse(directoryUriString)
        val directory = DocumentFile.fromTreeUri(context, directoryUri)
        val uri = directory?.createFile(mimeType, filename)?.uri
        return uri?.let {
            context.contentResolver.openOutputStream(it)
                ?: throw FileIOException(uri)
        } ?: throw FileIOException(uri)
    }

    class FileIOException(uri: Uri?) : IOException("Could not open ${uri?.path ?: "file"}")
}