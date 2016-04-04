package simulation.beb;

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

public class BebSimulationObserver extends ComponentDefinition {

	private static final Logger log = LoggerFactory.getLogger(BebSimulationObserver.class);
	
	Positive<Timer> timer = requires(Timer.class);
    Positive<Network> network = requires(Network.class);
 
    private final int numNodes;
    
    private UUID timerId;
    
    public BebSimulationObserver(Init init) {
    	numNodes = init.numNodes;

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

            if(gv.getValue("simulation.beb.numreceived", Integer.class) == numNodes ) {
            	 log.info("Terminating simulation as the messages is received at :{} nodes", numNodes);
            	 gv.terminate();
            }
        }
    };
    
    public static class Init extends se.sics.kompics.Init<BebSimulationObserver> {

        public final int numNodes;

        public Init(int numNodes) {
            this.numNodes = (numNodes + 1);
        }
    }

    private void schedulePeriodicCheck() {
    	
        long period = config().getValue("simulation.beb.checktimeout", Long.class);
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
