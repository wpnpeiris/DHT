package kth.id2203;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kth.id2203.epfd.event.Restore;
import kth.id2203.epfd.event.Suspect;
import kth.id2203.epfd.port.EventuallyPerfectFailureDetector;
import kth.id2203.event.Get;
import kth.id2203.event.Put;
import kth.id2203.network.TAddress;
import kth.id2203.network.TMessage;
import kth.id2203.register.event.ArReadRequest;
import kth.id2203.register.event.ArReadResponse;
import kth.id2203.register.event.ArWriteRequest;
import kth.id2203.register.event.ArWriteResponse;
import kth.id2203.register.port.AtomicRegister;
import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;

public class Node extends ComponentDefinition {
	
	private static final Logger log = LoggerFactory.getLogger(Node.class);
	
	private Positive<Network> network = requires(Network.class);
	
	private Positive<AtomicRegister> nnar = requires(AtomicRegister.class);
		
	private Positive<EventuallyPerfectFailureDetector> epfd = requires(EventuallyPerfectFailureDetector.class);
	
	private TAddress self;
	
	public Node(Init init) {
		log.info("Initialize node with self address: " + init.self.getIp() + ":" + init.self.getPort());
        this.self = init.self;
    }
	
	ClassMatchedHandler<Put, TMessage> putHandler = new ClassMatchedHandler<Put, TMessage>() {

        @Override
        public void handle(Put event, TMessage context) {
        	log.info("Receive Put request [key: " + event.getKey() + ", value:" + event.getValue() + "]");
        	trigger(new ArWriteRequest(event.getKey(), event.getValue()), nnar);
        }
    };

    ClassMatchedHandler<Get, TMessage> getHandler = new ClassMatchedHandler<Get, TMessage>() {

        @Override
        public void handle(Get event, TMessage context) {
        	log.info("Receive Get request [key: " + event.getKey() + "]");
        	trigger(new ArReadRequest(event.getKey()), nnar);
        }
    };
	
    Handler<ArReadResponse> readResponseHanlder = new Handler<ArReadResponse>() {
    	public void handle(ArReadResponse event) {
    		log.info("Got Read Value " + event.getValue());
    	}
    };
    
    Handler<ArWriteResponse> writeResponseHanlder = new Handler<ArWriteResponse>() {
    	public void handle(ArWriteResponse event) {
    		log.info("Got Write Response ");
    	}
    };
    
    Handler<Suspect> suspectHandler = new Handler<Suspect>() {
        @Override
        public void handle(Suspect suspect) {
            log.info("Process suspected :" + suspect.getSource());
        }
    };

    Handler<Restore> restoreHandler = new Handler<Restore>() {
        @Override
        public void handle(Restore restore) {
            log.info("Process restored :" + restore.getSource());
        }
    };
    
    {
		subscribe(readResponseHanlder, nnar);
		subscribe(writeResponseHanlder, nnar);
		subscribe(putHandler, network);
		subscribe(getHandler, network);
		subscribe(suspectHandler, epfd);
        subscribe(restoreHandler, epfd);
	}
    
	public static class Init extends se.sics.kompics.Init<Node> {
        public final TAddress self;
        public Init(TAddress self) {
            this.self = self;
        }
    }
}
