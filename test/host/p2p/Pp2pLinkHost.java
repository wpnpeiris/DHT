package host.p2p;

import kth.id2203.network.TAddress;
import kth.id2203.pp2p.Pp2pLink;
import kth.id2203.pp2p.port.Pp2pLinkPort;
import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.netty.NettyInit;
import se.sics.kompics.network.netty.NettyNetwork;

public class Pp2pLinkHost extends ComponentDefinition {

	public Pp2pLinkHost(Init init) {

		Component network = create(NettyNetwork.class, new NettyInit(init.self));
		Component p2pLink = create(Pp2pLink.class, new Pp2pLink.Init(init.self));

		connect(p2pLink.getNegative(Network.class), network.getPositive(Network.class), Channel.TWO_WAY);

		Component p2pTest = create(Pp2pLinkApp.class,
				new Pp2pLinkApp.Init((init.self.getPort() == 20001 ? true : false), init.self, init.dest));
		connect(p2pTest.getNegative(Pp2pLinkPort.class), p2pLink.getPositive(Pp2pLinkPort.class));

	}
	
	public static class Init extends se.sics.kompics.Init<Pp2pLinkHost> {
		public final TAddress self;
		public final TAddress dest;
		public Init(TAddress self, TAddress dest) {
			this.self = self;
			this.dest = dest;
		}
	}
}
