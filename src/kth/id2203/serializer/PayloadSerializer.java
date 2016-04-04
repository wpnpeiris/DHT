package kth.id2203.serializer;

import com.google.common.base.Optional;

import io.netty.buffer.ByteBuf;
import kth.id2203.beb.event.BEBMessage;
import kth.id2203.message.MessagePayload;
import kth.id2203.pp2p.event.P2PAckMessage;
import kth.id2203.pp2p.event.P2PMessage;
import se.sics.kompics.network.netty.serialization.Serializer;
import se.sics.kompics.network.netty.serialization.Serializers;

public class PayloadSerializer implements Serializer {

    private static final byte GET = 3;
    private static final byte PUT = 4;
    private static final byte REPLY = 5;
    
    private static final byte BEB = 7;
    private static final byte P2P = 8;
    private static final byte ACK = 9;
    private static final byte PAYLOAD = 10;
    
    @Override
    public int identifier() {
        return 200;
    }

    @Override
    public void toBinary(Object o, ByteBuf buf) {
    	if(o instanceof BEBMessage) {
    		BEBMessage r = (BEBMessage) o;
            buf.writeByte(BEB);
        } else if(o instanceof P2PMessage) {
        	P2PMessage r = (P2PMessage) o;
            buf.writeByte(P2P);
            Serializers.toBinary(r.getMessage(), buf);
        } else if(o instanceof P2PAckMessage) {
        	P2PAckMessage r = (P2PAckMessage) o;
            buf.writeByte(ACK);
            Serializers.toBinary(r.getMessage(), buf);
        } else if(o instanceof MessagePayload) {
        	MessagePayload r = (MessagePayload) o;
        	buf.writeByte(PAYLOAD);
        	Serializers.toBinary(r.getPayload(), buf);
        }

    }

    @Override
    public Object fromBinary(ByteBuf buf, Optional<Object> hint) {
        byte type = buf.readByte(); 
        switch (type) {
        case BEB: {
            return new BEBMessage();
        } 
        
        case P2P: {
        	MessagePayload payload = (MessagePayload) Serializers.fromBinary(buf, Optional.absent());
        	return new P2PMessage(payload);
        } 
          
        case ACK: {
        	MessagePayload payload = (MessagePayload) Serializers.fromBinary(buf, Optional.absent());
        	return new P2PAckMessage(payload);
        } 
            
        case PAYLOAD: {
        	String payload = (String) Serializers.fromBinary(buf, Optional.absent());
            return new MessagePayload(payload);
        } 
        
        }
        return null;
    }
}
