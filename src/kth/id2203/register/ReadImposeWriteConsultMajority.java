package kth.id2203.register;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kth.id2203.beb.event.BEBDeliver;
import kth.id2203.beb.event.BEBroadcast;
import kth.id2203.beb.port.BroadcastPort;
import kth.id2203.message.MessagePayload;
import kth.id2203.network.TAddress;
import kth.id2203.pp2p.event.P2PAckDeliver;
import kth.id2203.pp2p.event.P2PAckSend;
import kth.id2203.pp2p.port.Pp2pLinkPort;
import kth.id2203.register.event.ArReadRequest;
import kth.id2203.register.event.ArReadResponse;
import kth.id2203.register.event.ArWriteRequest;
import kth.id2203.register.event.ArWriteResponse;
import kth.id2203.register.port.AtomicRegister;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;

public class ReadImposeWriteConsultMajority extends ComponentDefinition {
	private static final Logger log = LoggerFactory.getLogger(ReadImposeWriteConsultMajority.class);

	private static final String READ_OPT = "READ";
	private static final String WRITE_OPT = "WRITE";
	
	private static final String READ_ACK = "VALUE";
	private static final String WRITE_ACK = "ACK";
	
	private Positive<BroadcastPort> beb = requires(BroadcastPort.class);
	private Positive<Pp2pLinkPort> pp2p = requires(Pp2pLinkPort.class);
	private Negative<AtomicRegister> nnar = provides(AtomicRegister.class);

	private TAddress self;
	private Integer numNodes;
	
//	private Integer ts, wr, val;
	private Map<Integer, Data> keyData;
	
//	private List<ReadInfo> readlist;
	private Map<Integer, ArrayList<ReadInfo>> readlist;
	
//	private Integer rid;
	private Map<Integer, Integer> rid;
	
//	private Integer acks;
	private Map<Integer, Integer> acks;
	
//	private boolean reading;
	private Map<Integer, Boolean> reading;
	
//	private Integer readkey;
//    private String readval;
	private Map<Integer, Val> readval = new HashMap<Integer, Val>();
    
//    private Integer rr;
	private Map<Integer, Integer> rr = new HashMap<Integer, Integer>();
//    private Integer maxts;
	private Map<Integer, Integer> maxts = new HashMap<Integer, Integer>();
    
//    private Integer writekey;
//	private String writeval;
    private Map<Integer, Val> writeval = new HashMap<Integer, Val>();

	public ReadImposeWriteConsultMajority(Init init) {
		log.info("Initiate ReadImposeWriteConsultMajority Component");
		this.self = init.self;
		this.numNodes = init.numNodes;

//		this.ts = 0;
//        this.val = 0; // This is the value of the register we are trying to read
//        this.wr = 0;
		this.keyData  = new HashMap<Integer, Data>();
        
		this.readlist = new HashMap<Integer, ArrayList<ReadInfo>>();
		
//		this.rid = 0;
		this.rid = new HashMap<Integer, Integer>();
//		this.acks = 0;
		this.acks = new HashMap<Integer, Integer>();
		
//		this.reading = false;
		this.reading = new HashMap<Integer, Boolean>();
		
		
	}

	private Handler<ArReadRequest> handleReadRequest = new Handler<ArReadRequest>() {

		/**
		 * upon event ⟨ nnar, Read ⟩ do
         *       rid := rid + 1;
         *       acks := 0;
         *       readlist := [⊥]N ;
         *       reading := TRUE;
         *       trigger ⟨ beb, Broadcast | [READ, rid] ⟩;
		 */
		@Override
		public void handle(ArReadRequest event) {
			log.info("R1: Handle ArReadRequest at " + self.getIp() + ":" + self.getPort());

//			rid++;
			if(rid.containsKey(event.getKey())) {
				rid.put(event.getKey(), (rid.get(event.getKey()) + 1));
			} else {
				rid.put(event.getKey(), 1);
			}
			
//			acks = 0;
			acks.put(event.getKey(), 0);
			
			if(readlist.containsKey(event.getKey())) {
				readlist.get(event.getKey()).clear();
			} else {
				readlist.put(event.getKey(), new ArrayList<ReadInfo>());
			}
			
			reading.put(event.getKey(), true);

			MessagePayload message = new MessagePayload(READ_OPT + "," + rid.get(event.getKey()) + "," + event.getKey());
			log.info("R2: Broadcast Read Request [" + message.getPayload() + "] at" + self.getIp() + ":" + self.getPort());
			trigger(new BEBroadcast(message), beb);

		}

	};

	private Handler<BEBDeliver> bebDeliver = new Handler<BEBDeliver>() {

		@Override
		public void handle(BEBDeliver event) {
			String request = event.getMessage().getPayload();
			if(request.contains(READ_OPT)) {
				handleRead(event.getFrom(), request);
			} else if(request.contains(WRITE_OPT)) {
				handleWrite(event.getFrom(), request);
			}
			
		}
		
		/**
		 * upon event ⟨ beb, Deliver | p, [READ, r] ⟩ do
         *       trigger ⟨ pl, Send | p, [VALUE, r, ts, wr, val] ⟩;
         *       
		 * @param src
		 * @param readRequest
		 */
		private void handleRead(TAddress src, String readRequest) {
			log.info("R3/W3: Handle Broadcast READ Request [" + readRequest + "] at " + self.getIp() + ":" + self.getPort());
			String[] request = readRequest.split(",");
			String r = request[1];
			Integer key = Integer.valueOf(request[2]);
			
			Data data;
			
			if(keyData.containsKey(key)) {
				data = keyData.get(key);
			} else {
				data = new Data(0, 0, null);
				keyData.put(key, data);
			}
			
			MessagePayload message = new MessagePayload(READ_ACK + "," + r + "," + data.getTs() + "," + data.getWr() + "," + key + "," + data.getVal());
			log.info("R4/W4: Send Ack [" + message.getPayload() + "] from " + self.getIp() + ":"+ self.getPort());
			trigger(new P2PAckSend(src, message), pp2p);
			
		}
		
		/**
		 * upon event ⟨ beb, Deliver | p, [WRITE, r, ts′, wr′, v′] ⟩ do
		 * 		if (ts′, wr′) is larger than (ts, wr) then
		 * 			(ts, wr, val) := (ts′, wr′, v′);
		 * 		trigger ⟨ pl, Send | p, [ACK, r] ⟩;
		 * 
		 * @param writeRequest
		 */
		private void handleWrite(TAddress src, String writeRequest) {
			log.info("R7/W7: Handle WRITE Request: " + writeRequest + " at " + self.getIp() + " " + self.getPort());
			String[] request = writeRequest.split(",");
			Integer r = Integer.valueOf(request[1]);
			Integer ts1 = Integer.valueOf(request[2]);
			Integer wr1 = Integer.valueOf(request[3]);
			Integer k1 = Integer.valueOf(request[4]);
			String v1 = request[5];
			
			Data keyFlag = keyData.get(k1);
			if((ts1 > keyFlag.getTs()) && (wr1 >= keyFlag.getWr())) {
				keyFlag.setTs(ts1);
				keyFlag.setWr(wr1);
				keyFlag.setVal(v1);
//				val = v1;
//				keyData.put(k1, v1);
			}
			
			MessagePayload message = new MessagePayload(WRITE_ACK + "," + r + "," + k1);
			log.info("R8/W8: Send Ack [" + writeRequest + "] from " + self.getIp() + " " + self.getPort());
			trigger(new P2PAckSend(src, message), pp2p);
			
		}

	};
	
	private Handler<P2PAckDeliver> handleP2PAckMessage = new Handler<P2PAckDeliver>() {
		@Override
		public void handle(P2PAckDeliver event) {
			String request = event.getMessage().getPayload();
			if(request.contains(WRITE_ACK)) {
				handleWriteAck(event.getFrom(), event.getMessage().getPayload());
			} else if(request.contains(READ_ACK)) {
				handleReadAck(event.getFrom(), event.getMessage().getPayload());
			}
		}
		
		/**
		 * upon event ⟨ pl, Deliver | q, [VALUE, r, ts′, wr′, v′] ⟩ such that r = rid do
		 * 		readlist[q] := (ts′, wr′, v′);
		 * 		if #(readlist) > N/2 then
		 * 			(maxts, rr, readval) := highest(readlist);
		 * 			readlist := [⊥]N ;
		 * 			if reading = TRUE then
		 * 				trigger ⟨ beb, Broadcast | [WRITE, rid, maxts, rr, readval] ⟩;
		 * 			else
		 * 				trigger ⟨ beb, Broadcast | [WRITE, rid, maxts + 1, rank(self), writeval] ⟩;
		 * 
		 */
		private void handleReadAck(TAddress src, String ack) {
			log.info("R5/W5: Hanlde Pp2p Deliver Ack message [" + ack + "] at "
					+ self.getIp() + ":" + self.getPort());
			String[] request = ack.split(",");
			Integer r = Integer.valueOf(request[1]);
			Integer ts1 = Integer.valueOf(request[2]);
			Integer wr1 = Integer.valueOf(request[3]);
			Integer k1 = Integer.valueOf(request[4]);
			String v1 = request[5];
			
			if(rid.containsKey(k1) && rid.get(k1).equals(r)) {
				List<ReadInfo> rl;
				if(readlist.containsKey(k1)) {
					rl = readlist.get(k1);
				} else {
					rl = new ArrayList<ReadInfo>();	
				}
				
				rl.add(new ReadInfo(ts1, wr1, k1, v1, src.hashCode()));
				
				if (rl.size() > (numNodes / 2)) {
					Collections.sort(rl, new Comparator<ReadInfo>() {
						@Override
						public int compare(ReadInfo o1, ReadInfo o2) {
							if (o1.getTs() < o2.getTs())
								return -1;
							else if (o1.getTs() > o2.getTs())
								return 1;
							else
								return (o1.getNodeid() < o2.getNodeid()) ? -1 : 1;
						}
					});
					
					ReadInfo highest = rl.get(rl.size() - 1);
					
					rr.put(k1, new Integer(highest.getWr()));
	                readval.put(k1, new Val(highest.getKey(), highest.getVal()));
	                maxts.put(k1, new Integer(highest.getTs()));
	                
	                rl.clear();
	                
					if (reading.containsKey(k1) && reading.get(k1)) {
						MessagePayload message = new MessagePayload(WRITE_OPT + "," + rid.get(k1) + "," + maxts.get(k1) + "," + rr.get(k1) + "," + k1 + "," + readval.get(k1).getVal());
						log.info("R6: Broadcast [" + message.getPayload() + "] from " + self.getIp() + ":" + self.getPort());
						trigger(new BEBroadcast(message), beb);
					} else {
						MessagePayload message = new MessagePayload(WRITE_OPT + "," + rid.get(k1) + "," + (maxts.get(k1) + 1) + "," + self.hashCode() + "," + k1 + "," + writeval.get(k1).getVal());
						log.info("W6: Broadcast [" + message.getPayload() + "] from " + self.getIp() + ":" + self.getPort());
						trigger(new BEBroadcast(message), beb);
					}
				}
				
			}
		}
		
		/**
		 * upon event ⟨ pl, Deliver | q, [ACK, r] ⟩ such that r = rid do
		 * 	acks := acks + 1;
		 * 	if acks > N/2 then
		 * 		acks := 0;
		 * 		if reading = TRUE then
		 * 			reading := FALSE;
		 * 			trigger ⟨ nnar, ReadReturn | readval ⟩;
		 * 		else
		 * 			trigger ⟨ nnar, WriteReturn ⟩;
		 * 
		 * @param src
		 * @param ack
		 */
		private void handleWriteAck(TAddress src, String ack) {
			Integer r = Integer.valueOf(ack.split(",")[1]);
			Integer key = Integer.valueOf(ack.split(",")[2]);
			
			if(rid.containsKey(key) && rid.get(key).equals(r)) {
				log.info("R9/W9: Pp2p Deliver Ack message [" + ack + "] at " + self.getIp() + ":" + self.getPort());
//				acks += 1;
				Integer ackx = acks.get(key);
				ackx += 1;
				acks.put(key, ackx);

				if(ackx > (numNodes / 2)) {
					acks.put(key, 0);
					
					if (reading.containsKey(key) && reading.get(key)) {
						reading.put(key, false);
						trigger(new ArReadResponse(0, readval.get(key).getVal()), nnar);
					} else {
						trigger(new ArWriteResponse(), nnar);
					}
				}
			}
			
		}

	};

	private Handler<ArWriteRequest> handleWriteRequest = new Handler<ArWriteRequest>() {

		/**
		 * upon event ⟨ nnar, Write | v ⟩ do
         *       rid := rid + 1;
         *       writeval := v;
         *       acks := 0;
         *       readlist := [⊥]N ;
         *       trigger ⟨ beb, Broadcast | [READ, rid] ⟩;
		 */
		@Override
		public void handle(ArWriteRequest event) {
			log.info("W1: Handle ArWriteRequest [" + event.getKey() + "," + event.getValue() + "] at" + self.getIp() + ":" + self.getPort());
			
//			rid++;
			if(rid.containsKey(event.getKey())) {
				rid.put(event.getKey(), (rid.get(event.getKey()) + 1));
			} else {
				rid.put(event.getKey(), 1);
			}

//			writekey = event.getKey();
//			writeval = event.getValue();
			writeval.put(event.getKey(), new Val(event.getKey(), event.getValue()));
//			acks = 0;
			acks.put(event.getKey(), 0);
			
			if(readlist.containsKey(event.getKey())) {
				readlist.get(event.getKey()).clear();
			} else {
				readlist.put(event.getKey(), new ArrayList<ReadInfo>());
			}
			
			MessagePayload message = new MessagePayload(READ_OPT + "," + rid.get(event.getKey()) + "," + event.getKey());
			log.info("W2: Broadcast [" + message.getPayload() + "] from " + self.getIp() + ":" + self.getPort());
			trigger(new BEBroadcast(message), beb);
		}

	};
	
	

	{
		subscribe(handleReadRequest, nnar);
		subscribe(bebDeliver, beb);
		subscribe(handleP2PAckMessage, pp2p);

		subscribe(handleWriteRequest, nnar);
	}
	
	public static class Init extends se.sics.kompics.Init<ReadImposeWriteConsultMajority> {
		public final TAddress self;
		public final Integer numNodes;
		public Init(TAddress self, Integer numNodes) {
			this.self = self;
			this.numNodes = numNodes;
		}
	}
}
