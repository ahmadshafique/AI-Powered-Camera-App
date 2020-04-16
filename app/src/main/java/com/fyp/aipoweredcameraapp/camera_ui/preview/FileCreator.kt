package com.fyp.aipoweredcameraapp.camera_ui.preview

object FileCreator {

    const val JPEG_FORMAT = ".jpg"

    fun createTempFile(fileFormat: String) =
        createTempFile(System.currentTimeMillis().toString(), fileFormat)
}