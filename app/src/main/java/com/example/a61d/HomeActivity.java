package com.example.a61d;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.ArrayList;
import java.util.Random;

public class HomeActivity extends AppCompatActivity {

    String username;
    ArrayList<String> userInterests;
    CardView task1Card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        username = getIntent().getStringExtra("username");

        // ✅ 从数据库中读取兴趣列表
        DBHelper dbHelper = new DBHelper(this);
        String rawInterest = dbHelper.getUserInterests(username);
        userInterests = new ArrayList<>();

        if (rawInterest != null && !rawInterest.isEmpty()) {
            String[] split = rawInterest.split(",");
            for (String item : split) {
                userInterests.add(item.trim());
            }
        }

        TextView welcomeText = findViewById(R.id.textWelcome);
        welcomeText.setText("Welcome, " + username + "!");

        task1Card = findViewById(R.id.cardTask1);

        task1Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedTopic = "math"; // fallback

                if (!userInterests.isEmpty()) {
                    Random random = new Random();
                    int randomIndex = random.nextInt(userInterests.size());
                    selectedTopic = userInterests.get(randomIndex);
                }

                Intent intent = new Intent(HomeActivity.this, QuizActivity.class);
                intent.putExtra("topic", selectedTopic);
                intent.putExtra("username", username);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
    }

}
