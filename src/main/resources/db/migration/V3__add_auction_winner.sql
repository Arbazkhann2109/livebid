ALTER TABLE auctions
ADD COLUMN winning_bid_id BIGINT NULL;

ALTER TABLE auctions
ADD CONSTRAINT fk_auctions_winning_bid
FOREIGN KEY (winning_bid_id)
REFERENCES bids(id);

CREATE INDEX idx_auctions_winning_bid
ON auctions(winning_bid_id);