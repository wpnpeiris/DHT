package simulation;

import java.util.List;

import kth.id2203.Node;
import kth.id2203.config.Grid;
import kth.id2203.network.TAddress;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Init;
import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution;
import se.sics.kompics.simulator.events.system.StartNodeEvent;


public class NodeStructureScenario {

	static Operation1 startSlave = new Operation1<StartNodeEvent, Integer>() {

        @Override
        public StartNodeEvent generate(final Integer n) {
            return new StartNodeEvent() {

                List<TAddress> allNodes = Grid.getAllNodes();
                TAddress selfAdr = allNodes.get(n);

                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }

                @Override
                public Class<? extends ComponentDefinition> getComponentDefinition() {
                    return Node.class;
                }

                @Override
                public Init getComponentInit() {
                    return new Node.Init(selfAdr);
                }
            };
        }
    };

    static Operation startLeader = new Operation<StartNodeEvent>() {

		@Override
		public StartNodeEvent generate() {
			return new StartNodeEvent() {

                List<TAddress> allNodes = Grid.getAllNodes();
                TAddress selfAdr = allNodes.get(0);

				@Override
				public Class getComponentDefinition() {
					return Node.class;
				}

				@Override
				public Init getComponentInit() {
                    return new Node.Init(selfAdr);
				}

				@Override
				public Address getNodeAddress() {
					return selfAdr;
				}

			};
		}

	};
	
	public static SimulationScenario simpleNodeStructure() {
		SimulationScenario scenario = new SimulationScenario() {
			{
				final SimulationScenario.StochasticProcess slaves = new SimulationScenario.StochasticProcess() {
					{
						eventInterArrivalTime(constant(1000));
                        raise(5, startSlave, new BasicIntSequentialDistribution(1));
					}
				};
				
                SimulationScenario.StochasticProcess leader = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startLeader);
                    }
                };

                slaves.start();
                leader.startAfterTerminationOf(1000, slaves);
				terminateAfterTerminationOf(10000, leader);
			}

		};

		return scenario;
	}
}