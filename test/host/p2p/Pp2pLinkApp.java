package host.p2p;

import kth.id2203.network.TAddress;
import kth.id2203.pp2p.event.P2PDeliver;
import kth.id2203.pp2p.event.P2PSend;
import kth.id2203.pp2p.port.Pp2pLinkPort;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;

public class Pp2pLinkApp extends ComponentDefinition {

	Positive<Pp2pLinkPort> p2pLinkPort = requires(Pp2pLinkPort.class);
	boolean triggerTestMessage;
	TAddress self;
	TAddress dest;
	
	public Pp2pLinkApp(Init init) {
		System.out.println("Init Pp2pLinkApp: " + init.triggerTestMessage);
		this.triggerTestMessage = init.triggerTestMessage;
		this.self = init.self;
		this.dest = init.dest;		
	}
	
	Handler<Start> startHandler = new Handler<Start>() {
		public void handle(Start event) {
			System.out.println("startHandler " + triggerTestMessage);
			if (triggerTestMessage) {
				System.out.println("Triger Test P2P message");
				trigger(new P2PSend(dest), p2pLinkPort);
			}
		}
	};
	
	Handler<P2PDeliver> deliverHandler = new Handler<P2PDeliver>() {
		public void handle(P2PDeliver event) {
			System.out.println("Got a Deliver Event from: " + event.getFrom().getIp() + ":" + event.getFrom().getPort());
		}
	};
	
	{
		subscribe(startHandler, control);
		subscribe(deliverHandler, p2pLinkPort);
	}
	
	public static class Init extends se.sics.kompics.Init<Pp2pLinkApp> {
		public final boolean triggerTestMessage;
		public final TAddress self;
		public final TAddress dest;
		
		public Init(boolean triggerTestMessage, TAddress self, TAddress dest) {
			this.triggerTestMessage = triggerTestMessage;
			this.self = self;
			this.dest = dest;
		}
	}
}
