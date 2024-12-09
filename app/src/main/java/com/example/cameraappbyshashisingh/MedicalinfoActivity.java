package com.example.cameraappbyshashisingh;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MedicalinfoActivity extends AppCompatActivity {
/*
*         This is just specifically made for just Lab blood test report
*
* */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medicalinfo);

        EditText reportDetails = findViewById(R.id.infotext);
        Button process = findViewById(R.id.process);

        String recognizedText = getIntent().getStringExtra(MainActivity.MSG);
        reportDetails.setText(recognizedText);

//        recognizedText = reportDetails.getText().toString();

        process.setOnClickListener(v -> {
            String finalRecognizedText = reportDetails.getText().toString();

            // Analyze the text and display results
            String analysisResult = analyzeHealthData(finalRecognizedText);
            reportDetails.setText(analysisResult);
        });




        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Analyze the text and parse common health report details.
     */
    private String analyzeHealthData(String text) {
        StringBuilder result = new StringBuilder();

        // Parse Hemoglobin
        String hbLevelStr = extractValue(text, "hemoglobin|hb");
        if (!hbLevelStr.equals("Not Found")) {
            float hbLevel = Float.parseFloat(hbLevelStr);
            result.append("Hemoglobin: ").append(hbLevel).append("\n");
            result.append(getHemoglobinStatus(hbLevel)).append("\n");
        }

        // Parse RBC
        String rbcLevelStr = extractValue(text, "rbc|red blood cell");
        if (!rbcLevelStr.equals("Not Found")) {
            float rbcLevel = Float.parseFloat(rbcLevelStr);
            result.append("RBC Count: ").append(rbcLevel).append("\n");
            result.append(getRbcStatus(rbcLevel)).append("\n");
        }

        // Parse Cholesterol
        String cholesterolStr = extractValue(text, "cholesterol");
        if (!cholesterolStr.equals("Not Found")) {
            float cholesterol = Float.parseFloat(cholesterolStr);
            result.append("Cholesterol: ").append(cholesterol).append("\n");
            result.append(getCholesterolStatus(cholesterol)).append("\n");
        }

        // Parse Blood Sugar
        String bloodSugarStr = extractValue(text, "blood sugar");
        if (!bloodSugarStr.equals("Not Found")) {
            float bloodSugar = Float.parseFloat(bloodSugarStr);
            result.append("Blood Sugar: ").append(bloodSugar).append("\n");
            result.append(getBloodSugarStatus(bloodSugar)).append("\n");
        }

        if (result.length() == 0) {
            return "No health-related information found.";
        }

        return result.toString();
    }

    /**
     * Analyze hemoglobin levels to determine health status.
     */
    private String getHemoglobinStatus(float hb) {
        if (hb < 12) {
            return "Hemoglobin is low: Possible Anemia.";
        } else if (hb > 18) {
            return "Hemoglobin is high: Could indicate dehydration.";
        }
        return "Hemoglobin is normal.";
    }

    /**
     * Analyze RBC levels to determine health status.
     */
    private String getRbcStatus(float rbc) {
        if (rbc < 4.1) {
            return "RBC count is low: Possible anemia.";
        } else if (rbc > 5.5) {
            return "RBC count is high: May suggest a medical condition.";
        }
        return "RBC count is normal.";
    }

    /**
     * Analyze cholesterol levels to determine health status.
     */
    private String getCholesterolStatus(float cholesterol) {
        if (cholesterol < 200) {
            return "Cholesterol levels are optimal.";
        } else if (cholesterol >= 200 && cholesterol < 240) {
            return "Cholesterol levels are borderline high.";
        } else {
            return "Cholesterol is high: Risk of cardiovascular disease.";
        }
    }

    /**
     * Analyze blood sugar levels to determine health status.
     */
    private String getBloodSugarStatus(float bloodSugar) {
        if (bloodSugar < 70) {
            return "Blood sugar is low: Risk of hypoglycemia.";
        } else if (bloodSugar > 140) {
            return "Blood sugar is high: Risk of diabetes.";
        }
        return "Blood sugar is within a normal range.";
    }
    /**
     * Extract numerical values using regex pattern.
     */
    private String extractValue(String text, String patternString) {
        Pattern pattern = Pattern.compile(patternString + "\\s*[:\\s]*([0-9]+\\.?[0-9]*)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return "Not Found";
    }
}