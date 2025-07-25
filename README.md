# 📌 Habilitation BNA

Application web de gestion des habilitations pour la **Banque Nationale Agricole (BNA)**.  
Elle permet de gérer les utilisateurs, leurs rôles et les autorisations via une interface sécurisée basée sur **Keycloak**.

---

## 🛠️ Technologies utilisées

- **Backend** : Spring Boot (Java 17+)
- **Frontend** : Angular
- **Sécurité** : Keycloak (authentification & gestion des rôles)
- **Base de données** : MySQL
- **ORM** : Spring Data JPA / Hibernate

---

## 🔐 Fonctionnalités principales

- 🔑 Authentification centralisée via **Keycloak**
- 👥 Gestion des utilisateurs (CRUD)
- 🛡️ Gestion des rôles et autorisations
- ⚙️ Attribution automatique des rôles selon profil
- 📊 Dashboard personnalisé selon les rôles :
  - **Scrum Master** : gestion complète
  - **Product Owner** : vue projet étendue
  - **Développeur** : accès limité aux tâches/messages

---

## ⚙️ Configuration Keycloak (extrait)

1. Créer un **realm** : `habilitation-bna`
2. Créer un **client** : `habilitation-client`
   - Type : confidential / public
   - Redirect URI : `http://localhost:8080/*`
3. Définir les **rôles** :
   - `ROLE_ADMIN`
   - `ROLE_USER`
   - `ROLE_SCRUM_MASTER`
4. Ajouter les **utilisateurs** et assigner les rôles

---

## 🚀 Lancer l’application localement

### 🧩 Prérequis
- Java 17
- MySQL
- Keycloak (version 22+ recommandée)
- Maven

### 📦 Étapes

```bash
# Cloner le projet
git clone https://github.com/ton-utilisateur/habilitation-bna.git
cd habilitation-bna

# Configurer application.properties
# (Base de données, Keycloak URI, etc.)

# Démarrer l'application Spring Boot
./mvnw spring-boot:run
