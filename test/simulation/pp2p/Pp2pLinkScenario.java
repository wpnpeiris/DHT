package simulation.pp2p;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import kth.id2203.network.TAddress;
import se.sics.kompics.Init;
import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation;
import se.sics.kompics.simulator.events.system.SetupEvent;
import se.sics.kompics.simulator.events.system.StartNodeEvent;
import se.sics.kompics.simulator.run.LauncherComp;
import se.sics.kompics.simulator.util.GlobalView;

public class Pp2pLinkScenario {
	
	private static final int NUM_TEST_MESSAGES = 1000;
	
	static Operation setupOp = new Operation<SetupEvent>() {
        @Override
        public SetupEvent generate() {
            return new SetupEvent() {
                @Override
                public void setupGlobalView(GlobalView gv) {
                    gv.setValue("simulation.pp2p.numreceived", 0);
                }
            };
        }
    };
    
    static Operation startObserverOp = new Operation<StartNodeEvent>() {
        @Override
        public StartNodeEvent generate() {
            return new StartNodeEvent() {
                TAddress selfAdr;

                {
                    try {
                        selfAdr = new TAddress(InetAddress.getByName("0.0.0.0"), 0);
                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public Map<String, Object> initConfigUpdate() {
                    HashMap<String, Object> config = new HashMap<>();
                    config.put("simulation.pp2p.checktimeout", 2000);
                    return config;
                }
                
                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }

                @Override
                public Class getComponentDefinition() {
                    return Pp2pSimulationObserver.class;
                }

                @Override
                public Init getComponentInit() {
                    return new Pp2pSimulationObserver.Init(NUM_TEST_MESSAGES);
                }
            };
        }
    };

    
	static Operation startLinkReceiverPoint = new Operation<StartNodeEvent>() {

		@Override
		public StartNodeEvent generate() {
			return new StartNodeEvent() {
				TAddress selfAdr;

				{
					try {
						selfAdr = new TAddress(InetAddress.getByName("192.193.0.2"), 10000);
					} catch (UnknownHostException ex) {
						throw new RuntimeException(ex);
					}
				}

				@Override
				public Class getComponentDefinition() {
					return LinkPointHost.class;
				}

				@Override
				public Init getComponentInit() {
					return new LinkPointHost.Init(selfAdr, false);
				}

				@Override
				public Address getNodeAddress() {
					return selfAdr;
				}

			};
		}

	};
	
	static Operation startLinkSenderPoint = new Operation<StartNodeEvent>() {

		@Override
		public StartNodeEvent generate() {
			return new StartNodeEvent() {
				TAddress selfAdr;

				{
					try {
						selfAdr = new TAddress(InetAddress.getByName("192.193.0.1"), 10000);
					} catch (UnknownHostException ex) {
						throw new RuntimeException(ex);
					}
				}

				@Override
				public Class getComponentDefinition() {
					return LinkPointHost.class;
				}

				@Override
				public Init getComponentInit() {
					return new LinkPointHost.Init(selfAdr, NUM_TEST_MESSAGES, true);
				}

				@Override
				public Address getNodeAddress() {
					return selfAdr;
				}

			};
		}

	};
	
	public static SimulationScenario linkPoint1() {
		SimulationScenario scenario = new SimulationScenario() {
			{
				SimulationScenario.StochasticProcess setup = new SimulationScenario.StochasticProcess() {
                    {
                        raise(1, setupOp);
                    }
                };
                
                SimulationScenario.StochasticProcess observer = new SimulationScenario.StochasticProcess() {
                    {
                        raise(1, startObserverOp);
                    }
                };
                
				StochasticProcess receiver = new StochasticProcess() {
					{
						eventInterArrivalTime(constant(1000));
						raise(1, startLinkReceiverPoint);
					}
				};
				
				StochasticProcess sender = new StochasticProcess() {
					{
						eventInterArrivalTime(constant(1000));
						raise(1, startLinkSenderPoint);
					}
				};

				setup.start();
				observer.startAfterTerminationOf(0, setup);
				receiver.startAfterTerminationOf(1000, observer);
				sender.startAfterTerminationOf(1000, receiver);
				terminateAfterTerminationOf(1000*10000, sender);
			}
		};

		return scenario;
	}

	public static void main(String[] args) {
		long seed = 123;
		SimulationScenario.setSeed(seed);
		SimulationScenario simpleBootScenario = linkPoint1();
		simpleBootScenario.simulate(LauncherComp.class);
	}
}
