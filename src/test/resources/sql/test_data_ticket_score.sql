-- Clear old data
DELETE FROM ratings;
DELETE FROM tickets;
DELETE FROM users;
DELETE FROM rating_categories;

-- Rating Categories
INSERT INTO rating_categories (id, name, weight) VALUES (1, 'Spelling', 1);
INSERT INTO rating_categories (id, name, weight) VALUES (2, 'Grammar', 0.7);
INSERT INTO rating_categories (id, name, weight) VALUES (3, 'GDPR', 1.2);
INSERT INTO rating_categories (id, name, weight) VALUES (4, 'Randomness', 1);

-- Users
INSERT INTO users (id, name) VALUES (1, 'Reviewer A');
INSERT INTO users (id, name) VALUES (2, 'Agent B');

-- Ticket 201: Good spelling and GDPR
INSERT INTO tickets (id, subject, created_at) VALUES (201, 'Ticket A', '2025-07-01 08:00:00');

INSERT INTO ratings (rating, ticket_id, rating_category_id, reviewer_id, reviewee_id, created_at)
VALUES
  (5, 201, 1, 1, 2, '2025-07-01 09:00:00'),  -- Spelling: 5 * 1
  (4, 201, 3, 1, 2, '2025-07-01 09:10:00');  -- GDPR: 4 * 1.2

-- Ticket 202: Weak grammar and randomness
INSERT INTO tickets (id, subject, created_at)
VALUES (202, 'Ticket B', '2025-07-01 10:00:00');

INSERT INTO ratings (rating, ticket_id, rating_category_id, reviewer_id, reviewee_id, created_at)
VALUES
  (3, 202, 2, 1, 2, '2025-07-01 10:30:00'),  -- Grammar: 3 * 0.7
  (2, 202, 4, 1, 2, '2025-07-01 10:45:00');  -- Randomness: 2 * 1