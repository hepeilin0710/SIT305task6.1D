package com.example.a61d;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.*;

public class QuizActivity extends AppCompatActivity {

    LinearLayout quizContainer;
    Button submitButton;
    ProgressDialog progressDialog;

    ArrayList<String> userAnswers = new ArrayList<>();
    ArrayList<String> correctAnswers = new ArrayList<>();
    ArrayList<String> insights = new ArrayList<>();

    OkHttpClient client;

    String topic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        topic = getIntent().getStringExtra("topic");  // 从 intent 传入的主题

        quizContainer = findViewById(R.id.quizContainer);
        submitButton = findViewById(R.id.buttonSubmit);

        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .writeTimeout(180, TimeUnit.SECONDS)
                .build();

        // 显示加载框
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading quiz...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        fetchQuizData(topic);

        submitButton.setOnClickListener(v -> {
            userAnswers.clear();

            for (int i = 0; i < quizContainer.getChildCount(); i++) {
                View block = quizContainer.getChildAt(i);
                RadioGroup group = block.findViewById(R.id.optionGroup);
                if (group == null) continue;

                int checkedId = group.getCheckedRadioButtonId();
                if (checkedId == -1) {
                    Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show();
                    return;
                }

                RadioButton selected = block.findViewById(checkedId);
                userAnswers.add(selected.getText().toString());
            }

            // 跳转至结果页
            Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
            intent.putStringArrayListExtra("userAnswers", userAnswers);
            intent.putStringArrayListExtra("correctAnswers", correctAnswers);
            intent.putStringArrayListExtra("insights", insights);
            intent.putExtra("username", getIntent().getStringExtra("username"));
            intent.putStringArrayListExtra("interests", getIntent().getStringArrayListExtra("interests"));
            startActivity(intent);
        });
    }

    private void fetchQuizData(String topic) {
        String url = "http://192.168.0.78:5000/getQuiz?topic=" + topic.replace(" ", "%20");

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(QuizActivity.this, "Failed to fetch quiz", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    buildQuizUI(json);
                });
            }
        });
    }

    private void buildQuizUI(String json) {
        try {
            JSONObject root = new JSONObject(json);
            JSONArray quizArray = root.getJSONArray("quiz");

            for (int i = 0; i < quizArray.length(); i++) {
                JSONObject q = quizArray.getJSONObject(i);
                String question = q.getString("question");
                JSONArray options = q.getJSONArray("options");
                String correct = q.getString("correct_answer");
                String insight = q.getString("insight");

                correctAnswers.add(getOptionText(options, correct));
                insights.add(insight);

                View block = getLayoutInflater().inflate(R.layout.quiz_item, quizContainer, false);

                TextView questionText = block.findViewById(R.id.textQuestion);
                questionText.setText("Q" + (i + 1) + ". " + question);

                RadioGroup group = block.findViewById(R.id.optionGroup);
                for (int j = 0; j < options.length(); j++) {
                    RadioButton btn = new RadioButton(this);
                    btn.setText(options.getString(j));
                    group.addView(btn);
                }

                quizContainer.addView(block);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to parse quiz", Toast.LENGTH_SHORT).show();
        }
    }

    private String getOptionText(JSONArray options, String correctAnswerRaw) {
        // 统一处理：**OPTION B** → B → index → text
        String letter = correctAnswerRaw.toUpperCase().replace("OPTION", "").replace("*", "").replace("(", "").replace(")", "").trim();
        int index = -1;
        switch (letter) {
            case "A": index = 0; break;
            case "B": index = 1; break;
            case "C": index = 2; break;
            case "D": index = 3; break;
        }
        try {
            return index >= 0 ? options.getString(index) : "";
        } catch (Exception e) {
            return "";
        }
    }
}

