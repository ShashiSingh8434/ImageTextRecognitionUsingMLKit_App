package com.example.cameraappbyshashisingh;

import static android.content.Intent.getIntent;
import static com.example.cameraappbyshashisingh.MainActivity.MSG;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class TextInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_text_info);


        // Get the text passed from the first Activity
        String receivedText = getIntent().getStringExtra(MSG);

        // Example: Set the received text to a TextView
        TextView textView = findViewById(R.id.textView);
        textView.setText(receivedText);
        receivedText = receivedText +" common uses ";

        // Start scraping in a background thread
        String finalReceivedText = receivedText;
        new Thread(() -> {
            StringBuilder results = new StringBuilder();
            try {
                // URL to scrape (example: Google search query)
                String url = "https://www.google.com/search?q=" + finalReceivedText.replace(" ", "+");

                // Fetch the HTML document
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
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


                // Update the TextView on the main thread
                runOnUiThread(() -> textView.setText(results.toString()));

            } catch (IOException e) {
                runOnUiThread(() -> textView.setText("Error: " + e.getMessage()));
            }
        }).start();



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}