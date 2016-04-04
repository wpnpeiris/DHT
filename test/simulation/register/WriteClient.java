package simulation.register;

import org.omg.CORBA.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kth.id2203.event.Put;
import kth.id2203.network.TAddress;
import kth.id2203.network.TMessage;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.Transport;
import se.sics.kompics.timer.Timer;

public class WriteClient extends ComponentDefinition {
	
	private static final Logger log = LoggerFactory.getLogger(WriteClient.class);
	
	Positive<Network> network = requires(Network.class);
	Positive<Timer> timer = requires(Timer.class);
	
	private final TAddress self;
	private final TAddress dest;
	
	private final Integer dataKey;
	private final String dataValue;
	
	private final boolean faileAfter;
	
	public WriteClient(Init init) {
		log.info("Initiate Write Client");
		this.self = init.self;
		this.dest = init.dest;
		this.dataKey = init.dataKey;
		this.dataValue = init.dataValue;
		this.faileAfter = init.faileAfter;
	}
	
	Handler<Start> startHandler = new Handler<Start>(){
		public void handle(Start event) {
			log.info("Issue PUT on " + dest.getIp() + ":" + dest.getPort());
			trigger(new TMessage(self, dest, Transport.TCP, new Put(self.getIp() + ":" + self.getPort(), dataKey, dataValue)), network);
			if(faileAfter) {
				log.info("Kill the Write Clinet " +  self.getIp() + ":" + self.getPort());
				Thread.currentThread().interrupt();
			}
		}
	};
	
	{
		subscribe(startHandler, control);
	}
	
	public static class Init extends se.sics.kompics.Init<WriteClient> {
		public final TAddress self;
		public final TAddress dest;
		public final Integer dataKey;
		public final String dataValue;
		public final boolean faileAfter;
		
		public Init(TAddress self, TAddress dest, Integer dataKey, String dataValue) {
			this.self = self;
			this.dest = dest;
			this.dataKey = dataKey;
			this.dataValue = dataValue;
			this.faileAfter = false;
		}
		
		public Init(TAddress self, TAddress dest, Integer dataKey, String dataValue, boolean faileAfter) {
			this.self = self;
			this.dest = dest;
			this.dataKey = dataKey;
			this.dataValue = dataValue;
			this.faileAfter = faileAfter;
		}
	}
}
