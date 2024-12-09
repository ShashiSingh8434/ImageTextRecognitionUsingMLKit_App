package com.example.cameraappbyshashisingh;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ActivityResultLauncher<Intent> galleryLauncher;

    private static final String TAG = "CameraApp";
    private static final int REQUEST_CAMERA_PERMISSION = 1001;
    private static final int PERMISSION_REQUEST_CODE = 100;
    public static final String MSG = "com.example.cameraappbyshashisingh.recognizedTextValueSendingToTextInfoActivity";
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;
    private Camera currentCamera;
    private boolean CameraFacing = true;

    private boolean toMedicalInfo = false;
    private boolean isChecked = false;


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        Button captureButton = findViewById(R.id.save);
        Button cameraFace = findViewById(R.id.cameraToggle);


        Button buttonPickImage = findViewById(R.id.infoSend);
//        CheckBox checkBox = findViewById(R.id.checkBox);

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        if (imageUri != null) {
                            File file = getFileFromUri(imageUri);
                            if (file != null) {
                                processImage(file);
                            }
                        }
                    }
                });
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
        buttonPickImage.setOnClickListener(v -> openGallery());
//        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            MainActivity.this.isChecked = isChecked;
//            if (isChecked) {
//                toMedicalInfo = true;
//            }else {
//                toMedicalInfo = false;
//            }
//        });
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }
    private File getFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File file = new File(getCacheDir(), "selected_image");
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fos.close();
            inputStream.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    private void processImage(File file) {
        recognizeText(file);
        Toast.makeText(this, "Image file: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
    }
    private void initializeCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                androidx.camera.lifecycle.ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error setting up camera provider", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void toggleCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }

        CameraFacing = !CameraFacing;
        bindPreview();
    }

    private void bindPreview() {
        if (cameraProvider == null) {
            Log.e(TAG, "Camera provider is not yet initialized.");
            return;
        }

        Preview preview = new Preview.Builder().build();
        imageCapture = new ImageCapture.Builder().build();

        CameraSelector cameraSelector = CameraFacing
                ? new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
                : new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        currentCamera = cameraProvider.bindToLifecycle(
                (LifecycleOwner) this, cameraSelector, preview, imageCapture);
    }

    private String extractAlphanumeric(String text) {
        return text.replaceAll("[^a-zA-Z0-9 ]", "");

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
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this,"Photo saved: " + photoFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    recognizeText(photoFile);
                });

            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Photo capture failed: ", exception);
            }
        });

    }

    private void recognizeText(File photoFile) {
        try {
            InputImage image = InputImage.fromFilePath(this, Uri.fromFile(photoFile));

            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            // Process the image
            recognizer.process(image)
                    .addOnSuccessListener(this::handleRecognizedText)
                    .addOnFailureListener(e -> Log.e(TAG, "Text recognition failed", e));



        } catch (IOException e) {
            Toast.makeText(this, "Error reading text", Toast.LENGTH_SHORT).show();
        }
    }
    private void handleRecognizedText(Text result) {
        StringBuilder reconstructedText = new StringBuilder();

        // Process each block and line of text
        for (Text.TextBlock block : result.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                // Append each line with a newline character
                reconstructedText.append(line.getText().trim()).append("  \n");
            }
        }

        // Convert the reconstructed text to a string
        String finalText = reconstructedText.toString();

        // Extract only alphanumeric characters (if required)
        finalText = extractAlphanumeric(finalText);

        if (!finalText.isEmpty()) {
            Intent intent;
            if (toMedicalInfo) {
                intent = new Intent(MainActivity.this, MedicalinfoActivity.class);
            } else {
                intent = new Intent(MainActivity.this, TextInfoActivity.class);
            }
            intent.putExtra(MSG, finalText); // Add the text
            startActivity(intent);

            Toast.makeText(this, "Text processed and passed to the next activity", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No text detected in the image", Toast.LENGTH_SHORT).show();
        }
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
