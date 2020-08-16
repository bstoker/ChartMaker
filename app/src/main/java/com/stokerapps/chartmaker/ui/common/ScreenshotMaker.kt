/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.annotation.RequiresApi
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.common.AppDispatchers as Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

private const val DIRECTORY_SCREENSHOTS = "Screenshots"
private const val FILE_EXTENSION = ".png"
private const val MAX_IMAGE_QUALITY = 100
private const val MIME_TYPE = "image/*"

/**
 * Saves screenshots to the common /Pictures/Screenshots/ directory.
 */
class ScreenshotMaker(val context: Context?) {

    suspend fun takeAndSaveScreenshot(
        view: View,
        filename: String,
        quality: Int = MAX_IMAGE_QUALITY
    ): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                takeAndSaveScreenshotApi29(view, filename, quality)
            }
            else -> {
                takeAndSaveScreenshotCompat(view, filename, quality)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun takeAndSaveScreenshotApi29(
        view: View,
        filename: String,
        quality: Int = MAX_IMAGE_QUALITY
    ): Boolean {

        val contentResolver = context?.contentResolver
        val contentUri =
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val imageDetails = getImageDetailsWithPendingStatus(filename)
        var success = false

        withContext(Dispatchers.IO) {
            val bitmap = view.toBitmap()
            val uri = contentResolver?.insert(contentUri, imageDetails)
            if (uri != null) {
                success = contentResolver.saveBitmap(bitmap, uri, quality)
                contentResolver.releasePendingStatus(uri, imageDetails)
                notifyMediaScannerService(uri.toString())
            }
            bitmap.recycle()
        }
        return success
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun ContentResolver.saveBitmap(
        bitmap: Bitmap,
        uri: Uri,
        quality: Int = 100
    ): Boolean {
        try {
            openFileDescriptor(uri, "w", null)?.use {
                if (it.fileDescriptor != null) {
                    with(FileOutputStream(it.fileDescriptor)) {
                        bitmap.compress(
                            Bitmap.CompressFormat.PNG,
                            quality, this
                        )
                        flush()
                        close()
                    }
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getImageDetailsWithPendingStatus(filename: String): ContentValues =
        ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename + FILE_EXTENSION)
            put(MediaStore.MediaColumns.MIME_TYPE, MIME_TYPE)
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + File.separator + DIRECTORY_SCREENSHOTS
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun ContentResolver.releasePendingStatus(uri: Uri, values: ContentValues) {
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        update(uri, values, null, null)
    }

    @Suppress("DEPRECATION")
    private suspend fun takeAndSaveScreenshotCompat(
        view: View,
        filename: String,
        quality: Int = MAX_IMAGE_QUALITY
    ): Boolean {

        val picturesDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val screenshotsDirectory = File(
            picturesDirectory,
            DIRECTORY_SCREENSHOTS
        )
        var success = false

        withContext(Dispatchers.IO) {
            val bitmap = view.toBitmap()
            try {
                if (screenshotsDirectory.exists() || screenshotsDirectory.mkdirs()) {

                    val uri = Uri.parse(
                        screenshotsDirectory.absolutePath + File.separator
                                + filename + FILE_EXTENSION
                    )

                    saveBitmapCompat(bitmap, uri, quality)
                    notifyMediaScannerService(uri.toString())
                    success = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                bitmap.recycle()
            }
        }
        return success
    }

    private fun notifyMediaScannerService(vararg paths: String) {
        context?.let {
            MediaScannerConnection.scanFile(it, paths, null, null)
        }
    }

    /**
     * @exception FileNotFoundException
     * @exception SecurityException
     * @exception IOException
     */
    private fun saveBitmapCompat(bitmap: Bitmap, uri: Uri, quality: Int = MAX_IMAGE_QUALITY) {
        val fos = FileOutputStream(File(uri.toString()))
        bitmap.compress(
            Bitmap.CompressFormat.PNG,
            quality, fos
        )
        fos.flush()
        fos.close()
    }

    private fun View.toBitmap(config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
        val bitmap: Bitmap = Bitmap.createBitmap(width, height, config)
        val canvas = Canvas(bitmap)
        background?.let { background.draw(canvas) } ?: canvas.drawColor(getBackgroundColor())
        draw(canvas)
        return bitmap
    }

    private fun getBackgroundColor(): Int {
        return context?.getColorCompat(R.color.colorBackgroundChart) ?: Color.WHITE
    }

}