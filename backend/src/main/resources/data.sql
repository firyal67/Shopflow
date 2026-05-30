-- ============================================================
-- ShopFlow -- Donnees de demonstration
-- Mot de passe pour tous les utilisateurs : Password1
-- ============================================================

-- ============================================================
-- UTILISATEURS
-- ============================================================
INSERT INTO users (email, mot_de_passe, prenom, nom, role, actif, date_creation) VALUES
('admin@shopflow.com',    '$2a$10$u70pOdd6fu68afmbvvOEdxepjKTCtvb7K20SGVHlrzcoCmcPxJnany', 'Admin',   'ShopFlow', 'ADMIN',    true, NOW()),
('vendeur1@shopflow.com', '$2a$10$u70pOdd6fu68afmbvvOEdxepjKTCtvb7K20SGVHlrzcoCmcPxJnany', 'Marie',   'Martin',   'SELLER',   true, NOW()),
('vendeur2@shopflow.com', '$2a$10$u70pOdd6fu68afmbvvOEdxepjKTCtvb7K20SGVHlrzcoCmcPxJnany', 'Pierre',  'Dupont',   'SELLER',   true, NOW()),
('client1@shopflow.com',  '$2a$10$u70pOdd6fu68afmbvvOEdxepjKTCtvb7K20SGVHlrzcoCmcPxJnany', 'Jean',    'Bernard',  'CUSTOMER', true, NOW()),
('client2@shopflow.com',  '$2a$10$u70pOdd6fu68afmbvvOEdxepjKTCtvb7K20SGVHlrzcoCmcPxJnany', 'Sophie',  'Leroy',    'CUSTOMER', true, NOW());

-- ============================================================
-- PROFILS VENDEURS
-- ============================================================
INSERT INTO seller_profiles (user_id, nom_boutique, description, logo, note) VALUES
(2, 'Mode et Style',   'Vetements tendance pour homme et femme', 'https://picsum.photos/seed/shop1/200', 4.5),
(3, 'Tech Universe',   'Accessoires et gadgets high-tech',       'https://picsum.photos/seed/shop2/200', 4.2);

-- ============================================================
-- ADRESSES
-- ============================================================
INSERT INTO addresses (user_id, rue, ville, code_postal, pays, principal) VALUES
(4, '12 Rue de la Paix',       'Paris',     '75001', 'France', true),
(4, '5 Avenue des Fleurs',     'Lyon',      '69001', 'France', false),
(5, '8 Boulevard Victor Hugo', 'Marseille', '13001', 'France', true);

-- ============================================================
-- CATEGORIES
-- ============================================================
INSERT INTO categories (nom, description, parent_id) VALUES
('Vetements',    'Mode et habillement',          NULL),
('Electronique', 'Appareils et accessoires tech', NULL),
('Maison',       'Decoration et mobilier',        NULL),
('T-Shirts',     'T-shirts et hauts',             1),
('Pantalons',    'Jeans et pantalons',             1),
('Smartphones',  'Telephones mobiles',             2),
('Accessoires',  'Accessoires tech',               2);

-- ============================================================
-- PRODUITS
-- ============================================================
INSERT INTO products (seller_id, nom, description, prix, prix_promo, stock, actif, date_creation, note_moyenne, total_ventes) VALUES
(2, 'T-Shirt Premium Coton Bio',  'T-shirt en coton bio, coupe moderne, disponible en plusieurs tailles', 29.99, 19.99, 50, true, NOW(), 4.5, 120),
(2, 'Jean Slim Fit',              'Jean slim fit stretch, confortable et elegant',                         59.99, NULL,  30, true, NOW(), 4.2, 85),
(2, 'Robe Ete Fleurie',           'Robe legere parfaite pour l''ete, motif floral',                        45.00, 35.00, 25, true, NOW(), 4.7, 60),
(2, 'Veste en Jean',              'Veste en jean classique, coupe droite',                                 79.99, NULL,  20, true, NOW(), 4.0, 45),
(3, 'Ecouteurs Bluetooth Pro',    'Ecouteurs sans fil avec reduction de bruit active, 30h d''autonomie',   89.99, 69.99, 40, true, NOW(), 4.6, 200),
(3, 'Chargeur Rapide USB-C 65W',  'Chargeur rapide compatible avec tous les appareils USB-C',              24.99, NULL,  100,true, NOW(), 4.3, 350),
(3, 'Coque iPhone 15 Pro',        'Coque de protection premium en silicone liquide',                       19.99, 14.99, 80, true, NOW(), 4.1, 180),
(3, 'Support Telephone Voiture',  'Support magnetique universel pour tableau de bord',                     15.99, NULL,  60, true, NOW(), 3.9, 95);

-- ============================================================
-- IMAGES PRODUITS
-- ============================================================
INSERT INTO product_images (product_id, image_url) VALUES
(1, 'https://picsum.photos/seed/tshirt1/400/400'),
(1, 'https://picsum.photos/seed/tshirt2/400/400'),
(2, 'https://picsum.photos/seed/jean1/400/400'),
(2, 'https://picsum.photos/seed/jean2/400/400'),
(3, 'https://picsum.photos/seed/robe1/400/400'),
(4, 'https://picsum.photos/seed/veste1/400/400'),
(5, 'https://picsum.photos/seed/ecouteurs1/400/400'),
(5, 'https://picsum.photos/seed/ecouteurs2/400/400'),
(6, 'https://picsum.photos/seed/chargeur1/400/400'),
(7, 'https://picsum.photos/seed/coque1/400/400'),
(8, 'https://picsum.photos/seed/support1/400/400');

-- ============================================================
-- ASSOCIATIONS PRODUITS - CATEGORIES
-- ============================================================
INSERT INTO product_categories (product_id, category_id) VALUES
(1, 1), (1, 4),
(2, 1), (2, 5),
(3, 1),
(4, 1),
(5, 2), (5, 7),
(6, 2), (6, 7),
(7, 2), (7, 7),
(8, 2), (8, 7);

-- ============================================================
-- VARIANTES PRODUITS
-- ============================================================
INSERT INTO product_variants (product_id, attribut, valeur, stock_supplementaire, prix_delta) VALUES
(1, 'Taille', 'S',  10, 0.00),
(1, 'Taille', 'M',  15, 0.00),
(1, 'Taille', 'L',  15, 0.00),
(1, 'Taille', 'XL', 10, 2.00),
(2, 'Taille', '38', 8,  0.00),
(2, 'Taille', '40', 10, 0.00),
(2, 'Taille', '42', 8,  0.00),
(2, 'Taille', '44', 4,  0.00),
(3, 'Taille', 'S',  8,  0.00),
(3, 'Taille', 'M',  10, 0.00),
(3, 'Taille', 'L',  7,  0.00),
(5, 'Couleur', 'Noir',  15, 0.00),
(5, 'Couleur', 'Blanc', 15, 0.00),
(5, 'Couleur', 'Bleu',  10, 5.00);

-- ============================================================
-- COUPONS
-- ============================================================
INSERT INTO coupons (code, type, valeur, date_expiration, usages_max, usages_actuels, actif) VALUES
('BIENVENUE10', 'PERCENT', 10.00, '2026-10-21 00:00:00', 100, 5,  true),
('PROMO20',     'PERCENT', 20.00, '2026-05-21 00:00:00', 50,  12, true),
('REMISE5',     'FIXED',   5.00,  '2026-07-21 00:00:00', 200, 30, true),
('FLASH15',     'PERCENT', 15.00, '2026-04-28 00:00:00', 30,  0,  true);

-- ============================================================
-- COMMANDES
-- ============================================================
INSERT INTO orders (customer_id, statut, numero_commande, adresse_livraison, sous_total, frais_livraison, remise_coupon, total_ttc, date_commande, is_new) VALUES
(4, 'DELIVERED', 'ORD-2026-00001', '12 Rue de la Paix, 75001 Paris, France',                  109.97, 0.00, 0.00, 109.97, '2026-03-22 10:00:00', false),
(4, 'SHIPPED',   'ORD-2026-00002', '12 Rue de la Paix, 75001 Paris, France',                   89.99, 0.00, 9.00,  80.99, '2026-04-16 14:30:00', true),
(5, 'PENDING',   'ORD-2026-00003', '8 Boulevard Victor Hugo, 13001 Marseille, France',          45.98, 5.99, 0.00,  51.97, '2026-04-20 09:15:00', true);

-- ============================================================
-- LIGNES DE COMMANDE
-- ============================================================
INSERT INTO order_items (order_id, product_id, variant_id, quantite, prix_unitaire) VALUES
(1, 1, 2,    2, 19.99),
(1, 6, NULL, 1, 24.99),
(1, 8, NULL, 1, 15.99),
(2, 5, 12,   1, 69.99),
(3, 3, 10,   1, 35.00),
(3, 7, NULL, 1, 14.99);

-- ============================================================
-- AVIS
-- ============================================================
INSERT INTO reviews (customer_id, product_id, note, commentaire, date_creation, approuve) VALUES
(4, 1, 5, 'Excellent t-shirt, tres confortable et belle qualite !', '2026-03-27 10:00:00', true),
(4, 6, 4, 'Chargeur rapide et efficace, livraison rapide.',         '2026-03-27 10:05:00', true),
(4, 8, 4, 'Bon support, tient bien le telephone.',                  '2026-03-27 10:10:00', true),
(4, 5, 5, 'Son exceptionnel, reduction de bruit parfaite !',        '2026-04-18 15:00:00', false),
(5, 3, 5, 'Magnifique robe, coupe parfaite et tissu agreable.',     '2026-04-20 11:00:00', false);
