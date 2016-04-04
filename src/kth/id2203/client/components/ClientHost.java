package kth.id2203.client.components;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kth.id2203.network.TAddress;
import kth.id2203.pp2p.Pp2pLink;
import kth.id2203.pp2p.port.Pp2pLinkPort;
import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.netty.NettyInit;
import se.sics.kompics.network.netty.NettyNetwork;

public class ClientHost extends ComponentDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(ClientHost.class);

    public ClientHost(Init init) {
        Client.Init clInit = new Client.Init(init.getSelf(), init.getAllNodes(), (byte) 2, 30, 10);
        Component client = create(Client.class, clInit);
        Component pp2p = create(Pp2pLink.class, new Pp2pLink.Init(init.getSelf()));
        Component network = create(NettyNetwork.class, new NettyInit(init.self));

        connect(client.getNegative(Pp2pLinkPort.class), pp2p.getPositive(Pp2pLinkPort.class), Channel.TWO_WAY);
        connect(pp2p.getNegative(Network.class), network.getPositive(Network.class), Channel.TWO_WAY);
    }

    public static class Init extends se.sics.kompics.Init<ClientHost> {
        private final TAddress self;
        private List<TAddress> allNodes;

        public Init(TAddress self, List<TAddress> allNodes) {
            this.self = self;
            this.allNodes = allNodes;
        }

        public TAddress getSelf() {
            return self;
        }

        public List<TAddress> getAllNodes() {
            return allNodes;
        }
    }

}
