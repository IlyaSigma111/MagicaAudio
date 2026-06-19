package com.example.magica.audio

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object AudioUtils {

    fun copyUriToInternal(context: Context, uri: Uri, fileName: String): String {
        val dir = File(context.filesDir, "imports")
        dir.mkdirs()
        val dest = File(dir, "${System.currentTimeMillis()}_$fileName")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(dest).use { output ->
                input.copyTo(output)
            }
        }
        return dest.absolutePath
    }

    fun getFileName(context: Context, uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) return it.getString(idx)
            }
        }
        val path = uri.path ?: return "audio_${System.currentTimeMillis()}"
        return path.substringAfterLast('/').ifEmpty { "audio_${System.currentTimeMillis()}" }
    }

    fun getOutputDir(context: Context): File {
        val dir = File(context.filesDir, "exports")
        dir.mkdirs()
        return dir
    }

    fun getCacheDir(context: Context): File {
        val dir = File(context.cacheDir, "audio")
        dir.mkdirs()
        return dir
    }

    fun cleanCache(context: Context) {
        getCacheDir(context).listFiles()?.forEach { it.delete() }
    }
}
