package kth.id2203.pp2p.event;


import kth.id2203.message.MessagePayload;
import kth.id2203.network.TAddress;
import se.sics.kompics.KompicsEvent;

public class P2PAckDeliver implements KompicsEvent {
	private final TAddress from;
	
	private final MessagePayload message;
	
	public P2PAckDeliver(TAddress from, MessagePayload message) {
		this.from = from;
		this.message = message;
	}

	public TAddress getFrom() {
		return from;
	}

	public MessagePayload getMessage() {
		return message;
	}
	
	
}
