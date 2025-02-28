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
    try:
        data = request.get_json()
        if not data or "1. open" not in data:
            return jsonify({"error": "Invalid request, '1. open' missing"}), 400

        price = float(data["1. open"])  # Extract stock price
        prediction = model.predict([[price]])  # Predict anomaly

        return jsonify({"anomaly": bool(prediction[0] == -1)})  # Convert to Python bool
    except Exception as e:
        return jsonify({"error": str(e)}), 500  # Ensure JSON response even on errors


if __name__ == '__main__':
    app.run(port=5000)
