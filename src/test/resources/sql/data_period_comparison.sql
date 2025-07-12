DELETE FROM ratings;
DELETE FROM tickets;
DELETE FROM users;
DELETE FROM rating_categories;

INSERT INTO rating_categories (id, name, weight) VALUES
  (1, 'Tone', 2);

INSERT INTO users (id, name) VALUES
  (1, 'Alice'),
  (2, 'Bob');

INSERT INTO tickets (id, subject, created_at) VALUES
  (1, 'T1', '2025-06-24 08:00:00'),  -- previous week
  (2, 'T2', '2025-07-01 08:00:00');  -- current week

INSERT INTO ratings (id, rating, ticket_id, rating_category_id, reviewer_id, reviewee_id, created_at) VALUES
  (1, 3, 1, 1, 1, 2, '2025-06-25 10:00:00'),
  (2, 5, 2, 1, 1, 2, '2025-07-02 11:00:00');
