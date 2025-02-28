from flask import Flask, request, jsonify
import numpy as np
from sklearn.ensemble import IsolationForest

app = Flask(__name__)

# Sample model (train on dummy data)
model = IsolationForest(contamination=0.05, random_state=42)
sample_data = np.array([[100], [102], [101], [105], [500]])  # Fake dataset
model.fit(sample_data)

@app.route('/detect', methods=['POST'])
def detect():
    data = request.get_json()
    price = float(data.get("1. open", 0))  # Extract stock price
    prediction = model.predict([[price]])  # Predict anomaly

    return jsonify({"anomaly": prediction[0] == -1})

if __name__ == '__main__':
    app.run(port=5000)
