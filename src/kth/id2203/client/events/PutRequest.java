package kth.id2203.client.events;


import kth.id2203.config.Grid;
import kth.id2203.network.TAddress;
import kth.id2203.pp2p.event.P2PDeliver;

public class PutRequest extends P2PDeliver {
    private static final long serialVersionUID = 7429988893396340783L;
    // PutRequest(key, value): contains the key and the value to be stored
    private int key, value;
    private byte type;

    public PutRequest(TAddress source, int key, int value) {
        super(source);

        this.key = key;
        this.value = value;
        this.type = Grid.PUTREQ;
    }

    public int getKey() {
        return key;
    }

    public int getValue() {
        return value;
    }


    public byte getType() {
        return type;
    }
}
