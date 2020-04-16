package com.fyp.aipoweredcameraapp

import android.app.Application
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig


class CameraXApp : Application(), CameraXConfig.Provider {

    override fun getCameraXConfig() = Camera2Config.defaultConfig()
}