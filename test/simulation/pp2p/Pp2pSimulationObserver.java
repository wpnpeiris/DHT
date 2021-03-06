package simulation.pp2p;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.network.Network;
import se.sics.kompics.simulator.util.GlobalView;
import se.sics.kompics.timer.CancelPeriodicTimeout;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;

public class Pp2pSimulationObserver extends ComponentDefinition {

	private static final Logger log = LoggerFactory.getLogger(Pp2pSimulationObserver.class);
	
	Positive<Timer> timer = requires(Timer.class);
    Positive<Network> network = requires(Network.class);
 
    private final int numMessages;
    
    private UUID timerId;
    
    public Pp2pSimulationObserver(Init init) {
    	numMessages = init.numMessages;

        subscribe(handleStart, control);
        subscribe(handleCheck, timer);
    }
    
    Handler<Start> handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            schedulePeriodicCheck();
        }
    };
    
    @Override
    public void tearDown() {
        trigger(new CancelPeriodicTimeout(timerId), timer);
    }
    
    Handler<CheckTimeout> handleCheck = new Handler<CheckTimeout>() {
        @Override
        public void handle(CheckTimeout event) {
            GlobalView gv = config().getValue("simulation.globalview", GlobalView.class);
            
            if(gv.getValue("simulation.pp2p.numreceived", Integer.class) == numMessages) {
            	 log.info("Terminating simulation as the required messages:{} is received", numMessages);
            	gv.terminate();
            }
//            if(gv.getValue("simulation.pongs", Integer.class) > minPings) {
//                log.info("Terminating simulation as the minimum pings:{} is achieved", minPings);
//                gv.terminate();
//            }
//            if(gv.getDeadNodes().size() > minDeadNodes) {
//                log.info("Terminating simulation as the min dead nodes:{} is achieved", minDeadNodes);
//                gv.terminate();
//            }
        }
    };
    
    public static class Init extends se.sics.kompics.Init<Pp2pSimulationObserver> {

        public final int numMessages;

        public Init(int numMessages) {
            this.numMessages = numMessages;
        }
    }

    private void schedulePeriodicCheck() {
    	
        long period = config().getValue("simulation.pp2p.checktimeout", Long.class);
        SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(period, period);
        CheckTimeout timeout = new CheckTimeout(spt);
        spt.setTimeoutEvent(timeout);
        trigger(spt, timer);
        timerId = timeout.getTimeoutId();
    }

    public static class CheckTimeout extends Timeout {

        public CheckTimeout(SchedulePeriodicTimeout spt) {
            super(spt);
        }
    }
}
