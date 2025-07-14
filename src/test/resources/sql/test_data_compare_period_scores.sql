-- Clear old data
DELETE FROM ratings;
DELETE FROM tickets;
DELETE FROM users;
DELETE FROM rating_categories;

-- Rating categories
INSERT INTO rating_categories (id, name, weight) VALUES (1, 'Spelling', 1);
INSERT INTO rating_categories (id, name, weight) VALUES (2, 'Grammar', 0.7);
INSERT INTO rating_categories (id, name, weight) VALUES (3, 'GDPR', 1.2);

-- Users
INSERT INTO users (id, name) VALUES (1, 'Reviewer A');
INSERT INTO users (id, name) VALUES (2, 'Agent B');

-- ----------------------
-- Ticket 101 - Day 1: 2025-07-01
-- ----------------------
INSERT INTO tickets (id, subject, created_at) VALUES (101, 'Ticket X', '2025-07-01 08:00:00');
INSERT INTO ratings (rating, ticket_id, rating_category_id, reviewer_id, reviewee_id, created_at) VALUES
  (5, 101, 1, 1, 2, '2025-07-01 09:00:00'),
  (4, 101, 3, 1, 2, '2025-07-01 09:10:00');

-- ----------------------
-- Ticket 102 - Day 2: 2025-07-02
-- ----------------------
INSERT INTO tickets (id, subject, created_at) VALUES (102, 'Ticket Y', '2025-07-02 10:00:00');
INSERT INTO ratings (rating, ticket_id, rating_category_id, reviewer_id, reviewee_id, created_at) VALUES
  (3, 102, 2, 1, 2, '2025-07-02 10:20:00'),
  (2, 102, 1, 1, 2, '2025-07-02 10:25:00');

-- ----------------------
-- Ticket 103 - Day 3: 2025-07-03
-- ----------------------
INSERT INTO tickets (id, subject, created_at) VALUES (103, 'Ticket Z', '2025-07-03 09:00:00');
INSERT INTO ratings (rating, ticket_id, rating_category_id, reviewer_id, reviewee_id, created_at) VALUES
  (4, 103, 1, 1, 2, '2025-07-03 09:15:00'),
  (5, 103, 2, 1, 2, '2025-07-03 09:20:00');

-- ----------------------
-- Ticket 104 - Day 4: 2025-07-04
-- ----------------------
INSERT INTO tickets (id, subject, created_at) VALUES (104, 'Ticket A', '2025-07-04 10:00:00');
INSERT INTO ratings (rating, ticket_id, rating_category_id, reviewer_id, reviewee_id, created_at) VALUES
  (5, 104, 3, 1, 2, '2025-07-04 10:10:00'),
  (4, 104, 1, 1, 2, '2025-07-04 10:12:00');
