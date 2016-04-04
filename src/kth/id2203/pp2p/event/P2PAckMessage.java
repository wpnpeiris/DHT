package kth.id2203.pp2p.event;


import kth.id2203.message.MessagePayload;
import se.sics.kompics.KompicsEvent;

public class P2PAckMessage implements KompicsEvent { 
	
	private final MessagePayload message;

	public P2PAckMessage(MessagePayload message) {
		this.message = message;
	}
	
	public MessagePayload getMessage() {
		return message;
	}
}
