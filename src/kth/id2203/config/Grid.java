package kth.id2203.config;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kth.id2203.network.TAddress;
import kth.id2203.util.Hasher;


public class Grid {

    private static final int DELTA = 3;
    private static final int NODESCOUNT = 9;
    private static final int KEYSPERGROUP = 20;

    public static final byte GETREQ = 3;
    public static final byte PUTREQ = 4;
    public static final byte GETREPLY = 5;
    public static final byte PUTREPLY = 6;

    public static final byte BEB = 7;
    public static final byte P2P = 8;

    public static TAddress getClientAddress() {
        try {
            InetAddress baseIP = InetAddress.getByName("127.0.0.1");
            int basePort = 20000 + NODESCOUNT;

            return new TAddress(baseIP, basePort);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<TAddress> getAllNodes() {
        try {
            // Create the static 6 nodes
            List<TAddress> nodes = new ArrayList<>();
            InetAddress baseIP = InetAddress.getByName("127.0.0.1");
            int basePort = 20000;

            for (int i = 0; i < NODESCOUNT; i++) {
                nodes.add(new TAddress(baseIP, basePort + i));
            }

            return nodes;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<ReplicationGroup> getReplicationGroups() {
//        List<TAddress> allNodes = getAllNodes();
//        List<ReplicationGroup> group = new ArrayList<>();
//
//        int nodesLeft = NODESCOUNT, temp = 0, previousBound = 0, previousIndex = 0, loop = nodesLeft / DELTA;
//        for (int i = 0; i < loop; i++) {
//            temp = nodesLeft - DELTA;
//            ReplicationGroup r = new ReplicationGroup(i, previousBound, previousBound + KEYSPERGROUP);
//            if(temp < DELTA) {
//                for (int j = 0; j < nodesLeft; j++)
//                    r.addToGroup(allNodes.get(previousIndex + j));
//                group.add(r);
//                break;
//            }
//            else {
//                for (int j = 0; j < DELTA; j++)
//                    r.addToGroup(allNodes.get(previousIndex + j));
//                previousBound += KEYSPERGROUP;
//                previousIndex += DELTA;
//                nodesLeft -= DELTA;
//                group.add(r);
//            }
//        }

        return new ArrayList(loadReplicationGroups().values());
    }
    
    public static ReplicationGroup getReplicationGroup(int id) {
    	return loadReplicationGroups().get(id);
    }
    
    private static Map<Integer, ReplicationGroup> loadReplicationGroups() {
    	List<TAddress> allNodes = getAllNodes();
        Map<Integer, ReplicationGroup> group = new HashMap<>();

        int nodesLeft = NODESCOUNT, temp = 0, previousBound = 0, previousIndex = 0, loop = nodesLeft / DELTA;
        for (int i = 0; i < loop; i++) {
            temp = nodesLeft - DELTA;
            ReplicationGroup r = new ReplicationGroup(i, previousBound, previousBound + KEYSPERGROUP);
            if(temp < DELTA) {
                for (int j = 0; j < nodesLeft; j++)
                    r.addToGroup(allNodes.get(previousIndex + j));
                group.put(i, r);
                break;
            }
            else {
                for (int j = 0; j < DELTA; j++)
                    r.addToGroup(allNodes.get(previousIndex + j));
                previousBound += KEYSPERGROUP;
                previousIndex += DELTA;
                nodesLeft -= DELTA;
                group.put(i, r);
            }
        }
        
        return group;

    }

    public static ReplicationGroup getReplicaGroupByAddress(TAddress addr) {
        for (ReplicationGroup r: getReplicationGroups()) {
            if(r.contains(addr))
                return r;
        }
        // Shouldn't happen
        return null;
    }

    public static ReplicationGroup getReplicaGroupByKey(int key) {
        int hashedKey = Hasher.hash(key, getTotalKeys());
        for (ReplicationGroup r: getReplicationGroups()) {
            if(r.inRange(hashedKey))
                return r;
        }
        // Shouldn't happen
        return null;
    }

    private static int getTotalKeys() {
        return KEYSPERGROUP * (NODESCOUNT/DELTA);
    }
}
