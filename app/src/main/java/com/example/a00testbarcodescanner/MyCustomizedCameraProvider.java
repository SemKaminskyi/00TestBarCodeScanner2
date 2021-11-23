package com.example.a00testbarcodescanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;

import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraXConfig;
import androidx.camera.lifecycle.ProcessCameraProvider;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class MyCustomizedCameraProvider {

    private static boolean configured = false;

    @SuppressLint("UnsafeOptInUsageError")
    static ListenableFuture<ProcessCameraProvider> getInstance(Context context) {
        synchronized(MyCustomizedCameraProvider.class) {
            if (!configured) {
                configured = true;
                Handler mySchedulerHandler = new Handler();
                Executor myExecutor = Executors.newSingleThreadExecutor();
                ProcessCameraProvider.configureInstance(
                        CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
                                .setCameraExecutor(myExecutor)
                                .setSchedulerHandler(mySchedulerHandler)
                                .build());
            }
        }
        return ProcessCameraProvider.getInstance(context);
    }
}
