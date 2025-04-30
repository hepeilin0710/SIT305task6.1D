import os
from flask import Flask, request, jsonify
import re
from transformers import AutoTokenizer, AutoModelForCausalLM
import torch

app = Flask(__name__)

# Load the model and tokenizer
# MODEL = "meta-llama/Llama-3.2-1B"
MODEL = "google/gemma-3-1b-it"
tokenizer = AutoTokenizer.from_pretrained(MODEL)
model = AutoModelForCausalLM.from_pretrained(MODEL)

# Determine device (GPU if available, else CPU)
device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")
model.to(device)  # Move model to the appropriate device
print(f"Using device: {device}")

def fetchQuizFromLlama(student_topic):
    print(f"Generating quiz for topic: {student_topic} using {MODEL}")
    prompt = (
        f"You are a helpful tutor. Please generate a quiz with 3 questions to test students on the provided topic. "
        f"For each question, generate 4 options where only one of the options is correct. "
        f"After the answer, write one paragraph that explains why the answer is correct and gives a short learning suggestion to help the student better understand the topic.\n\n"
        f"Format your response as follows:\n"
        f"**QUESTION 1:** [Your question here]?\n"
        f"**OPTION A:** [First option]\n"
        f"**OPTION B:** [Second option]\n"
        f"**OPTION C:** [Third option]\n"
        f"**OPTION D:** [Fourth option]\n"
        f"**ANS:** [Correct answer letter]\n"
        f"**INSIGHT:** [One paragraph combining explanation and tip]\n\n"
        f"**QUESTION 2:** [Your question here]?\n"
        f"**OPTION A:** [First option]\n"
        f"**OPTION B:** [Second option]\n"
        f"**OPTION C:** [Third option]\n"
        f"**OPTION D:** [Fourth option]\n"
        f"**ANS:** [Correct answer letter]\n"
        f"**INSIGHT:** [One paragraph combining explanation and tip]\n\n"
        f"**QUESTION 3:** [Your question here]?\n"
        f"**OPTION A:** [First option]\n"
        f"**OPTION B:** [Second option]\n"
        f"**OPTION C:** [Third option]\n"
        f"**OPTION D:** [Fourth option]\n"
        f"**ANS:** [Correct answer letter]\n"
        f"**INSIGHT:** [One paragraph combining explanation and tip]\n\n"
        f"Ensure text is properly formatted. It needs to start with a question, then the options, and finally the correct answer. "
        f"Follow this pattern for all questions. "
        f"Here is the student topic:\n{student_topic}"
    )

    try:
        # Tokenize the input prompt
        inputs = tokenizer(prompt, return_tensors="pt").to(device)  # Move to the same device as model

        # Generate text
        outputs = model.generate(
            **inputs,
            max_new_tokens=500,  # Maximum number of new tokens to generate
            temperature=0.7,     # Control randomness
            top_p=0.9,           # Nucleus sampling
            do_sample=True,      # Enable sampling for diversity
            pad_token_id=tokenizer.eos_token_id  # Handle padding (optional, avoids warning)
        )

        # Decode the generated tokens
        generated_text = tokenizer.decode(outputs[0], skip_special_tokens=True)

        # Find all appearances of "**QUESTION 1:**"
        quiz_start = generated_text.find("**QUESTION 1:**")
        if quiz_start == -1:
            raise Exception("Failed to generate a properly formatted quiz")
        quiz_text = generated_text[quiz_start:]
        return quiz_text
    except Exception as e:
        raise Exception(f"Failed to generate quiz with model: {str(e)}")
  
    

def process_quiz(quiz_text):
    questions = []
    pattern = re.compile(
        r'\*\*QUESTION \d+:\*\* (.+?)\n'
        r'\*\*OPTION A:\*\* (.+?)\n'
        r'\*\*OPTION B:\*\* (.+?)\n'
        r'\*\*OPTION C:\*\* (.+?)\n'
        r'\*\*OPTION D:\*\* (.+?)\n'
        r'\*\*ANS:\*\* (.+?)\n'
        r'\*\*INSIGHT:\*\* (.+?)(?=\n\*\*QUESTION|\Z)',
        re.DOTALL
    )
    matches = pattern.findall(quiz_text)

    for match in matches:
        question = match[0].strip()
        options = [match[1].strip(), match[2].strip(), match[3].strip(), match[4].strip()]
        correct_ans = match[5].strip()
        insight = match[6].strip()

       
        if "[Your question here]" in question or "[First option]" in options[0]:
            continue

        question_data = {
            "question": question,
            "options": options,
            "correct_answer": correct_ans,
            "insight": insight
        }
        questions.append(question_data)

    
    return questions[:3]


@app.route('/getQuiz', methods=['GET'])
def get_quiz():
    print("Request received")
    student_topic = request.args.get('topic')
    if not student_topic:
        return jsonify({'error': 'Missing topic parameter'}), 400
    try:
        quiz = fetchQuizFromLlama(student_topic)
        print(quiz)
        processed_quiz = process_quiz(quiz)
        if not processed_quiz:
            return jsonify({'error': 'Failed to parse quiz data', 'raw_response': quiz}), 500
        return jsonify({'quiz': processed_quiz}), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/test', methods=['GET'])
def run_test():
    return jsonify({'quiz': "test"}), 200

#if __name__ == '__main__':
    #port_num = 5000
    #print(f"App running on port {port_num}")
    #app.run(port=port_num, host="0.0.0.0")
if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000, debug=True)


