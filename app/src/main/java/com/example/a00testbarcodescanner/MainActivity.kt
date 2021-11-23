package com.example.a00testbarcodescanner

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView
    private lateinit var analizer: MyImageAnalyzer
    lateinit var tv: TextView
    companion object{
        val _text = MutableLiveData<String>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv = findViewById(R.id.tv)

        previewView = findViewById(R.id.previw)
        this.window.setFlags(1024, 1024)

        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this.applicationContext)

        analizer = MyImageAnalyzer(supportFragmentManager, this)

        cameraProviderFuture.addListener(Runnable {
            try {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)!=(PackageManager.PERMISSION_GRANTED)){
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA),101)
                }else {

                    val processCameraProvider = cameraProviderFuture.get() as ProcessCameraProvider
                    bindPreview(processCameraProvider)
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))

        val ld :LiveData<String> = _text
        ld.observe(this, Observer {
            tv.text = it
        })

    }

    private fun bindPreview(processCameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder().build()

        val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        preview.setSurfaceProvider(previewView.surfaceProvider)
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1024, 1024))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(cameraExecutor, analizer)
        processCameraProvider.unbindAll()

        processCameraProvider.bindToLifecycle(this,cameraSelector, preview)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty()){
            var processCameraProvider: ProcessCameraProvider? = null
            try {
            processCameraProvider = cameraProviderFuture.get() as ProcessCameraProvider
            }catch (e: Exception){
                e.printStackTrace()
            }
            processCameraProvider?.let { bindPreview(it) }
        }
    }

    private class MyImageAnalyzer(private val fragmentManager: FragmentManager,private val context: Context) : ImageAnalysis.Analyzer {
        private val bg = BottomDialog()

        @SuppressLint("UnsafeOptInUsageError")
        override fun analyze(imageProxy: ImageProxy) {
            makeToas("start Analyze")
        bg.show(fragmentManager, "")
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                        Barcode.FORMAT_QR_CODE,
                        Barcode.FORMAT_AZTEC)
                    .build()

                val scanner = BarcodeScanning.getClient(options)

                val result = scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                       readerBarCodeData(barcodes)
                    }
                    .addOnFailureListener {
                        // Task failed with an exception
                        // ...
                    }
                    .addOnCompleteListener{
                        imageProxy.close()
                    }


            }
        }

        private fun readerBarCodeData(barcodes: List<Barcode>) {
            for (barcode in barcodes) {
                makeToas("barcode in barcodes")
                val bounds = barcode.boundingBox
                val corners = barcode.cornerPoints

                val rawValue = barcode.rawValue

                val valueType = barcode.valueType
                // See API reference for complete list of supported types
                when (valueType) {
                    Barcode.TYPE_WIFI -> {
                        Log.e("BARCODE", "wifi")
                        makeToas("wifi")
                        val ssid = barcode.wifi!!.ssid
                        val password = barcode.wifi!!.password
                        val type = barcode.wifi!!.encryptionType
                    }
                    Barcode.TYPE_URL -> {
                        Log.e("BARCODE", "url")
                        makeToas("url")
                        if(!bg.isAdded){
                            bg.show(fragmentManager, "")
                        }
                        val title = barcode.url!!.title
                        val url = barcode.url!!.url
                        bg.fetchUrl(url)

                    }
                    Barcode.TYPE_TEXT ->{
                        Log.e("BARCODE", "text")
                        makeToas("text")
                        Companion._text.postValue(barcode.toString())
                    }
                }
            }
        }


    private fun makeToas(str:String){
        Toast.makeText(context,str,Toast.LENGTH_SHORT)
    }
    }
}