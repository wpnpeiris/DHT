package kth.id2203.register.event;


import se.sics.kompics.KompicsEvent;

public class ArReadRequest implements KompicsEvent {
	private final int key;
	
	public ArReadRequest(int key) {
		this.key = key;
	}

	public int getKey() {
		return key;
	}
	
	
}
