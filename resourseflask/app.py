from flask import Flask, request, jsonify
import pandas as pd
import joblib

# Charger le modèle
model = joblib.load("ressource_model.pkl")

app = Flask(__name__)

@app.route("/prediction", methods=["POST"])
def prediction():
    try:
        data = request.get_json()

        # Vérifier que libelle et typeRessource sont fournis
        if not all(k in data for k in ("libelle", "typeRessource")):
            return jsonify({"error": "Veuillez fournir 'libelle' et 'typeRessource'"}), 400

        # Créer DataFrame avec les données reçues
        df_input = pd.DataFrame([{
            "libelle": data["libelle"],
            "typeRessource": data["typeRessource"]
        }])

        # Prédire
        prediction = model.predict(df_input)[0]

        return jsonify({
            "libelle": data["libelle"],
            "typeRessource": data["typeRessource"],
            "temps_estime_jours": float(prediction)
        })

    except Exception as e:
        return jsonify({"error": str(e)}), 400

if __name__ == "__main__":
    app.run(port=5001, debug=True)
