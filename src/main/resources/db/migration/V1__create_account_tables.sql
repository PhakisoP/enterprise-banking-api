CREATE TABLE accounts (
    account_number INT NOT NULL,
    customer_id INT,
    account_type VARCHAR(255),
    balance DECIMAL(19, 2),
    pin VARCHAR(255),
    failed_attempts INT,
    is_locked BIT,
    PRIMARY KEY (account_number)
);

CREATE TABLE transactions (
    transaction_id INT NOT NULL AUTO_INCREMENT,
    account_number INT,
    transaction_type VARCHAR(255),
    amount DECIMAL(19, 2),
    balance_after DECIMAL(19, 2),
    transaction_date DATETIME(6),
    PRIMARY KEY (transaction_id)
);
