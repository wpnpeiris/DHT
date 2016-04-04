package simulation.register;

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

public class ConcurrentReadWriteScenario {

	private static final int NUM_NODES = 10;
	
	public static final int EXECUTION_PROCESS_COUNT = 10;
	
	public static final int DATA_KEY = 100;
	public static final String DATA_VALUE1 = "DATA_VALUE_OF_100";

	public static final String DATA_VALUE2 = "DATA_VALUE_OF_200";
	
	static Operation setupOp = new Operation<SetupEvent>() {
		@Override
		public SetupEvent generate() {
			return new SetupEvent() {
				@Override
				public void setupGlobalView(GlobalView gv) {
					gv.setValue("simulation.register.type", "LINEARIAZABLE");
					gv.setValue("simulation.register.execution_steps", new ArrayList<String>());
					gv.setValue("simulation.register.write_response_received", false);
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
					config.put("simulation.register.checktimeout", 1000);
					return config;
				}

				@Override
				public Address getNodeAddress() {
					return selfAdr;
				}

				@Override
				public Class getComponentDefinition() {
					return ConcurrentReadWriteObserver.class;
				}

				@Override
				public Init getComponentInit() {
					return Init.NONE;
				}
			};
		}
	};
	
	static Operation1 startRegister = new Operation1<StartNodeEvent, Integer>() {

		@Override
		public StartNodeEvent generate(final Integer self) {
			return new StartNodeEvent() {
				TAddress selfAdr;
				List<TAddress> all;
				
				{
					try {
						selfAdr = new TAddress(InetAddress.getByName("10.19.0." + self), 10000);
						all = new ArrayList<>();
						for (int i = 1; i <= NUM_NODES; i++) {
			                all.add(new TAddress(InetAddress.getByName("10.19.0." + i), 10000));
			            }
					} catch (UnknownHostException ex) {
						throw new RuntimeException(ex);
					}
				
				}
				
				@Override
				public Class getComponentDefinition() {
					return RegistryHost.class;
				}

				@Override
				public Init getComponentInit() {
					return new RegistryHost.Init(selfAdr, all);
				}

				@Override
				public Address getNodeAddress() {
					return selfAdr;
				}
				
			};
		}
		
	};
	
	static Operation1 startWriteClient = new Operation1<StartNodeEvent, Integer>() {

		@Override
		public StartNodeEvent generate(final Integer writerId) {
			return new StartNodeEvent() {
				TAddress selfAdr;
				TAddress dest;
				{
					try {
						selfAdr = new TAddress(InetAddress.getByName("10.19.0.99"), 10000);
						dest = new TAddress(InetAddress.getByName("10.19.0.1"), 10000);
					} catch (UnknownHostException ex) {
						throw new RuntimeException(ex);
					}
					
				}
				@Override
				public Class getComponentDefinition() {
					return WriteClient.class;
				}

				@Override
				public Init getComponentInit() {
					return new WriteClient.Init(selfAdr, dest, DATA_KEY,  writerId == 1 ? DATA_VALUE1 : DATA_VALUE2);
				}

				@Override
				public Address getNodeAddress() {
					return selfAdr;
				}
				
			};
		}
		
	};
	
	static Operation1 startReadClient = new Operation1<StartNodeEvent, Integer>() {

		@Override
		public StartNodeEvent generate(final Integer readerId) {
			return new StartNodeEvent() {
				TAddress selfAdr;
				TAddress dest;
				{
					try {
						selfAdr = new TAddress(InetAddress.getByName("10.19.0.1" + readerId), 10000);
						dest = new TAddress(InetAddress.getByName("10.19.0." + readerId), 10000);
					} catch (UnknownHostException ex) {
						throw new RuntimeException(ex);
					}
					
				}
				@Override
				public Class getComponentDefinition() {
					return ReadClient.class;
				}

				@Override
				public Init getComponentInit() {
					return new ReadClient.Init(selfAdr, dest, DATA_KEY);
				}

				@Override
				public Address getNodeAddress() {
					return selfAdr;
				}
				
			};
		}
		
	};
	
	public static SimulationScenario register() {
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

				StochasticProcess startRegisters = new StochasticProcess() {
					{
						eventInterArrivalTime(constant(1000));
						raise(NUM_NODES, startRegister, new BasicIntSequentialDistribution(1));
					}
				};
				
				StochasticProcess startWriteClients1 = new StochasticProcess() {
					{
						eventInterArrivalTime(constant(1000));
						raise(1, startWriteClient, new BasicIntSequentialDistribution(1));
					}
				};
				
				StochasticProcess startReadClients2 = new StochasticProcess() {
					{
						eventInterArrivalTime(constant(1000));
						raise(1, startReadClient, new BasicIntSequentialDistribution(2));
					}
				};
				
				StochasticProcess startReadClients3 = new StochasticProcess() {
					{
						eventInterArrivalTime(constant(1000));
						raise(1, startReadClient, new BasicIntSequentialDistribution(3));
					}
				};
				
				StochasticProcess startWriteClients2 = new StochasticProcess() {
					{
						eventInterArrivalTime(constant(1000));
						raise(1, startWriteClient, new BasicIntSequentialDistribution(2));
					}
				};
				
				
				StochasticProcess startReadClients4 = new StochasticProcess() {
					{
						eventInterArrivalTime(constant(1000));
						raise(1, startReadClient, new BasicIntSequentialDistribution(4));
					}
				};
				
				setup.start();
				observer.startAfterTerminationOf(0, setup);
				startRegisters.startAfterTerminationOf(1000, observer);
				startWriteClients1.startAfterTerminationOf(1000, startRegisters);
				startReadClients2.startAfterTerminationOf(1000, startWriteClients1);
				startWriteClients2.startAfterTerminationOf(1000, startReadClients2);
				
				startReadClients3.startAfterTerminationOf(1, startWriteClients2);
				startReadClients4.startAfterTerminationOf(1, startWriteClients2);
				terminateAfterTerminationOf(100000, startReadClients4);
			}
		};
		return scenario;
	}
	
	public static void main(String[] args) {
		long seed = 123;
		SimulationScenario.setSeed(seed);
		SimulationScenario simpleBootScenario = register();
		simpleBootScenario.simulate(LauncherComp.class);
	}
}
