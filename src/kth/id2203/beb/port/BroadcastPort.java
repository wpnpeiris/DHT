package kth.id2203.beb.port;

import kth.id2203.beb.event.BEBDeliver;
import kth.id2203.beb.event.BEBroadcast;
import se.sics.kompics.PortType;

public class BroadcastPort extends PortType {
	{
		request(BEBroadcast.class);
		indication(BEBDeliver.class);
	}
}
