package com.example.cameraappbyshashisingh;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    /*
    *
    *
    *
    *
    * now i have to just make gallery here image is clicked and saved in app data so have me make it avaible in galleryActivity
    *
    *
    *
    * */
    private static final String TAG = "CameraApp";
    private static final int REQUEST_CAMERA_PERMISSION = 1001;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;
    private Camera currentCamera; // Reference to the currently active camera
    private boolean CameraFacing = true; // State variable to track the current facing direction
    // In your activity
    ActivityResultLauncher<Intent> imagePickerLauncher;


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        Button captureButton = findViewById(R.id.save);
        Button cameraFace = findViewById(R.id.cameraToggle);
        Button gallery = findViewById(R.id.galleryGo);

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            initializeCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }

        captureButton.setOnClickListener(v -> capturePhoto());
        cameraFace.setOnClickListener(v -> toggleCamera());
        gallery.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
            startActivity(intent);
        });
    }
    private void initializeCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                androidx.camera.lifecycle.ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(); // Start the initial camera state
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error setting up camera provider", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void toggleCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll(); // Stop all current camera activities
        }

        // Change the camera facing direction state
        CameraFacing = !CameraFacing;

        // Now bind the camera with the new direction
        bindPreview();
    }

    private void bindPreview() {
        if (cameraProvider == null) {
            Log.e(TAG, "Camera provider is not yet initialized.");
            return;
        }

        // Set up the preview and capture options
        Preview preview = new Preview.Builder().build();
        imageCapture = new ImageCapture.Builder().build();

        CameraSelector cameraSelector = CameraFacing
                ? new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
                : new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        currentCamera = cameraProvider.bindToLifecycle(
                (LifecycleOwner) this, cameraSelector, preview, imageCapture);
    }


    private void capturePhoto() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "photo_" + System.currentTimeMillis() + ".jpg");


        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputFileOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "Photo saved: " + photoFile.getAbsolutePath(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Photo capture failed: ", exception);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }

        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }
}
