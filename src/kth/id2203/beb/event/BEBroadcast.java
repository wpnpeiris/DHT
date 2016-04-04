package kth.id2203.beb.event;

import kth.id2203.message.MessagePayload;
import se.sics.kompics.KompicsEvent;

public class BEBroadcast implements KompicsEvent {

	private final MessagePayload message;

	public BEBroadcast(MessagePayload message) {
		this.message = message;
	}
	
	public MessagePayload getMessage() {
		return message;
	}

}
