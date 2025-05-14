# javafx
# eConsult+

## Overview

eConsult+ est une plateforme de santé innovante permettant la gestion et la réalisation de consultations médicales en ligne. Elle offre un espace sécurisé pour les patients et les professionnels de santé afin de faciliter la prise de rendez-vous, la gestion des traitements, la prescription, le suivi des consultations et la gestion des réclamations. Ce projet a été développé dans le cadre d’un cursus universitaire à Esprit (2024-2025).

## Features

Gestion des utilisateurs (patients, médecins, administrateurs)
Prise de rendez-vous en ligne
Gestion des réclamations
Gestion des traitements et prescriptions
Gestion des consultations médicales
Gestion des produits médicaux
Tableau de bord et statistiques
Notifications et messagerie intégrée


### Tech Stack

- *Web* : PHP, Symfony
- *Desktop* : Java, JavaFX
- *Base de données* : MySQL

### Web Application (Symfony)

Application web développée avec le framework PHP Symfony, permettant la gestion des utilisateurs, rendez-vous, consultations, réclamations, traitements, prescriptions et produits médicaux via une interface moderne et sécurisée.


### Desktop Application (JavaFX)

Application desktop développée avec JavaFX, offrant une interface riche pour la gestion des fonctionnalités principales de la plateforme eConsult+ côté personnel médical ou administratif.


### Other Tools

Composer pour la gestion des dépendances PHP
Maven ou Gradle pour la gestion du projet JavaFX
(Ajouter d’autres outils spécifiques si besoin)


## Directory Structure

src/
├── main/
│   ├── java/
│   │   └── Controllers/
│   │       ├── Front/
│   │       ├── Back/
│   │       └── consultation/
│   └── resources/
│       ├── application.properties
│       └── static/
└── test/

## Getting Started

1. *Cloner le dépôt*
   
   git clone <url-du-repo>
   
2. *Configurer la base de données*  
   Modifier application.properties avec vos informations de connexion.

3. *Lancer l’application*
   
   ./mvnw spring-boot:run
   

4. *Accéder à l’application*  
   Ouvrir [http://localhost:8080](http://localhost:8000)

## Acknowledgments

Merci à l’équipe pédagogique d’Esprit.
Basé sur les bonnes pratiques Java/Spring Boot.
Toute contribution est la bienvenue !


---

*Mots-clés* : santé, consultation en ligne, gestion utilisateurs, rendez-vous, réclamation, traitement, prescription, produit médical, Java, Spring Boot, API, plateforme santé, e-santé, Esprit
