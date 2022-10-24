package com.projet.sluca.smallbrother.libs

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.activation.DataSource

class ByteArrayDataSource(val data: ByteArray, val type: String?) : DataSource {

    override fun getContentType(): String = type ?: "application/octet-stream"

    @Throws(IOException::class)
    override fun getInputStream(): InputStream = ByteArrayInputStream(data)

    override fun getName(): String = "ByteArrayDataSource"

    @Throws(IOException::class)
    override fun getOutputStream(): OutputStream {
        throw IOException("Not Supported")
    }
}