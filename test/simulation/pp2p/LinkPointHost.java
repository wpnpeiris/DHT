package simulation.pp2p;

import kth.id2203.network.TAddress;
import kth.id2203.pp2p.Pp2pLink;
import kth.id2203.pp2p.port.Pp2pLinkPort;
import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;

public class LinkPointHost extends ComponentDefinition {

	Positive<Timer> timer = requires(Timer.class);
	Positive<Network> network = requires(Network.class);
	
	public LinkPointHost(Init init) {
		Component p2pLink = create(Pp2pLink.class, new Pp2pLink.Init(init.self));
		connect(p2pLink.getNegative(Network.class), network, Channel.TWO_WAY);
		
		Component linkPoint = create(LinkPoint.class,new LinkPoint.Init( init.numMessages, init.isSender));
		connect(linkPoint.getNegative(Pp2pLinkPort.class), p2pLink.getPositive(Pp2pLinkPort.class));
	}
	
	public static class Init extends se.sics.kompics.Init<LinkPointHost> {
		public final TAddress self;
		public final int numMessages;
		public final boolean isSender;

		public Init(TAddress self, int numMessages, boolean isSender) {
			this.self = self;
			this.numMessages = numMessages;
			this.isSender = isSender;
		}
		
		public Init(TAddress self, boolean isSender) {
			this.self = self;
			this.isSender = isSender;
			this.numMessages = -1;
		}
	}
}
