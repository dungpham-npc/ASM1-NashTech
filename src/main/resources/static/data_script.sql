-- Insert 10 categories
DO $$
BEGIN
    FOR i IN 1..10 LOOP
        INSERT INTO categories (name, description, created_at, updated_at, is_active)
        VALUES (
            'Category ' || i,
            'Description for category ' || i,
            EXTRACT(EPOCH FROM now() - (random() * interval '365 days')),
            EXTRACT(EPOCH FROM now()),
            true
        );
    END LOOP;
END $$;

-- Insert 200 products
DO $$
BEGIN
    FOR i IN 1..200 LOOP
        INSERT INTO products (name, description, price, is_featured, is_active, category_id, created_at, updated_at)
        VALUES (
            'Product ' || i,
            'Description for product ' || i,
            round((random() * 990 + 10)::numeric, 2), -- prices from 10 to 1000
            (random() > 0.8), -- 20% chance to be featured
            true,
            (1 + floor(random() * 10))::bigint, -- category_id from 1 to 10
            EXTRACT(EPOCH FROM now() - (random() * interval '365 days')),
            EXTRACT(EPOCH FROM now())
        );
    END LOOP;
END $$;
