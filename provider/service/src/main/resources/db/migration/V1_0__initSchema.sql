-- pet store
CREATE TABLE category(
    id INT IDENTITY,
    name VARCHAR(25) NOT NULL,
    description VARCHAR(255) NOT NULL,
    image_url VARCHAR(55),
    PRIMARY KEY (id)
);

CREATE TABLE product (
 id INT IDENTITY,
 category_id INT NOT NULL,
 name VARCHAR(25) NOT NULL,
 description VARCHAR(255) NOT NULL,
 image_url VARCHAR(55),
 PRIMARY KEY (id),
 FOREIGN KEY (category_id) REFERENCES category(id)
);

CREATE TABLE address (
 id INT IDENTITY,
 street1 VARCHAR(55) NOT NULL,
 street2 VARCHAR(55),
 city VARCHAR(55) NOT NULL,
 state VARCHAR(25) NOT NULL,
 zip VARCHAR(5) NOT NULL,
 latitude DECIMAL(14,10) NOT NULL,
 longitude DECIMAL(14,10) NOT NULL,
 PRIMARY KEY (id)
);

CREATE TABLE seller_contact (
 id INT IDENTITY,
 last_name VARCHAR(24) NOT NULL,
 first_name VARCHAR(24) NOT NULL,
 email VARCHAR(24) NOT NULL,
 PRIMARY KEY (id)
);

CREATE TABLE item (
 id INT IDENTITY,
 product_id INT NOT NULL,
 name VARCHAR(30) NOT NULL,
 description VARCHAR(500) NOT NULL,
 image_url VARCHAR(55),
 image_thumb_url VARCHAR(55),
 price DECIMAL(14,2) NOT NULL,
 address_id INT NOT NULL,
 seller_contact_id INT NOT NULL,
 total_score INTEGER NOT NULL,
 number_of_votes INTEGER NOT NULL,
 disabled INTEGER NOT NULL,
 PRIMARY KEY (id),
 FOREIGN KEY (address_id) REFERENCES address(id),
 FOREIGN KEY (product_id) REFERENCES product(id),
 FOREIGN KEY (seller_contact_id) REFERENCES seller_contact(id)
);

CREATE TABLE tag(
    id INT IDENTITY,
    tag VARCHAR(30) NOT NULL,
    ref_count INTEGER NOT NULL,
    PRIMARY KEY (id),
    UNIQUE(tag)
);

CREATE TABLE tag_item(
    tag_id INTEGER NOT NULL,
    item_id INTEGER NOT NULL,
    UNIQUE(tag_id, item_id),
    FOREIGN KEY (item_id) REFERENCES item(id),
    FOREIGN KEY (tag_id) REFERENCES tag(id)
);