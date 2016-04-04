package kth.id2203.register.port;

import kth.id2203.register.event.ArReadRequest;
import kth.id2203.register.event.ArReadResponse;
import kth.id2203.register.event.ArWriteRequest;
import kth.id2203.register.event.ArWriteResponse;
import se.sics.kompics.PortType;

public class AtomicRegister extends PortType {
	{
		indication(ArReadResponse.class);
		indication(ArWriteResponse.class);
		request(ArReadRequest.class);
		request(ArWriteRequest.class);
	}
}
