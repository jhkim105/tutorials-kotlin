-- Source Table
CREATE TABLE source (
                              id INT PRIMARY KEY,
                              name VARCHAR(50),
                              amount DECIMAL(10, 2)
);

-- Target Table
CREATE TABLE target (
                              id INT PRIMARY KEY,
                              name VARCHAR(50),
                              amount DECIMAL(10, 2)
);