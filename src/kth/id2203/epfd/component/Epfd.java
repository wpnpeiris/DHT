package kth.id2203.epfd.component;



import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kth.id2203.epfd.event.Restore;
import kth.id2203.epfd.event.Suspect;
import kth.id2203.epfd.port.EventuallyPerfectFailureDetector;
import kth.id2203.network.TAddress;
import kth.id2203.pp2p.event.P2PSend;
import kth.id2203.pp2p.port.Pp2pLinkPort;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

public class Epfd extends ComponentDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(Epfd.class);

    private Positive<Timer> timer = requires(Timer.class);
    private Positive<Pp2pLinkPort> pp2p = requires(Pp2pLinkPort.class);
    private Negative<EventuallyPerfectFailureDetector> epfd = provides(EventuallyPerfectFailureDetector.class);

    private Set<TAddress> alive;
    private Set<TAddress> suspected;
    private long delay;
    private long delta;
    private Integer sequenceNo;
    private final TAddress self;
    private final List<TAddress> allNodes;
    private ScheduleTimeout timeout;

    public Epfd(Init init) {
        self = init.getSelf();
        allNodes = init.getAll();

        delay = init.getInitialDelay();
        delta = init.getDeltaDelay();
        sequenceNo = 0;
        alive = new HashSet<>(init.getAll());
        suspected = new HashSet<>();

        subscribe(startHandler, control);
        subscribe(handleCheckTimeout, timer);
        subscribe(handleHbRequests, pp2p);
        subscribe(handleHbReplies, pp2p);
    }

    private void setTimer(long delay) {
        timeout = new ScheduleTimeout(delay);
        timeout.setTimeoutEvent(new CheckTimeout(timeout));
        trigger(timeout, timer);
    }

    Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start start) {
            /*
            upon event ⟨ ✸P, Init ⟩ do
                alive := Π;
                suspected := ∅;
                delay := ∆;
                starttimer(delay);
            */
            LOG.info("Epfd started at " + self.toString());
            setTimer(delay);
        }
    };

    private Handler<CheckTimeout> handleCheckTimeout = new Handler<CheckTimeout>() {

        /*
        upon event ⟨ Timeout ⟩ do
            if alive ∩ suspected ̸= ∅ then
                delay := delay + ∆;
            forall p ∈ Π do
                if (p ̸∈ alive) ∧ (p ̸∈ suspected) then
                    suspected := suspected ∪ {p};
                    trigger ⟨ ✸P, Suspect | p ⟩;
                else if (p ∈ alive) ∧ (p ∈ suspected) then
                    suspected := suspected \ {p};
                    trigger ⟨ ✸P, Restore | p ⟩;
                trigger ⟨ pl, Send | p, [HEARTBEATREQUEST] ⟩;
            alive := ∅;
            starttimer(delay);
         */

        @Override
        public void handle(CheckTimeout event) {
            for (TAddress node : alive) {
                if (suspected.contains(node)) {
                    LOG.info("New timeout delay is: {}", delay);
                    delay += delta;
                    break;
                }
            }
            sequenceNo++;

            for (TAddress node : allNodes) {
                if ((!alive.contains(node)) && (!suspected.contains(node))) {
                    suspected.add(node);
                    Suspect suspectEvent = new Suspect(node);
                    trigger(suspectEvent, epfd);
                } else if (alive.contains(node) && suspected.contains(node)) {
                    suspected.remove(node);
                    Restore restoreEvent = new Restore(node);
                    trigger(restoreEvent, epfd);
                }

                // Send Heartbeat request to all nodes
                HeartbeatReqMsg hbRequestMsg = new HeartbeatReqMsg(
                        self, sequenceNo);
                trigger(new P2PSend(node, null), pp2p);
            }

            alive.clear();

            // Reschedule timer
            setTimer(delay);
        }
    };

    private Handler<HeartbeatReqMsg> handleHbRequests = new Handler<HeartbeatReqMsg>() {
        /*
            upon event ⟨ pl, Deliver | q, [HEARTBEATREQUEST] ⟩ do
                trigger ⟨ pl, Send | q, [HEARTBEATREPLY] ⟩;
        */
        @Override
        public void handle(HeartbeatReqMsg event) {
            //logger.info("Received HB Request from node: " + event.getSource());
            HeartbeatRespMsg hbRespMsg = new HeartbeatRespMsg(self, event.getSequenceNumber());
            trigger(new P2PSend(event.getFrom(), null), pp2p);
        }
    };

    // Handle Heartbeat replies
    private Handler<HeartbeatRespMsg> handleHbReplies = new Handler<HeartbeatRespMsg>() {
        /*
        upon event ⟨ pl, Deliver | p, [HEARTBEATREPLY] ⟩ do
            alive := alive ∪ {p};
         */

        @Override
        public void handle(HeartbeatRespMsg event) {
            if (event.getSequenceNumber().equals(sequenceNo)
                    || suspected.contains(event.getFrom())) {
                LOG.info("Received reply from :" + event.getFrom());
                alive.add(event.getFrom());
            }
        }
    };

    public static class Init extends se.sics.kompics.Init<Epfd> {
        private final TAddress self;
        private List<TAddress> all;
        private final long initialDelay;
        private final long deltaDelay;

        public Init(TAddress self, List<TAddress> all, long initialDelay, long deltaDelay) {
            this.self = self;
            this.all = all;
            this.initialDelay = initialDelay;
            this.deltaDelay = deltaDelay;
        }

        public TAddress getSelf() {
            return self;
        }

        public List<TAddress> getAll() {
            return all;
        }

        public long getInitialDelay() {
            return initialDelay;
        }

        public long getDeltaDelay() {
            return deltaDelay;
        }
    }
}
