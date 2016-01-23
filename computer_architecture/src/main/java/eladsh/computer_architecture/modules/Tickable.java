package eladsh.computer_architecture.modules;

/**
 * An interface to simulate hardware modules with state (flops)
 */
public interface Tickable {
	
	/**
	 * perform actions before the tick
	 */
	public void preTick();
	
	/**
	 * perform tick actions 
	 */
	public void tick();
	
	/**
	 * perform actions after the tick
	 */
	public void postTick();
}
