package host;

import kth.id2203.beb.event.BEBDeliver;
import kth.id2203.beb.event.BEBroadcast;
import kth.id2203.beb.port.BroadcastPort;
import kth.id2203.message.MessagePayload;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;

public class BroadcastTestApp extends ComponentDefinition {

	Positive<BroadcastPort> broadcastPort = requires(BroadcastPort.class);

	boolean triggerTestMessage;

	public BroadcastTestApp(Init init) {
		System.out.println("Trigger Test Broad Cast message: " + init.triggerTestMessage);
		this.triggerTestMessage = init.triggerTestMessage;
	}

	Handler<Start> startHandler = new Handler<Start>() {
		public void handle(Start event) {
			if (triggerTestMessage) {
				System.out.println("Triger Test Broadcast message");
				trigger(new BEBroadcast(new MessagePayload("TEST Message")), broadcastPort);
			}
		}
	};

	Handler<BEBDeliver> deliverHandler = new Handler<BEBDeliver>() {
		public void handle(BEBDeliver event) {
			System.out.println("Got a Deliver Event!");
		}
	};


	{
		subscribe(startHandler, control);
		subscribe(deliverHandler, broadcastPort);
	}

	public static class Init extends se.sics.kompics.Init<BroadcastTestApp> {
		public final boolean triggerTestMessage;

		public Init(boolean triggerTestMessage) {
			this.triggerTestMessage = triggerTestMessage;
		}
	}
}
