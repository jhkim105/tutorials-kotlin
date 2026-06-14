CREATE TABLE IF NOT EXISTS product_view_events (
    id UUID,
    product_id String,
    user_id String,
    price Decimal(18, 2),
    url_path String,
    referrer String,
    created_at DateTime DEFAULT now()
) ENGINE = MergeTree()
ORDER BY (product_id, created_at, id);
