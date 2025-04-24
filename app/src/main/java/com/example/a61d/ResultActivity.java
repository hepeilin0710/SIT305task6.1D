package com.example.a61d;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

    LinearLayout resultContainer;
    Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        resultContainer = findViewById(R.id.resultContainer);
        continueButton = findViewById(R.id.buttonContinue);

        ArrayList<String> userAnswers = getIntent().getStringArrayListExtra("userAnswers");
        ArrayList<String> correctAnswers = getIntent().getStringArrayListExtra("correctAnswers");
        ArrayList<String> insights = getIntent().getStringArrayListExtra("insights");

        int score = 0;

        for (int i = 0; i < correctAnswers.size(); i++) {
            String user = userAnswers.get(i);
            String correct = correctAnswers.get(i);
            String insight = insights.get(i);

            if (user.equals(correct)) score++;

            View block = getLayoutInflater().inflate(R.layout.result_item, resultContainer, false);

            TextView title = block.findViewById(R.id.textTitle);
            TextView correctText = block.findViewById(R.id.textCorrectAnswer);
            TextView insightText = block.findViewById(R.id.textInsight);

            title.setText("Question " + (i + 1));
            correctText.setText("✅ Correct Answer: " + correct);
            insightText.setText(insight);

            resultContainer.addView(block);
        }

        Toast.makeText(this, "Your score: " + score + "/" + correctAnswers.size(), Toast.LENGTH_SHORT).show();

        continueButton.setOnClickListener(v -> {

            Intent intent = new Intent(ResultActivity.this, HomeActivity.class);
            intent.putExtra("username", getIntent().getStringExtra("username"));  // ✅ 传回原来的用户名
            startActivity(intent);
            finish();
        });
    }
}
