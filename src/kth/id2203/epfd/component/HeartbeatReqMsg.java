package kth.id2203.epfd.component;


import kth.id2203.network.TAddress;
import kth.id2203.pp2p.event.P2PDeliver;


public class HeartbeatReqMsg extends P2PDeliver {

    private static final long serialVersionUID = 490215700829591158L;

    private final Integer sequenceNumber;

    public HeartbeatReqMsg(TAddress source, Integer sequenceNumber) {
        super(source);
        this.sequenceNumber = sequenceNumber;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }
}
