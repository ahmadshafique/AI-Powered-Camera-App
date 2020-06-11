package com.fyp.aipoweredcameraapp.camera_ui.preview

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.TextureViewMeteringPointFactory
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.fyp.aipoweredcameraapp.ActivityImage
import com.google.common.util.concurrent.ListenableFuture
import com.fyp.aipoweredcameraapp.R
import com.fyp.aipoweredcameraapp.camera_ui.preview.FileCreator.JPEG_FORMAT
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.File
import java.util.concurrent.Executors
import kotlin.math.abs
import androidx.camera.core.ImageCapture


class CameraFragment : Fragment() {

    private lateinit var processCameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var processCameraProvider: ProcessCameraProvider
    private var lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
    private var x1: Float = 0.0F
    private var x2: Float = 0.0F
    private var deltaX: Float = 0.0F
    private val MinDistance = 150

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        processCameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        processCameraProviderFuture.addListener(Runnable {
            processCameraProvider = processCameraProviderFuture.get()
            viewFinder.post { setupCamera() }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::processCameraProvider.isInitialized) {
            processCameraProvider.unbindAll()
        }
    }

    private fun setupCamera() {
        processCameraProvider.unbindAll()
        val camera = processCameraProvider.bindToLifecycle(
                this,
                lensFacing,
                buildPreviewUseCase(),
                buildImageCaptureUseCase())//,
                //buildImageAnalysisUseCase())
        setupTapFunctions(camera.cameraControl)
    }

    private fun buildPreviewUseCase(): Preview {
        val display = viewFinder.display
        val metrics = DisplayMetrics().also { display.getMetrics(it) }
        val preview = Preview.Builder()
                .setTargetRotation(display.rotation)
                .setTargetResolution(Size(metrics.widthPixels, metrics.heightPixels))
                .build()
                .apply {
                    previewSurfaceProvider = viewFinder.previewSurfaceProvider
                }
        preview.previewSurfaceProvider = viewFinder.previewSurfaceProvider
        return preview
    }

    private fun buildImageCaptureUseCase(): ImageCapture {
        val display = viewFinder.display
        val metrics = DisplayMetrics().also { display.getMetrics(it) }
        val capture = ImageCapture.Builder()
                //.setTargetRotation(display.rotation)
                //.setTargetResolution(Size(1000, 1000))
                .setTargetResolution(Size(metrics.widthPixels, metrics.heightPixels))
                .setFlashMode(ImageCapture.FLASH_MODE_OFF)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()

        val executor = Executors.newSingleThreadExecutor()
        cameraCaptureImageButton.setOnClickListener {

            Toast.makeText(context, "Capturing image...keep camera steady !!", Toast.LENGTH_LONG).show();

            // Setup image capture metadata
            val metadata = ImageCapture.Metadata().apply {
                // Mirror image when using the front camera
                isReversedHorizontal = lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA
            }

            capture.takePicture(
                    FileCreator.createTempFile(JPEG_FORMAT),
                    metadata,
                    executor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(file: File) {

                            val intent = Intent(requireContext(), ActivityImage::class.java)
                            intent.putExtra("image_source", "camera")
                            intent.putExtra("filePath", file.absolutePath)
                            startActivity(intent)

                        }

                        override fun onError(imageCaptureError: Int, message: String, cause: Throwable?) {
                            //activity!!.runOnUiThread {
                                Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_LONG).show()
                            //}
                            Log.e("CameraFragment", "Capture error $imageCaptureError: $message", cause)
                        }
                    })
        }
        return capture
    }

    /*
    private fun buildImageAnalysisUseCase(): ImageAnalysis {
        val display = viewFinder.display
        val metrics = DisplayMetrics().also { display.getMetrics(it) }
        val analysis = ImageAnalysis.Builder()
                .setTargetRotation(display.rotation)
                .setTargetResolution(Size(metrics.widthPixels, metrics.heightPixels))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
                .setImageQueueDepth(10)
                .build()
        analysis.setAnalyzer(
                Executors.newSingleThreadExecutor(),
                ImageAnalysis.Analyzer { imageProxy ->
                    Log.d("CameraFragment", "Image analysis result $imageProxy")
                    imageProxy.close()
                })
        return analysis
    }
     */

    private fun setupTapFunctions(cameraControl: CameraControl) {
        viewFinder.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> { x1 = event.x }
                MotionEvent.ACTION_UP -> {
                    x2 = event.x
                    deltaX = abs(x2 - x1);

                    // flip camera tap
                    if (deltaX > MinDistance) {
                        // Left to Right swipe action or Right to left swipe action
                        if (x2 > x1 || x2 < x1) {
                            flipCamera()
                            setupCamera()
                            return@setOnTouchListener true
                        }
                    }
                    // focus tap
                    else {
                        val textureView = viewFinder.getChildAt(0) as? TextureView
                                ?: return@setOnTouchListener true
                        val factory = TextureViewMeteringPointFactory(textureView)

                        val point = factory.createPoint(event.x, event.y)
                        val action = FocusMeteringAction.Builder.from(point).build()
                        cameraControl.startFocusAndMetering(action)
                        return@setOnTouchListener true
                    }
                }
            }
            return@setOnTouchListener true
        }
    }

    private fun flipCamera() {
        if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA)
            lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA
        else
            lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
    }

}