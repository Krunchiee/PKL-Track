package com.example.pkltrack.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.InputStream
import java.io.OutputStream

object FileUtil {
    fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = System.currentTimeMillis().toString() + ".jpg"
            val tempFile = File(context.cacheDir, fileName)

            tempFile.outputStream().use { output: OutputStream ->
                inputStream.copyTo(output)
            }

            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
