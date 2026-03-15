-- Stock
INSERT INTO stock (id, stock_code, exchange_code , stock_name, created_at, updated_at)
VALUES (1, 'A2', 'E2', 'Stock A', '2025-06-05 09:00:00', '2025-06-01 09:00:00'),
       (2, 'B1', 'E1', 'Stock B', '2025-06-05 09:00:00', '2025-06-05 13:00:00');

-- StockHistory
INSERT INTO stock_history (stock_id, stock_code, exchange_code , business_date, created_at)
VALUES (1, 'A1', 'E1', '2025-06-01', '2025-06-03 10:00:00'),
       (1, 'A2', 'E1', '2025-06-03',  '2025-06-03 09:00:00'),
       (1, 'A2', 'E2', '2025-06-05',  '2025-06-05 09:00:00');

-- StockPrice
INSERT INTO stock_price (stock_code, exchange_code, price, business_date)
VALUES ('A1', 'E1', 100.0, '2025-06-01'),
       ('A2', 'E1', 105.0, '2025-06-02'),
       ('A2', 'E2', 110.0, '2025-06-05'),
       ('A2', 'E2', 112.0, '2025-06-06'),
       ('E2', 'E2', 111.0, '2025-06-07');