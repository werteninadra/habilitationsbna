from flask import Flask, request, jsonify
import pandas as pd
import joblib
import os

app = Flask(__name__)

MODEL_PATH = "model.pkl"
if not os.path.exists(MODEL_PATH):
    raise FileNotFoundError(f"Modèle non trouvé à {MODEL_PATH}")

model = joblib.load(MODEL_PATH)
print("Modèle chargé:", model)

@app.route('/predict', methods=['POST'])
def predict():
    data = request.get_json(force=True)
    if not isinstance(data, dict):
        return jsonify({"error": "Données JSON invalides"}), 400

    historique = data.get("historique", [])
    if not historique:
        return jsonify({"error": "Données 'historique' manquantes"}), 400

    df = pd.DataFrame(historique)

    # Transformation des colonnes
    if 'estFerie' in df.columns:
        df['est_ferie'] = df['estFerie'].astype(int)
        df.drop(columns=['estFerie'], inplace=True)

    if 'meteo' in df.columns:
        meteo_dummies = pd.get_dummies(df['meteo'], prefix='meteo')
        for col in ['meteo_pluie', 'meteo_soleil']:
            if col not in meteo_dummies.columns:
                meteo_dummies[col] = 0
        df = pd.concat([df, meteo_dummies[['meteo_pluie', 'meteo_soleil']]], axis=1)
        df.drop(columns=['meteo'], inplace=True)

    # Renommage
    df.rename(columns={'nombreClients': 'nombre_clients', 'jourSemaine': 'jour_semaine'}, inplace=True)

    expected_columns = ['nombre_clients', 'capacite_max', 'jour_semaine', 'est_ferie', 'meteo_pluie', 'meteo_soleil']

    for col in expected_columns:
        if col not in df.columns:
            df[col] = 0

    df = df[expected_columns]

    # Conversion en int
    for col in ['meteo_pluie', 'meteo_soleil']:
        df[col] = df[col].astype(int)

    try:
        predictions = model.predict(df)
        return jsonify({"prediction": predictions.tolist()})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True, port=5000)
