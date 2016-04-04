package kth.id2203.pp2p;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kth.id2203.message.MessagePayload;
import kth.id2203.network.TAddress;
import kth.id2203.network.TMessage;
import kth.id2203.pp2p.event.P2PAckDeliver;
import kth.id2203.pp2p.event.P2PAckMessage;
import kth.id2203.pp2p.event.P2PAckSend;
import kth.id2203.pp2p.event.P2PDeliver;
import kth.id2203.pp2p.event.P2PMessage;
import kth.id2203.pp2p.event.P2PSend;
import kth.id2203.pp2p.port.Pp2pLinkPort;
import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.Transport;
import se.sics.kompics.timer.Timer;

public class Pp2pLink extends ComponentDefinition {

	private static final Logger log = LoggerFactory.getLogger(Pp2pLink.class);
	
	private TAddress self;
	
	private Positive<Network> network = requires(Network.class);
	private Positive<Timer> timer = requires(Timer.class);
	private Negative<Pp2pLinkPort> p2plink = provides(Pp2pLinkPort.class);
	
	
	public Pp2pLink(Init init) {
		this.self = init.self;
		log.info("Initiate Pp2pLink component " + self.getIp() + ":" + self.getPort());
		
		subscribe(handleSend, p2plink);
		subscribe(handleAck, p2plink);
		subscribe(handleDeliver, network);
		subscribe(handleAckDeliver, network);
	}
	
	
	private Handler<P2PAckSend> handleAck = new Handler<P2PAckSend>() {

		@Override
		public void handle(P2PAckSend event) {
			log.info("Trigger Ack from " + self.getIp() + ":" + self.getPort());
			P2PAckMessage msg = new P2PAckMessage(event.getMessage());
			trigger(new TMessage(self, event.getTo(), Transport.TCP, msg), network);
		}
		
	};
	
	
	private Handler<P2PSend> handleSend = new Handler<P2PSend>() {

		@Override
		public void handle(P2PSend event) {
			log.info("Trigger P2PSend from " + self.getIp() + ":" + self.getPort());
				P2PMessage msg = new P2PMessage(event.getMessage());
				trigger(new TMessage(self, event.getTo(), Transport.TCP, msg), network);
		}
		
	};
	
	ClassMatchedHandler<P2PMessage, TMessage> handleDeliver = new ClassMatchedHandler<P2PMessage, TMessage>() {

        @Override
        public void handle(P2PMessage event, TMessage context) {
        	log.info("P2PMessage deliver at " + self.getIp() + ":" + self.getPort());
			trigger(new P2PDeliver(context.getSource(), event.getMessage()), p2plink);
        }
    };
    
    ClassMatchedHandler<P2PAckMessage, TMessage> handleAckDeliver = new ClassMatchedHandler<P2PAckMessage, TMessage>() {

        @Override
        public void handle(P2PAckMessage event, TMessage context) {
        	log.info("P2PAckMessage deliver at " + self.getIp() + ":" + self.getPort());
			trigger(new P2PAckDeliver(context.getSource(), event.getMessage()), p2plink);
        }
    };
    
	public static class Init extends se.sics.kompics.Init<Pp2pLink> {
		public final TAddress self;
		
		public Init(TAddress self) {
			this.self = self;
		}
	}
}
