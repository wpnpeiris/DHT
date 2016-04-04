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

public class SimpleReadWriteScenario {

	private static final int NUM_NODES = 10;
	private static final int WRITER_ID = 1;
	private static final int READER_ID = 10;
	
	public static final int DATA_KEY = 100;
	public static final String DATA_VALUE = "DATA_VALUE_OF_100";
	
	static Operation setupOp = new Operation<SetupEvent>() {
		@Override
		public SetupEvent generate() {
			return new SetupEvent() {
				@Override
				public void setupGlobalView(GlobalView gv) {
					gv.setValue("simulation.register.type", "SIMPLE");
					gv.setValue("simulation.register.read_response_value", "NA");
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
					return SimpleReadWriteObserver.class;
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
	
	static Operation startWriteClient = new Operation<StartNodeEvent>() {

		@Override
		public StartNodeEvent generate() {
			return new StartNodeEvent() {
				TAddress selfAdr;
				TAddress dest;
				{
					try {
						selfAdr = new TAddress(InetAddress.getByName("10.19.0.0"), 10000);
						dest = new TAddress(InetAddress.getByName("10.19.0." + WRITER_ID), 10000);
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
					return new WriteClient.Init(selfAdr, dest, DATA_KEY, DATA_VALUE);
				}

				@Override
				public Address getNodeAddress() {
					return selfAdr;
				}
				
			};
		}
		
	};
	
	
	static Operation startReadClient = new Operation<StartNodeEvent>() {

		@Override
		public StartNodeEvent generate() {
			return new StartNodeEvent() {
				TAddress selfAdr;
				TAddress dest;
				{
					try {
						selfAdr = new TAddress(InetAddress.getByName("10.19.0.99"), 10000);
						dest = new TAddress(InetAddress.getByName("10.19.0." + READER_ID), 10000);
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
				
				StochasticProcess startWriteClients = new StochasticProcess() {
					{
						eventInterArrivalTime(constant(1000));
						raise(1, startWriteClient);
					}
				};
				
				StochasticProcess startReadClients = new StochasticProcess() {
					{
						eventInterArrivalTime(constant(1000));
						raise(1, startReadClient);
					}
				};

				setup.start();
				observer.startAfterTerminationOf(0, setup);
				startRegisters.startAfterTerminationOf(1000, observer);
				startWriteClients.startAfterTerminationOf(1000, startRegisters);
				startReadClients.startAfterTerminationOf(1000, startWriteClients);
				terminateAfterTerminationOf(100000, startReadClients);
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
