package kth.id2203.epfd.component;

import kth.id2203.network.TAddress;
import kth.id2203.pp2p.event.P2PDeliver;

public class HeartbeatRespMsg extends P2PDeliver {

    private static final long serialVersionUID = -306649641819166808L;
    private final Integer sequenceNumber;

    protected HeartbeatRespMsg(TAddress source, Integer sequenceNumber) {
        super(source);
        this.sequenceNumber = sequenceNumber;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }
}
