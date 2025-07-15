-- Clear old data
DELETE FROM ratings;
DELETE FROM tickets;
DELETE FROM users;
DELETE FROM rating_categories;

INSERT INTO rating_categories (id, name, weight) VALUES
  (1, 'Tone', 2),
  (2, 'Grammar', 1);

INSERT INTO users (id, name) VALUES
  (1, 'Alice'),
  (2, 'Bob');

INSERT INTO tickets (id, subject, created_at) VALUES
  (1, 'Ticket 1', '2025-07-01 08:00:00'),
  (2, 'Ticket 2', '2025-07-02 08:00:00');

INSERT INTO ratings (id, rating, ticket_id, rating_category_id, reviewer_id, reviewee_id, created_at) VALUES
  (1, 3, 1, 1, 1, 2, '2025-07-01 10:00:00'),
  (2, 5, 2, 2, 1, 2, '2025-07-02 11:00:00');
