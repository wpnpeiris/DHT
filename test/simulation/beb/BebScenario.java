package simulation.beb;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kth.id2203.network.TAddress;
import se.sics.kompics.Init;
import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution;
import se.sics.kompics.simulator.events.system.SetupEvent;
import se.sics.kompics.simulator.events.system.StartNodeEvent;
import se.sics.kompics.simulator.run.LauncherComp;
import se.sics.kompics.simulator.util.GlobalView;

public class BebScenario {

	private static final int NUM_NODES = 100;

	static Operation setupOp = new Operation<SetupEvent>() {
		@Override
		public SetupEvent generate() {
			return new SetupEvent() {
				@Override
				public void setupGlobalView(GlobalView gv) {
					gv.setValue("simulation.beb.numreceived", 0);
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
					config.put("simulation.beb.checktimeout", 2000);
					return config;
				}

				@Override
				public Address getNodeAddress() {
					return selfAdr;
				}

				@Override
				public Class getComponentDefinition() {
					return BebSimulationObserver.class;
				}

				@Override
				public Init getComponentInit() {
					return new BebSimulationObserver.Init(NUM_NODES);
				}
			};
		}
	};

	static Operation1 startBebReceiver = new Operation1<StartNodeEvent, Integer>() {

		@Override
		public StartNodeEvent generate(final Integer self) {
			return new StartNodeEvent() {
				TAddress selfAdr;
				List<TAddress> all;
				{
					try {
						selfAdr = new TAddress(InetAddress.getByName("192.193.0." + self), 10000);
						all = new ArrayList<>();
			            
					} catch (UnknownHostException ex) {
						throw new RuntimeException(ex);
					}
				}

				@Override
				public Class getComponentDefinition() {
					return BebPointHost.class;
				}

				@Override
				public Init getComponentInit() {
					return new BebPointHost.Init(selfAdr, all, false);
				}

				@Override
				public Address getNodeAddress() {
					return selfAdr;
				}

			};
		}

	};
	
	static Operation startBeBroadcaster = new Operation<StartNodeEvent>() {

		@Override
		public StartNodeEvent generate() {
			return new StartNodeEvent() {
				TAddress selfAdr;
				List<TAddress> all;
				{
					try {
						selfAdr = new TAddress(InetAddress.getByName("192.193.0.0"), 10000);
						all = new ArrayList<>();
			            for (int i = 1; i <= NUM_NODES; i++) {
			                all.add(new TAddress(InetAddress.getByName("192.193.0." + i), 10000));
			            }
			            all.add(selfAdr);
					} catch (UnknownHostException ex) {
						throw new RuntimeException(ex);
					}
				}

				@Override
				public Class getComponentDefinition() {
					return BebPointHost.class;
				}

				@Override
				public Init getComponentInit() {
					return new BebPointHost.Init(selfAdr, all, true);
				}

				@Override
				public Address getNodeAddress() {
					return selfAdr;
				}

			};
		}

	};
	
	public static SimulationScenario broadcast() {
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
						raise(NUM_NODES, startBebReceiver, new BasicIntSequentialDistribution(1));
					}
				};
				
				StochasticProcess broadcaster = new StochasticProcess() {
					{
						eventInterArrivalTime(constant(1000));
						raise(1, startBeBroadcaster);
					}
				};
				
                setup.start();
				observer.startAfterTerminationOf(0, setup);
				receiver.startAfterTerminationOf(1000, observer);
				broadcaster.startAfterTerminationOf(1000, receiver);
				terminateAfterTerminationOf(1000*10000, broadcaster);
			}
		};
		
		return scenario;
	}
	
	public static void main(String[] args) {
		long seed = 123;
		SimulationScenario.setSeed(seed);
		SimulationScenario simpleBootScenario = broadcast();
		simpleBootScenario.simulate(LauncherComp.class);
	}
}
