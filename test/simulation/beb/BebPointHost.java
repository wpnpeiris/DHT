package simulation.beb;

import java.util.List;

import kth.id2203.beb.BroadcastComponent;
import kth.id2203.beb.port.BroadcastPort;
import kth.id2203.network.TAddress;
import kth.id2203.pp2p.Pp2pLink;
import kth.id2203.pp2p.port.Pp2pLinkPort;
import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;

public class BebPointHost extends ComponentDefinition {
	
	Positive<Timer> timer = requires(Timer.class);
	Positive<Network> network = requires(Network.class);
	
	public BebPointHost(Init init) {

//		Component network = create(NettyNetwork.class, new NettyInit(init.self));
		Component p2pLink = create(Pp2pLink.class, new Pp2pLink.Init(init.self));
		connect(p2pLink.getNegative(Network.class), network, Channel.TWO_WAY);
		
		Component boradcast = create(BroadcastComponent.class, new BroadcastComponent.Init(init.self, init.all));
		connect(boradcast.getNegative(Pp2pLinkPort.class), p2pLink.getPositive(Pp2pLinkPort.class));
		
		Component bebPoint = create(BebPoint.class,
				new BebPoint.Init(init.isBroadcaster));
		connect(bebPoint.getNegative(BroadcastPort.class), boradcast.getPositive(BroadcastPort.class));

	}

	public static class Init extends se.sics.kompics.Init<BebPointHost> {
		public final TAddress self;
		public List<TAddress> all;
		public boolean isBroadcaster;
		
		public Init(TAddress self, List<TAddress> all, boolean isBroadcaster) {
			this.self = self;
			this.all = all;
			this.isBroadcaster = isBroadcaster;
		}
	}
}
