package kth.id2203.message;

public class MessagePayload {
	private final String paylod;

	public MessagePayload(String paylod) {
		this.paylod = paylod;
	}
	
	public String getPayload() {
		return paylod;
	}
}
