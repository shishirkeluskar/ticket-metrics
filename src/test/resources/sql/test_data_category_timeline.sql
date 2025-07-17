-- Clear existing data
DELETE FROM ratings;
DELETE FROM tickets;
DELETE FROM users;
DELETE FROM rating_categories;

-- Rating Categories
INSERT INTO rating_categories (id, name, weight) VALUES (1, 'Spelling', 1.0);
INSERT INTO rating_categories (id, name, weight) VALUES (2, 'Grammar', 0.7);
INSERT INTO rating_categories (id, name, weight) VALUES (3, 'GDPR', 1.2);

-- Users
INSERT INTO users (id, name) VALUES (1, 'Person A');
INSERT INTO users (id, name) VALUES (2, 'Person B');

-- Tickets (spread over multiple days)
INSERT INTO tickets (id, subject, created_at) VALUES
  -- July
  (1, 'Ticket A', '2025-07-01T10:00:00'),
  (2, 'Ticket B', '2025-07-02T11:00:00'),
  (3, 'Ticket C', '2025-07-03T14:00:00'),
  (4, 'Ticket D', '2025-07-09T09:00:00'),
  (5, 'Ticket E', '2025-07-16T15:00:00'),
  (6, 'Ticket F', '2025-07-23T12:00:00'),
  -- August
  (7, 'Ticket G', '2025-08-01T08:00:00'),
  (8, 'Ticket H', '2025-08-05T07:00:00'),
  (9, 'Ticket I', '2025-08-12T12:00:00'),
  -- September
  (10, 'Ticket J', '2025-09-09T11:00:00');

-- Ratings
INSERT INTO ratings (id, rating, ticket_id, rating_category_id, reviewer_id, reviewee_id, created_at) VALUES
  -- July
  (1, 4, 1, 1, 1, 2, '2025-07-01T10:15:00'), -- Spelling
  (2, 3, 2, 2, 1, 2, '2025-07-02T11:30:00'), -- Grammar
  (3, 5, 3, 1, 1, 2, '2025-07-03T14:45:00'), -- Spelling
  (4, 2, 4, 3, 1, 2, '2025-07-09T09:20:00'), -- GDPR
  (5, 4, 5, 2, 1, 2, '2025-07-16T15:10:00'), -- Grammar
  (6, 5, 6, 1, 1, 2, '2025-07-23T12:30:00'), -- Spelling
  (7, 3, 6, 2, 1, 2, '2025-07-23T12:45:00'), -- Grammar
  -- August
  (8, 4, 7, 1, 1, 2, '2025-08-01T08:00:00'), -- Spelling
  (9, 3, 7, 2, 1, 2, '2025-08-01T08:00:00'), -- Grammar
  (10, 5, 7, 1, 1, 2, '2025-08-01T08:00:00'), -- Spelling
  (11, 2, 8, 3, 1, 2, '2025-08-05T07:00:00'), -- GDPR
  (12, 4, 8, 2, 1, 2, '2025-08-05T07:00:00'), -- Grammar
  (13, 5, 9, 1, 1, 2, '2025-08-12T12:00:00'), -- Spelling
  (14, 3, 9, 2, 1, 2, '2025-08-12T12:00:00'), -- Grammar
  -- September
  (15, 4, 10, 1, 1, 2, '2025-09-09T11:00:00'), -- Spelling
  (16, 3, 10, 2, 1, 2, '2025-09-09T11:00:00'); -- Grammar