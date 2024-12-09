package com.example.cameraappbyshashisingh;

import static com.example.cameraappbyshashisingh.MainActivity.MSG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class TextInfoActivity extends AppCompatActivity {
/*
*    This is for normal searches like as if someone needs to search about some basic things using camera like as if on medical prescription
*    it is just for future reference and was one of the direction in which our project could have gone ....
*
* */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_text_info);

        EditText reportDetails = findViewById(R.id.editableText);
        Button searchGoogle = findViewById(R.id.search);
        Button textProcess = findViewById(R.id.textProcess);
        TextView textResult = findViewById(R.id.textInfoTextView);


        String receivedText = getIntent().getStringExtra(MSG);
        reportDetails.setText(receivedText);
//        String finalReceivedText = reportDetails.getText().toString();

        textProcess.setOnClickListener(v -> {
            new Thread(() -> {
                StringBuilder results = new StringBuilder();
                try {
                    // URL to scrape (example: Google search query)
                    String url = "https://www.google.com/search?q=" + "paracetamol".replace(" ", "+");

                    Document doc = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36")
                            .timeout(5000)
                            .get();


                    // Extract data using CSS selectors
                    Elements searchResults = doc.select("span.hgKElc"); // Titles of search results
                    for (int i = 0; i < searchResults.size(); i++) {
                        results.append(searchResults.get(i).text()).append("\n");
                        // Add a delay between requests to avoid being flagged as a bot
                        try {
                            Thread.sleep(2000); // Sleep for 1 second (1000 milliseconds)
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    runOnUiThread(() -> textResult.setText(results.toString()));

                } catch (IOException e) {
                    runOnUiThread(() -> textResult.setText("Please google search it api must have exceeded it's rate limit "));
                    Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
                }
            }).start();
        });



        searchGoogle.setOnClickListener(v -> {
            String query = getNewText(reportDetails);
            if (!query.isEmpty()) {
                // Create an intent to open Google with the search query
                String searchUrl = "https://www.google.com/search?q=" + Uri.encode(query);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl));

                // Open the search URL in the default web browser
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private String getNewText(EditText editText){
        return editText.getText().toString();
    }
}