package com.projet.sluca.smallbrother.libs

import android.util.Log
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class Compress(private val files: Array<String?>, private val zipFile: String) {
    fun zip() {
        try {
            var origin: BufferedInputStream?
            val dest = FileOutputStream(zipFile)
            val out = ZipOutputStream(BufferedOutputStream(dest))
            val data = ByteArray(BUFFER)
            for (i in files.indices) {
                if (files[i] != null) // [SL:] Si la cellule contient bien un fichier.
                {
                    Log.d("add:", files[i].toString())
                    Log.v("Compress", "Adding: " + files[i])
                    val fi = FileInputStream(files[i])
                    origin = BufferedInputStream(fi, BUFFER)
                    val entry = ZipEntry(
                        files[i]!!.substring(
                            files[i]!!.lastIndexOf("/") + 1
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