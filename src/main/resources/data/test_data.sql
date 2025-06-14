-- Suppression des données existantes (si nécessaire)
DELETE FROM ligne_vente;
DELETE FROM vente;
DELETE FROM produit;

-- Insertion des produits
INSERT INTO produit (nom, prix, code_barres, quantite, categorie, seuil_alerte) VALUES
('Café Arabica', 12.99, '1234567890123', 50, 'Boissons', 10),
('Café Robusta', 9.99, '1234567890124', 30, 'Boissons', 5),
('Croissant', 1.20, '1234567890125', 100, 'Pâtisseries', 20),
('Pain au chocolat', 1.30, '1234567890126', 80, 'Pâtisseries', 15),
('Sandwich Jambon', 4.50, '1234567890127', 25, 'Sandwichs', 8),
('Sandwich Poulet', 4.80, '1234567890128', 25, 'Sandwichs', 8),
('Muffin Chocolat', 2.50, '1234567890129', 40, 'Pâtisseries', 10),
('Cookie', 1.80, '1234567890130', 60, 'Pâtisseries', 15),
('Eau minérale', 1.00, '1234567890131', 100, 'Boissons', 20),
('Soda', 2.00, '1234567890132', 80, 'Boissons', 15);

-- Insertion des ventes (sur les 30 derniers jours)
INSERT INTO vente (date, total) VALUES
(CURRENT_DATE - INTERVAL '1 day', 25.50),
(CURRENT_DATE - INTERVAL '2 day', 18.30),
(CURRENT_DATE - INTERVAL '3 day', 32.40),
(CURRENT_DATE - INTERVAL '4 day', 15.20),
(CURRENT_DATE - INTERVAL '5 day', 28.90),
(CURRENT_DATE - INTERVAL '10 day', 22.50),
(CURRENT_DATE - INTERVAL '15 day', 19.80),
(CURRENT_DATE - INTERVAL '20 day', 35.60),
(CURRENT_DATE - INTERVAL '25 day', 16.40),
(CURRENT_DATE - INTERVAL '30 day', 24.70);

-- Insertion des lignes de vente
-- Vente 1
INSERT INTO ligne_vente (vente_id, produit_id, quantite, prix_unitaire, sous_total) VALUES
(1, 1, 2, 12.99, 25.98), -- 2 cafés Arabica
(1, 3, 1, 1.20, 1.20);   -- 1 croissant

-- Vente 2
INSERT INTO ligne_vente (vente_id, produit_id, quantite, prix_unitaire, sous_total) VALUES
(2, 2, 1, 9.99, 9.99),   -- 1 café Robusta
(2, 4, 2, 1.30, 2.60),   -- 2 pains au chocolat
(2, 9, 3, 1.00, 3.00);   -- 3 eaux minérales

-- Vente 3
INSERT INTO ligne_vente (vente_id, produit_id, quantite, prix_unitaire, sous_total) VALUES
(3, 5, 2, 4.50, 9.00),   -- 2 sandwiches jambon
(3, 6, 2, 4.80, 9.60),   -- 2 sandwiches poulet
(3, 10, 4, 2.00, 8.00);  -- 4 sodas

-- Vente 4
INSERT INTO ligne_vente (vente_id, produit_id, quantite, prix_unitaire, sous_total) VALUES
(4, 7, 3, 2.50, 7.50),   -- 3 muffins
(4, 8, 4, 1.80, 7.20);   -- 4 cookies

-- Vente 5
INSERT INTO ligne_vente (vente_id, produit_id, quantite, prix_unitaire, sous_total) VALUES
(5, 1, 1, 12.99, 12.99), -- 1 café Arabica
(5, 3, 2, 1.20, 2.40),   -- 2 croissants
(5, 9, 2, 1.00, 2.00);   -- 2 eaux minérales

-- Vente 6
INSERT INTO ligne_vente (vente_id, produit_id, quantite, prix_unitaire, sous_total) VALUES
(6, 2, 2, 9.99, 19.98),  -- 2 cafés Robusta
(6, 4, 1, 1.30, 1.30);   -- 1 pain au chocolat

-- Vente 7
INSERT INTO ligne_vente (vente_id, produit_id, quantite, prix_unitaire, sous_total) VALUES
(7, 5, 1, 4.50, 4.50),   -- 1 sandwich jambon
(7, 7, 2, 2.50, 5.00),   -- 2 muffins
(7, 10, 3, 2.00, 6.00);  -- 3 sodas

-- Vente 8
INSERT INTO ligne_vente (vente_id, produit_id, quantite, prix_unitaire, sous_total) VALUES
(8, 6, 3, 4.80, 14.40),  -- 3 sandwiches poulet
(8, 8, 2, 1.80, 3.60),   -- 2 cookies
(8, 9, 4, 1.00, 4.00);   -- 4 eaux minérales

-- Vente 9
INSERT INTO ligne_vente (vente_id, produit_id, quantite, prix_unitaire, sous_total) VALUES
(9, 1, 1, 12.99, 12.99), -- 1 café Arabica
(9, 3, 3, 1.20, 3.60);   -- 3 croissants

-- Vente 10
INSERT INTO ligne_vente (vente_id, produit_id, quantite, prix_unitaire, sous_total) VALUES
(10, 2, 1, 9.99, 9.99),  -- 1 café Robusta
(10, 4, 2, 1.30, 2.60),  -- 2 pains au chocolat
(10, 7, 1, 2.50, 2.50),  -- 1 muffin
(10, 10, 2, 2.00, 4.00); -- 2 sodas 