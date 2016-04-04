package kth.id2203.epfd.event;


import kth.id2203.network.TAddress;
import se.sics.kompics.KompicsEvent;


public class Suspect implements KompicsEvent {
    private final TAddress source;

    public Suspect(TAddress source) {
        this.source = source;
    }

    public final TAddress getSource() {
        return source;
    }
}
