-- Users table
CREATE TABLE IF NOT EXISTS utilisateurs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    login TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    role TEXT NOT NULL
);

-- Products table
CREATE TABLE IF NOT EXISTS produits (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nom TEXT NOT NULL,
    prix REAL NOT NULL,
    code_barres TEXT UNIQUE,
    stock INTEGER NOT NULL DEFAULT 0,
    categorie TEXT,
    seuil_alerte INTEGER DEFAULT 5
);

-- Sales table
CREATE TABLE IF NOT EXISTS ventes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    date DATETIME DEFAULT CURRENT_TIMESTAMP,
    montant_total REAL NOT NULL,
    remise REAL DEFAULT 0,
    utilisateur_id INTEGER,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id)
);

-- Sale items table
CREATE TABLE IF NOT EXISTS lignes_vente (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    vente_id INTEGER,
    produit_id INTEGER,
    quantite INTEGER NOT NULL,
    prix_unitaire REAL NOT NULL,
    FOREIGN KEY (vente_id) REFERENCES ventes(id),
    FOREIGN KEY (produit_id) REFERENCES produits(id)
);

-- Settings table
CREATE TABLE IF NOT EXISTS parametres (
    cle TEXT PRIMARY KEY,
    valeur TEXT NOT NULL
);

-- Insert default admin user if not exists
INSERT OR IGNORE INTO utilisateurs (login, password, role)
VALUES ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'admin');

-- Insert default settings
INSERT OR IGNORE INTO parametres (cle, valeur) VALUES
('nom_magasin', 'Mon Magasin'),
('adresse', '123 Rue Example'),
('telephone', '01 23 45 67 89'),
('email', 'contact@monmagasin.fr'),
('en_tete_ticket', 'Merci de votre visite !'),
('pied_ticket', 'À bientôt !'),
('imprimante', 'Imprimante par défaut'); 