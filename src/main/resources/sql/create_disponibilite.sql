CREATE TABLE IF NOT EXISTS disponibilite (
    id INT AUTO_INCREMENT PRIMARY KEY,
    medecin_id INT NOT NULL,
    date_debut DATETIME NOT NULL,
    date_fin DATETIME NOT NULL,
    statut VARCHAR(50) DEFAULT 'Disponible',
    FOREIGN KEY (medecin_id) REFERENCES user(id)
);
