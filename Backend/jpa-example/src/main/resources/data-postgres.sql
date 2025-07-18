-- Prvo kreirajmo korisnike i lokacije ako već ne postoje
--INSERT INTO users (
--  id,
--  email,
--  username,
--  password,
--  full_name,
--  address,
--  is_active,
--  role,
--  activated,
--  last_login,
--  number_of_people_following,
--  verification_token,
--  version
--) VALUES
--(
--  -1,
--  'johndoe@example.com',
--  'johndoe',
--  'password123',
--  'John Doe',
--  '123 Main St',
--  true,
--  'REGISTERED',
--  true,
--  NOW(),
--  3,
--  'token1',
--  0
--),
--(
--  -2,
--  'janedoe@example.com',
--  'janedoe',
--  'password456',
--  'Jane Doe',
--  '456 Second St',
--  true,
--  'REGISTERED',
--  true,
--  NOW(),
--  5,
--  'token2',
--  0
--);


--INSERT INTO locations (id, latitude, longitude, address) VALUES
--                                                             (2, 45.2671, 19.8335, '123 Main St'),
--                                                             (3, 44.7866, 20.4489, '456 Second St');

-- Kreiranje slika u tabeli 'images' sa vrednošću za 'compressed'
--INSERT INTO images (id, path, compressed) VALUES
--                                              (1, '/images/sunset.jpg', false),
--                                              (2, '/images/mountain.jpg', false),
--                                              (3, '/images/city.jpg', false);

-- Sada unosimo postove sa referencom na slike preko 'image_id'
--INSERT INTO posts (id, description, created_time, user_id, location_id, image_id) VALUES
 --                                                                                     (1, 'Beautiful sunset at the beach', '2024-11-11 17:00:00', -1, 1, 1),
   --                                                                                   (2, 'Amazing view from the mountain top', '2024-11-12 09:30:00', -2, 2, 2),
     --                                                                                 (3, 'Exploring the old city streets', '2024-11-13 14:45:00', -1, 1, 3);

-- Komentari na postove
--INSERT INTO comments (id, text, created_time, user_id, post_id) VALUES
--                                                                    (1, 'Amazing photo, really beautiful!', '2024-11-11 17:05:00', -2, 1),
--                                                                    (2, 'Thanks! The view was breathtaking.', '2024-11-11 17:10:00', -1, 1),
--                                                                    (3, 'Looks like a great place to visit!', '2024-11-12 10:00:00', -1, 2),
--                                                                    (4, 'I love exploring old cities too!', '2024-11-13 15:00:00', -2, 3);

-- Ažuriranje sekvence za komentare
--SELECT setval('comments_id_seq', (SELECT MAX(id) FROM comments) + 1);
--SELECT setval('images_id_seq', (SELECT MAX(id) FROM images));
--SELECT setval('posts_id_seq', (SELECT MAX(id) FROM posts));

-- Objave (pretpostavka: tabela se zove 'posts')
-- Pretpostavljamo da postoji slika sa ID 1 u tabeli images
-- Pretpostavljamo da postoji korisnik -1 i lokacija 1

--INSERT INTO posts (
--  id,
--  description,
--  image_id,
--  created_time,
--  user_id,
--  location_id,
--  likes_count,
--  version
--) VALUES
--  (100, 'Zec u basti', 1, NOW() - INTERVAL '3 days', -1, 1, 2, 0),
--  (101, 'Zec u snegu', 1, NOW() - INTERVAL '10 days', -2, 1, 3, 0),
--  (102, 'Zec na suncu', 1, NOW() - INTERVAL '2 days', -1, 1, 4, 0),
--  (103, 'Zec u kutiji', 1, NOW() - INTERVAL '5 days', -2, 1, 3, 0),
--  (104, 'Zec u sobi', 1, NOW() - INTERVAL '15 days', -1, 1, 1, 0);


-- Lajkovi (tabela post_liked_by_users)
-- Pretpostavka: ovo je join tabela za @ManyToMany između posts i users
--INSERT INTO post_likes (post_id, user_id) VALUES
--  (100, -2),
--  (100, -1),
--  (101, -1),
--  (102, -2),
--  (102, -1),
--  (103, -2),
--  (103, -1),
--  (104, -2);

