package com.projet.sluca.smallbrother.libs

import android.util.Log
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class Compress(private val _files: Array<String?>, private val _zipFile: String) {
    fun zip() {
        try {
            var origin: BufferedInputStream? = null
            val dest = FileOutputStream(_zipFile)
            val out = ZipOutputStream(BufferedOutputStream(dest))
            val data = ByteArray(BUFFER)
            for (i in _files.indices) {
                if (_files[i] != null) // [SL:] Si la cellule contient bien un fichier.
                {
                    Log.d("add:", _files[i]!!)
                    val compress = Log.v("Compress", "Adding: " + _files[i])
                    val fi = FileInputStream(_files[i])
                    origin = BufferedInputStream(fi, BUFFER)
                    val entry = ZipEntry(
                        _files[i]!!.substring(
                            _files[i]!!.lastIndexOf("/") + 1
                        )
                    )
                    out.putNextEntry(entry)
                    var count: Int
                    while (origin.read(data, 0, BUFFER).also {
                            count = it
                        } != -1) {
                        out.write(data, 0, count)
                    }
                    origin.close()
                }
            }
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val BUFFER = 80000
    }
}