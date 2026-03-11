-- Seed data — INSERT IGNORE skips rows that already exist (safe to re-run)
INSERT IGNORE INTO products (product_code, name, description, category, price, stock_quantity, low_stock_threshold)
VALUES
  ('P001', 'Laptop Pro 15',          '15-inch laptop, Intel i7, 16GB RAM, 512GB SSD',  'Electronics', 85000.00,  25,  5),
  ('P002', 'Wireless Mouse',          'Ergonomic wireless mouse, 1600 DPI',              'Electronics',  1200.00, 150, 20),
  ('P003', 'USB-C Hub 7-Port',        '7-in-1 USB-C hub with HDMI and PD charging',      'Electronics',  2500.00,   8, 10),
  ('P004', 'Mechanical Keyboard',     'TKL mechanical keyboard, Cherry MX switches',     'Electronics',  4500.00,  45, 10),
  ('P005', '27-inch Monitor',         '4K IPS display, 144Hz, USB-C',                    'Electronics', 22000.00,   3,  5),
  ('P006', 'Running Shoes Pro',       'Lightweight performance running shoes',            'Sports',       5500.00,  60, 15),
  ('P007', 'Yoga Mat Premium',        'Non-slip 6mm thick yoga mat',                     'Sports',       1800.00, 200, 30),
  ('P008', 'Stainless Water Bottle',  '750ml insulated stainless steel bottle',          'Sports',        800.00,   7, 10),
  ('P009', 'Java Programming Book',   'Effective Java, 3rd edition',                     'Books',         650.00,  90, 20),
  ('P010', 'Cloud Architecture Guide','Designing distributed systems — O''Reilly',       'Books',         850.00,  35, 10);
