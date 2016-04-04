package kth.id2203.epfd.event;


import kth.id2203.network.TAddress;
import se.sics.kompics.KompicsEvent;


public class Restore implements KompicsEvent {
    private final TAddress source;

    public Restore(TAddress source) {
        this.source = source;
    }

    public final TAddress getSource() {
        return source;
    }
}

