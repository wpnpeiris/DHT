package kth.id2203.client.events;



import kth.id2203.config.Grid;
import kth.id2203.network.TAddress;
import kth.id2203.pp2p.event.P2PDeliver;


public class PutReply extends P2PDeliver{
    private static final long serialVersionUID = 8777664112768146746L;
    private byte type;

    public PutReply(TAddress source) {
        super(source);

        this.type = Grid.PUTREPLY;
    }

    public byte getType() {
        return type;
    }
}
