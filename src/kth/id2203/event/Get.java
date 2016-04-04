package kth.id2203.event;

import se.sics.kompics.KompicsEvent;

public class Get implements KompicsEvent {

	private final String processId;
	
	private final Integer key;
	
	public Get(Integer key) {
		this.key = key;
		this.processId = null;
	}
	
	public Get(Integer key, String processId) {
		this.key = key;
		this.processId = processId;
	}

	public Integer getKey() {
		return key;
	}

	public String getProcessId() {
		return processId;
	}
	
	
}