package kth.id2203.pp2p.event;


import kth.id2203.message.MessagePayload;
import se.sics.kompics.KompicsEvent;

public class P2PMessage implements KompicsEvent { 
	
	private final MessagePayload message;

	public P2PMessage(MessagePayload message) {
		this.message = message;
	}
	
	public MessagePayload getMessage() {
		return message;
	}
}
