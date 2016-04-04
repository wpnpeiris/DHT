package kth.id2203.pp2p.event;


import kth.id2203.message.MessagePayload;
import kth.id2203.network.TAddress;
import se.sics.kompics.KompicsEvent;

public class P2PDeliver implements KompicsEvent {
	private final TAddress from;
	
	private final MessagePayload message;
	
	public P2PDeliver(TAddress from) {
		this.from = from;
		this.message = null;
	}

	
	public P2PDeliver(TAddress from, MessagePayload message) {
		this.from = from;
		this.message = message;
	}

	public MessagePayload getMessage() {
		return message;
	}

	public TAddress getFrom() {
		return from;
	}
}
