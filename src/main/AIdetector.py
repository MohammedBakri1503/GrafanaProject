from flask import Flask, request, jsonify
import numpy as np
from sklearn.ensemble import IsolationForest

app = Flask(__name__)

# Sample training data (simulating normal price ranges)
sample_data = np.array([
    [230], [235], [240], [245], [250],  # Normal stock prices
    [1000]  # Extreme anomaly (outlier)
])

# Train Isolation Forest model
model = IsolationForest(contamination=0.05, random_state=42)
model.fit(sample_data)

@app.route('/detect', methods=['POST'])
def detect():
    try:
        data = request.get_json()
        if not data or "close" not in data:
            return jsonify({"error": "Invalid request, 'close' price missing"}), 400

        price = float(data["close"])  # Extract stock closing price
        prediction = model.predict([[price]])  # Predict anomaly

        # Convert to Python bool (True if anomaly, False if normal)
        is_anomaly = bool(prediction[0] == -1)

        return jsonify({"anomaly": is_anomaly})
    
    except Exception as e:
        return jsonify({"error": str(e)}), 500  # Ensure JSON response even on errors

if __name__ == '__main__':
    app.run(port=5000, debug=True)
