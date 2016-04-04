package simulation.register;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kth.id2203.event.Get;
import kth.id2203.network.TAddress;
import kth.id2203.network.TMessage;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.Transport;
import se.sics.kompics.timer.Timer;

public class ReadClient extends ComponentDefinition {
	
	private static final Logger log = LoggerFactory.getLogger(ReadClient.class);
	
	Positive<Network> network = requires(Network.class);
	Positive<Timer> timer = requires(Timer.class);
	
	private final TAddress self;
	private final TAddress dest;
	
	private final Integer dataKey;
	
	public ReadClient(Init init) {
		log.info("Initiate Read Client");
		this.self = init.self;
		this.dest = init.dest;
		this.dataKey = init.dataKey;
	}
	
	Handler<Start> startHandler = new Handler<Start>(){
		public void handle(Start event) {
			log.info("Issue GET on " + dest.getIp() + ":" + dest.getPort());
			trigger(new TMessage(self, dest, Transport.TCP, new Get(dataKey, self.getIp() + ":" + self.getPort())), network);
		}
	};
	
	{
		subscribe(startHandler, control);
	}
	
	public static class Init extends se.sics.kompics.Init<ReadClient> {
		public final TAddress self;
		public final TAddress dest;
		public final Integer dataKey;
		
		public Init(TAddress self, TAddress dest, Integer dataKey) {
			this.self = self;
			this.dest = dest;
			this.dataKey = dataKey;
		}
	}
}
