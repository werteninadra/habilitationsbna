# train_model.py
import pandas as pd
import joblib
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.preprocessing import OneHotEncoder
from sklearn.ensemble import RandomForestRegressor
from sklearn.pipeline import Pipeline
from sklearn.compose import ColumnTransformer
from sklearn.model_selection import train_test_split

# Charger dataset
df = pd.read_csv("ress.csv", sep=";", encoding="latin-1")

# Features et target
X = df[["libelle", "typeRessource"]]
y = df["temps_estime_jours"]

# Prétraitement
preprocessor = ColumnTransformer(
    transformers=[
        ("libelle", TfidfVectorizer(), "libelle"),
        ("type", OneHotEncoder(handle_unknown="ignore"), ["typeRessource"])
    ]
)

# Modèle
model = Pipeline([
    ("preprocessor", preprocessor),
    ("regressor", RandomForestRegressor(n_estimators=100, random_state=42))
])

# Train/test split
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
model.fit(X_train, y_train)

# Sauvegarder le modèle
joblib.dump(model, "ressource_model.pkl")
print("✅ Modèle entraîné et sauvegardé dans ressource_model.pkl")
