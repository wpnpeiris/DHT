package simulation.register;

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

public class SimpleReadWriteObserver extends ComponentDefinition {
	private static final Logger log = LoggerFactory.getLogger(SimpleReadWriteObserver.class);
	
	Positive<Timer> timer = requires(Timer.class);
    Positive<Network> network = requires(Network.class);
    
    private UUID timerId;
    
    public SimpleReadWriteObserver() {
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

            if(gv.getValue("simulation.register.write_response_received", Boolean.class) == true  &&
            		gv.getValue("simulation.register.read_response_value", String.class).equals(SimpleReadWriteScenario.DATA_VALUE) ) {
            	 log.info("Terminating simulation as the write response and read response value received");
            	 gv.terminate();
            }
        }
    };

    private void schedulePeriodicCheck() {
    	
        long period = config().getValue("simulation.register.checktimeout", Long.class);
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
