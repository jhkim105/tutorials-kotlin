-- Stock
INSERT INTO stock (id, exchange_code, stock_code, business_date, created_at, updated_at)
VALUES (1, 'FE', '201', 'Stock A', '2025-06-01', '2025-06-01 09:00:00', '2025-06-01 09:00:00'),
       (2, 'META', '201', 'Stock A', '2025-06-05', '2025-06-05 13:00:00', '2025-06-05 13:00:00');

-- StockHistory
INSERT INTO stock_history (stock_id, exchange_code, stock_code, business_date, change_at)
VALUES (1, 'FE', '201', '2025-06-01', '2025-06-01 10:00:00'),
       (1, 'META', '201', '2025-06-05',  '2025-06-05 14:00:00');

-- StockPrice
INSERT INTO stock_price (exchange_code, stock_code, price, buinsess_date)
VALUES ('FE', '201', 100.0, '2025-06-01'),
       ('META', '201', 105.0, '2025-06-04'),
       ('META', '201', 110.0, '2025-06-05');
       ('META', '201', 112.0, '2025-06-06');
       ('META', '201', 111.0, '2025-06-07');