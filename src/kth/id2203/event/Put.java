package kth.id2203.event;

import se.sics.kompics.KompicsEvent;

public class Put implements KompicsEvent {

	private final String processId;
	
	private final Integer key;
	
	private final String value;
	
	public Put(Integer key, String value) {
		this.processId = null;
		this.key = key;
		this.value = value;
	}

	public Put(String processId, Integer key, String value) {
		this.processId = processId;
		this.key = key;
		this.value = value;
	}
	
	public Integer getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	public String getProcessId() {
		return processId;
	}
	
}