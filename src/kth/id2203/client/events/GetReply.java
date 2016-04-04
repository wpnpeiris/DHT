package kth.id2203.client.events;



import kth.id2203.config.Grid;
import kth.id2203.network.TAddress;
import kth.id2203.pp2p.event.P2PDeliver;


public class GetReply extends P2PDeliver {
    private static final long serialVersionUID = -4817090990532012025L;
    private int key, value;
    private byte type;

    public GetReply(TAddress source, int key, int value) {
        super(source);

        this.key = key;
        this.value = value;
        this.type = Grid.GETREPLY;
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
