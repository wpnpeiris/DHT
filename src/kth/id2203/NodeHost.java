package kth.id2203;

import java.net.InetAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kth.id2203.beb.BroadcastComponent;
import kth.id2203.beb.port.BroadcastPort;
import kth.id2203.config.Grid;
import kth.id2203.epfd.component.Epfd;
import kth.id2203.epfd.port.EventuallyPerfectFailureDetector;
import kth.id2203.network.TAddress;
import kth.id2203.pp2p.Pp2pLink;
import kth.id2203.pp2p.port.Pp2pLinkPort;
import kth.id2203.register.ReadImposeWriteConsultMajority;
import kth.id2203.register.port.AtomicRegister;
import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.netty.NettyInit;
import se.sics.kompics.network.netty.NettyNetwork;


public class NodeHost extends ComponentDefinition {

	private static final Logger LOG = LoggerFactory.getLogger(NodeHost.class);
	
	public NodeHost(Init init) {
		LOG.info("Initialize NodeHost");
		TAddress self = init.self;
		List<TAddress> all = init.all;
		
		Component network = create(NettyNetwork.class, new NettyInit(self));
		Component pl = create(Pp2pLink.class, new Pp2pLink.Init(self));
		connect(pl.getNegative(Network.class), network.getPositive(Network.class), Channel.TWO_WAY);
		
		Component beb = create(BroadcastComponent.class, new BroadcastComponent.Init(self, all));
		connect(beb.getNegative(Pp2pLinkPort.class), pl.getPositive(Pp2pLinkPort.class));
		
		Component regsiter = create(ReadImposeWriteConsultMajority.class, new ReadImposeWriteConsultMajority.Init(self, all.size()));
		connect(regsiter.getNegative(BroadcastPort.class), beb.getPositive(BroadcastPort.class));
		connect(regsiter.getNegative(Pp2pLinkPort.class), pl.getPositive(Pp2pLinkPort.class));
		
		Component epfd = create(Epfd.class, new Epfd.Init(self, all, 1000, 1000));
		connect(epfd.getNegative(Pp2pLinkPort.class), pl.getPositive(Pp2pLinkPort.class));
		
		Component node = create(Node.class, new Node.Init(self));
		connect(node.getNegative(AtomicRegister.class), regsiter.getPositive(AtomicRegister.class));
		connect(node.getNegative(EventuallyPerfectFailureDetector.class), epfd.getPositive(EventuallyPerfectFailureDetector.class));
	}
	
	public static class Init extends se.sics.kompics.Init<NodeHost> {
		public final TAddress self;
		public List<TAddress> all;
		public Init(String ip, Integer port, Integer groupId) throws Exception {
			this.self = new TAddress(InetAddress.getByName(ip), port);
			this.all = Grid.getReplicationGroups().get(groupId).getGroup();
		}
	}
}
