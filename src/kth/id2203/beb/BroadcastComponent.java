package kth.id2203.beb;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kth.id2203.beb.event.BEBDeliver;
import kth.id2203.beb.event.BEBroadcast;
import kth.id2203.beb.port.BroadcastPort;
import kth.id2203.network.TAddress;
import kth.id2203.network.TMessage;
import kth.id2203.pp2p.event.P2PDeliver;
import kth.id2203.pp2p.event.P2PSend;
import kth.id2203.pp2p.port.Pp2pLinkPort;
import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;

public class BroadcastComponent extends ComponentDefinition {

	private static final Logger log = LoggerFactory.getLogger(BroadcastComponent.class);

	private TAddress self;
	private List<TAddress> all;

	private Negative<BroadcastPort> beb = provides(BroadcastPort.class);
	Positive<Pp2pLinkPort> p2pLinkPort = requires(Pp2pLinkPort.class);

	public BroadcastComponent(Init init) {
		this.self = init.self;
		this.all = init.all;
		log.info("Initiate broadcast component " + self.getIp() + ":" + self.getPort());
		
		subscribe(handleBroadcast, beb);
		subscribe(handleP2PMessage, p2pLinkPort);
	}
	
	private Handler<BEBroadcast> handleBroadcast = new Handler<BEBroadcast>() {

		@Override
		public void handle(BEBroadcast event) {
			log.info("BEBroadcast BEBMessage from " + self.getIp() + ":" + self.getPort());
			for (TAddress dest : all) {
				trigger(new P2PSend(dest, event.getMessage()), p2pLinkPort);
			}
		}

	};
    
	private Handler<P2PDeliver> handleP2PMessage = new Handler<P2PDeliver>() {

		@Override
		public void handle(P2PDeliver event) {
			log.info("BEBDeliver broadcast message [" + event.getMessage().getPayload() + "] at " + self.getIp() + ":" + self.getPort());
			trigger(new BEBDeliver(event.getFrom(), event.getMessage()), beb);

		}

	};

    
	public static class Init extends se.sics.kompics.Init<BroadcastComponent> {
		public final TAddress self;
		public List<TAddress> all;

		public Init(TAddress self, List<TAddress> all) {
			this.self = self;
			this.all = all;
		}
	}
}
