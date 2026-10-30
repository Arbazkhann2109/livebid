package com.livebid.bidding;

import com.livebid.bidding.dto.BidUpdateMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class BidWebSocketPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public BidWebSocketPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishBid(BidUpdateMessage message) {
        String destination = "/topic/auctions/" + message.auctionId() + "/bids";

        messagingTemplate.convertAndSend(destination, message);
    }
}