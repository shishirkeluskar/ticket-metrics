DROP TABLE IF EXISTS ratings;
DROP TABLE IF EXISTS tickets;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS rating_categories;

-- rating_categories definition
CREATE TABLE rating_categories (
    id integer PRIMARY KEY AUTOINCREMENT,
    name text NOT NULL,
    weight real NOT NULL
);

-- tickets definition
CREATE TABLE tickets (
    id integer PRIMARY KEY AUTOINCREMENT,
    subject text NOT NULL,
    created_at datetime
);

-- users definition
CREATE TABLE users (
    id integer PRIMARY KEY AUTOINCREMENT,
    name text NOT NULL
);

-- ratings definition
CREATE TABLE ratings (
    id integer PRIMARY KEY AUTOINCREMENT,
    rating integer NOT NULL,
    ticket_id integer NOT NULL,
    rating_category_id integer NOT NULL,
    reviewer_id integer NOT NULL,
    reviewee_id integer NOT NULL,
    created_at datetime,
    FOREIGN KEY (ticket_id) REFERENCES tickets (id),
    FOREIGN KEY (reviewer_id) REFERENCES users (id),
    FOREIGN KEY (reviewee_id) REFERENCES users (id),
    FOREIGN KEY (rating_category_id) REFERENCES rating_catregories (id)
);