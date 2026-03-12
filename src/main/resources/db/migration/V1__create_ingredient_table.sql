CREATE TABLE IF NOT EXISTS ingredient_regulation (
    inci_name VARCHAR(255) PRIMARY KEY,
    max_concentration DOUBLE PRECISION,
    restricted BOOLEAN DEFAULT FALSE,
    prohibited BOOLEAN DEFAULT FALSE,
    conditions TEXT,
    product_types VARCHAR(255),
    regulation_ref VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_ingredient_inci_name ON ingredient_regulation (inci_name);

CREATE TABLE IF NOT EXISTS combination_rule (
    id BIGSERIAL PRIMARY KEY,
    ingredient_a VARCHAR(255) NOT NULL,
    ingredient_b VARCHAR(255) NOT NULL,
    condition_desc TEXT,
    safe_concentration_a DOUBLE PRECISION,
    required_concentration_b DOUBLE PRECISION,
    explanation TEXT,
    source VARCHAR(255)
);
