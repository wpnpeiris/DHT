package kth.id2203.client.events;

import kth.id2203.config.Grid;
import kth.id2203.network.TAddress;
import kth.id2203.pp2p.event.P2PDeliver;


public class GetRequest extends P2PDeliver {
    private static final long serialVersionUID = 8335028469218096522L;
    // GetRequest(key): send a request to retrieve a value based on a key
    private int key;
    private byte type;

    public GetRequest(TAddress source, int key) {
        super(source);

        this.key = key;
        this.type = Grid.GETREQ;
    }

    public int getKey() {
        return key;
    }

    public byte getType() {
        return type;
    }
}
