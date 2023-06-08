package com.myemerg.myemergdemo

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.Date
import java.util.Locale

class MyEmergFileProvider : FileProvider(R.xml.file_paths) {
    companion object {
        fun getUri(context: Context, dir: String, suffix: String): Uri {
            val file = getFile(context, dir, suffix)
            return getUriForFile(
                context,
                context.packageName + ".fileprovider",
                file
            )
        }

        fun getFile(context: Context, dir: String, suffix: String): File {
            val directory = File(context.cacheDir, dir)
            directory.mkdirs()
            val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            return File.createTempFile(name, suffix, directory)
        }
    }
}