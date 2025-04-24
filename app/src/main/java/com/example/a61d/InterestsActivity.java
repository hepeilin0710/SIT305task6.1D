package com.example.a61d;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class InterestsActivity extends AppCompatActivity {

    String username;
    Button buttonNext;
    ArrayList<String> selectedInterests = new ArrayList<>();
    String[] interests = {
            "Artificial Intelligence", "Machine Learning", "Data Structures", "Web Development",
            "Mobile Apps", "Cyber Security", "Cloud Computing", "Networking",
            "UI/UX Design", "Game Development", "Databases", "Big Data"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);

        username = getIntent().getStringExtra("username");
        buttonNext = findViewById(R.id.buttonNext);

        LinearLayout layout = findViewById(R.id.interestContainer);

        for (String topic : interests) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(topic);
            checkBox.setTextSize(16);
            checkBox.setPadding(8, 8, 8, 8);

            checkBox.setOnClickListener(v -> {
                if (((CheckBox) v).isChecked()) {
                    if (selectedInterests.size() >= 10) {
                        ((CheckBox) v).setChecked(false);
                        Toast.makeText(this, "You can select up to 10 interests", Toast.LENGTH_SHORT).show();
                    } else {
                        selectedInterests.add(((CheckBox) v).getText().toString());
                    }
                } else {
                    selectedInterests.remove(((CheckBox) v).getText().toString());
                }
            });

            layout.addView(checkBox);
        }

        buttonNext.setOnClickListener(v -> {
            if (selectedInterests.isEmpty()) {
                Toast.makeText(this, "Please select at least one interest", Toast.LENGTH_SHORT).show();
            } else {
                DBHelper dbHelper = new DBHelper(this);
                boolean success = dbHelper.updateUserInterests(username, selectedInterests);

                if (success) {
                    Toast.makeText(this, "Interests saved successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(InterestsActivity.this, HomeActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    finish();
                } else {
                    Toast.makeText(this, "Failed to save interests", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

