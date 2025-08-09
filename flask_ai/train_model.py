import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
import joblib

# Charger les données (ton fichier CSV)
df = pd.read_csv("data/his.csv", sep=";")

# Création de la cible : plein = 1 si nombre_clients >= capacite_max * 0.9
df["plein"] = (df["nombre_clients"] >= df["capacite_max"] * 0.9).astype(int)

# Encodage one-hot météo
df = pd.get_dummies(df, columns=["meteo"], drop_first=True)

# Sélection des features
X = df.drop(columns=["plein", "date", "agence_id"])
y = df["plein"]

# Séparation train/test
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# Entraînement du modèle
model = RandomForestClassifier(n_estimators=100, random_state=42)
model.fit(X_train, y_train)

# Sauvegarde
joblib.dump(model, "model.pkl")
print("✅ Modèle entraîné et sauvegardé dans model.pkl")
