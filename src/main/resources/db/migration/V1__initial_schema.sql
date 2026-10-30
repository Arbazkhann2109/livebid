
CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_role
        CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT chk_users_status
        CHECK (status IN ('ACTIVE', 'SUSPENDED'))
) ENGINE=InnoDB;

CREATE TABLE auctions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    seller_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    starting_price DECIMAL(13,2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'INR',
    starts_at TIMESTAMP(6) NOT NULL,
    ends_at TIMESTAMP(6) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_auctions_seller
        FOREIGN KEY (seller_id) REFERENCES users(id),
    CONSTRAINT chk_auctions_starting_price
        CHECK (starting_price >= 0),
    CONSTRAINT chk_auctions_time
        CHECK (ends_at > starts_at),
    CONSTRAINT chk_auctions_status
        CHECK (status IN ('DRAFT', 'SCHEDULED', 'LIVE', 'ENDED', 'CANCELLED')),
    INDEX idx_auctions_status_end (status, ends_at),
    INDEX idx_auctions_seller (seller_id, created_at)
) ENGINE=InnoDB;

CREATE TABLE bids (
    id BIGINT NOT NULL AUTO_INCREMENT,
    auction_id BIGINT NOT NULL,
    bidder_id BIGINT NOT NULL,
    amount DECIMAL(13,2) NOT NULL,
    placed_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_bids_auction
        FOREIGN KEY (auction_id) REFERENCES auctions(id),
    CONSTRAINT fk_bids_bidder
        FOREIGN KEY (bidder_id) REFERENCES users(id),
    CONSTRAINT chk_bids_amount
        CHECK (amount > 0),
    INDEX idx_bids_auction_amount (auction_id, amount),
    INDEX idx_bids_bidder_time (bidder_id, placed_at),
    INDEX idx_bids_auction_time (auction_id, placed_at)
) ENGINE=InnoDB;
