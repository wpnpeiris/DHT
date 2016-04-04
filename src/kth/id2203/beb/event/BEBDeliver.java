package kth.id2203.beb.event;


import kth.id2203.message.MessagePayload;
import kth.id2203.network.TAddress;
import se.sics.kompics.KompicsEvent;

public class BEBDeliver implements KompicsEvent {
	private final TAddress from;
	
	private final MessagePayload message;
	
	public BEBDeliver(TAddress from, MessagePayload message) {
		this.from = from;
		this.message =  message;
	}

	public TAddress getFrom() {
		return from;
	}

	public MessagePayload getMessage() {
		return message;
	}
	
	
}

