package kth.id2203.client.components;


import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kth.id2203.client.events.GetRequest;
import kth.id2203.client.events.PutRequest;
import kth.id2203.network.TAddress;
import kth.id2203.pp2p.event.P2PSend;
import kth.id2203.pp2p.port.Pp2pLinkPort;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;

public class Client extends ComponentDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(Client.class);
    private static byte GET = 1;
    private static byte PUT = 2;

    private Positive<Pp2pLinkPort> pp2p = requires(Pp2pLinkPort.class);
    private Integer key, value;
    private byte type;
    private TAddress self;
    private List<TAddress> all;

    public Client(Init init) {
        this.self = init.getSelf();
        this.all = init.getAllNodes();
        this.key = init.getKey();
        this.value = init.getValue();
        this.type = init.getRequestType();

        subscribe(startHandler, control);
    }

    Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            int rand = (new Random()).nextInt(all.size());
            TAddress dest = all.get(rand);
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(type == GET)
//                trigger(new P2PSend(dest, new GetRequest(self, key)), pp2p);
            	trigger(new P2PSend(dest, null), pp2p);
            else if(type == PUT) {
                LOG.info("------------------------");
                LOG.info(String.format("Sending PUT(%d, %d) to: %s", key, value, dest.toString()));
//                trigger(new P2PSend(dest, new PutRequest(self, key, value)), pp2p);
                trigger(new P2PSend(dest, null), pp2p);
            }
        }
    };

    public static class Init extends se.sics.kompics.Init<Client> {
        private final TAddress self;
        private List<TAddress> allNodes;
        private byte requestType;
        private Integer key, value;

        public Init(TAddress self, List<TAddress> allNodes, byte requestType, Integer key, Integer value) {
            this.self = self;
            this.allNodes = allNodes;
            this.requestType = requestType;
            this.key = key;
            this.value = value;
        }

        public TAddress getSelf() {
            return self;
        }

        public List<TAddress> getAllNodes() {
            return allNodes;
        }

        public byte getRequestType() {
            return requestType;
        }

        public Integer getKey() {
            return key;
        }

        public Integer getValue() {
            return value;
        }
    }
}
