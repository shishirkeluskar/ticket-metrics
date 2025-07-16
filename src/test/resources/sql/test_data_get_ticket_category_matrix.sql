-- Clear old data
DELETE FROM ratings;
DELETE FROM tickets;
DELETE FROM users;
DELETE FROM rating_categories;

-- Rating categories
INSERT INTO rating_categories (id, name, weight) VALUES (1, 'Spelling', 1);
INSERT INTO rating_categories (id, name, weight) VALUES (2, 'Grammar', 0.7);
INSERT INTO rating_categories (id, name, weight) VALUES (3, 'GDPR', 1.2);
INSERT INTO rating_categories (id, name, weight) VALUES (4, 'Randomness', 1);

-- Tickets created on different days
INSERT INTO tickets (id, subject, created_at) VALUES (1, 'Ticket 1', '2025-07-01 09:00:00');
INSERT INTO tickets (id, subject, created_at) VALUES (2, 'Ticket 2', '2025-07-02 10:00:00');
INSERT INTO tickets (id, subject, created_at) VALUES (3, 'Ticket 3', '2025-07-03 11:00:00');
INSERT INTO tickets (id, subject, created_at) VALUES (4, 'Ticket 4', '2025-07-04 12:00:00');

-- Users (reviewers and reviewees)
INSERT INTO users (id, name) VALUES (1, 'Reviewer A');
INSERT INTO users (id, name) VALUES (2, 'Reviewer B');
INSERT INTO users (id, name) VALUES (3, 'Reviewee A');
INSERT INTO users (id, name) VALUES (4, 'Reviewee B');

-- Ratings on tickets with different categories
INSERT INTO ratings (id, rating, ticket_id, rating_category_id, reviewer_id, reviewee_id, created_at) VALUES
  (1, 4, 1, 1, 1, 3, '2025-07-01 10:00:00'),
  (2, 3, 1, 2, 2, 3, '2025-07-01 10:05:00'),
  (3, 5, 2, 1, 1, 4, '2025-07-02 11:00:00'),
  (4, 2, 2, 3, 2, 4, '2025-07-02 11:30:00'),
  (5, 4, 3, 4, 1, 3, '2025-07-03 12:00:00'),
  (6, 1, 4, 2, 2, 4, '2025-07-04 13:00:00');
