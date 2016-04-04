package kth.id2203.pp2p.event;


import kth.id2203.message.MessagePayload;
import kth.id2203.network.TAddress;
import se.sics.kompics.KompicsEvent;

public class P2PSend implements KompicsEvent {
	private final TAddress to;
	
	private final MessagePayload message;
	
	public P2PSend(TAddress to, MessagePayload message) {
		this.to = to;
		this.message = message;
	}
	
	public P2PSend(TAddress to) {
		this.to = to;
		this.message = null;
	}

	public TAddress getTo() {
		return to;
	}

	public MessagePayload getMessage() {
		return message;
	}
	
	
	
}
