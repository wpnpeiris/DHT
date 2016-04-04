package simulation;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;

public class SimulatorComponent extends ComponentDefinition {
	protected Positive<Network> network = requires(Network.class);
	protected Positive<Timer> timer = requires(Timer.class);
}
