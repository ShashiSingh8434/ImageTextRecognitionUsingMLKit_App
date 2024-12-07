package com.example.cameraappbyshashisingh;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private ArrayList<String> photoList;
    private GalleryAdapter galleryAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gallery);

        recyclerView = findViewById(R.id.recyclerView);

        // Load images from storage
        photoList = getAllPhotos();

        // Set up RecyclerView with a GridLayoutManager
        galleryAdapter = new GalleryAdapter(photoList, this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(galleryAdapter);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private ArrayList<String> getAllPhotos() {
        ArrayList<String> photos = new ArrayList<>();
        File directory = getExternalFilesDir("/Pictures");

        if (directory != null && directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        photos.add(file.getAbsolutePath());
                    }
                }
            }
        }
        if (photos.isEmpty()) {
            Toast.makeText(this, "No photos found", Toast.LENGTH_SHORT).show();
        }
        return photos;
    }
}