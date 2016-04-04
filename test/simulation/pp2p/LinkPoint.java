package simulation.pp2p;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kth.id2203.network.TAddress;
import kth.id2203.pp2p.event.P2PDeliver;
import kth.id2203.pp2p.event.P2PSend;
import kth.id2203.pp2p.port.Pp2pLinkPort;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.simulator.util.GlobalView;
import simulation.SimulatorComponent;

public class LinkPoint extends SimulatorComponent {
	private static final Logger log = LoggerFactory.getLogger(LinkPoint.class);

	Positive<Pp2pLinkPort> p2pLinkPort = requires(Pp2pLinkPort.class);
	private int numMessages;
	private boolean isSender;
	
	private int numMessageRecive = 0;
	
	public LinkPoint(Init init) {
		log.info("Initiate LinkPoint");
		this.numMessages = init.numMessages;
		this.isSender = init.isSender;
	}

	Handler<Start> startHandler = new Handler<Start>() {

		@Override
		public void handle(Start event) {
			if (isSender) {
				log.info("Send " + numMessages + " of messages");
				TAddress dest;
				try {
					dest = new TAddress(InetAddress.getByName("192.193.0.2"), 10000);
					for(int i = 0; i < numMessages; i++) {
						trigger(new P2PSend(dest), p2pLinkPort);
					}
					
					log.info(numMessages + " MESSAGES SENT !!!" );
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				
			}

		}

	};
	
	Handler<P2PDeliver> deliverHandler = new Handler<P2PDeliver>() {
		public void handle(P2PDeliver event) {
			numMessageRecive++;
			log.info(numMessageRecive + " MESSAGES RECEIVED FROM: " + event.getFrom().getIp() + ":" + event.getFrom().getPort());
			
			GlobalView gv = config().getValue("simulation.globalview", GlobalView.class);
	        gv.setValue("simulation.pp2p.numreceived", gv.getValue("simulation.pp2p.numreceived", Integer.class) + 1);
		}
	};

	{
		subscribe(startHandler, control);
		subscribe(deliverHandler, p2pLinkPort);
	}

	public static class Init extends se.sics.kompics.Init<LinkPoint> {
		public final int numMessages;
		public final boolean isSender;

		public Init(int numMessages, boolean isSender) {
			this.numMessages = numMessages;
			this.isSender = isSender;
		}
	}
}
