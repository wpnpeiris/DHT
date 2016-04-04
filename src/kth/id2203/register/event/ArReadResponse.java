package kth.id2203.register.event;


import se.sics.kompics.KompicsEvent;

public class ArReadResponse implements KompicsEvent {
	private final int key;
    private final String value;

    public ArReadResponse(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
