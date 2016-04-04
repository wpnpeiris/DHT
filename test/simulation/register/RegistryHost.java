package simulation.register;

import java.util.List;

import kth.id2203.beb.BroadcastComponent;
import kth.id2203.beb.port.BroadcastPort;
import kth.id2203.network.TAddress;
import kth.id2203.pp2p.Pp2pLink;
import kth.id2203.pp2p.port.Pp2pLinkPort;
import kth.id2203.register.ReadImposeWriteConsultMajority;
import kth.id2203.register.port.AtomicRegister;
import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;

public class RegistryHost extends ComponentDefinition {

	Positive<Timer> timer = requires(Timer.class);
	Positive<Network> network = requires(Network.class);
	
	public RegistryHost(Init init) {
		Component pl = create(Pp2pLink.class, new Pp2pLink.Init(init.self));
		connect(pl.getNegative(Network.class), network, Channel.TWO_WAY);

		Component beb = create(BroadcastComponent.class, new BroadcastComponent.Init(init.self, init.all));
		connect(beb.getNegative(Pp2pLinkPort.class), pl.getPositive(Pp2pLinkPort.class));

		Component regsiter = create(ReadImposeWriteConsultMajority.class,
				new ReadImposeWriteConsultMajority.Init(init.self, init.all.size()));
		connect(regsiter.getNegative(BroadcastPort.class), beb.getPositive(BroadcastPort.class));
		connect(regsiter.getNegative(Pp2pLinkPort.class), pl.getPositive(Pp2pLinkPort.class));

		Component timer = create(JavaTimer.class, Init.NONE);

		Component testapp = create(RegistryApp.class, Init.NONE);
		connect(testapp.getNegative(AtomicRegister.class), regsiter.getPositive(AtomicRegister.class));
		connect(testapp.getNegative(Timer.class), timer.getPositive(Timer.class));

		connect(testapp.getNegative(Network.class), network, Channel.TWO_WAY);
	}

	public static class Init extends se.sics.kompics.Init<RegistryHost> {
		public final TAddress self;
		public final List<TAddress> all;
		
		public Init(TAddress self, List<TAddress> all) {
			this.self = self;
			this.all = all;
		}
	}
}
