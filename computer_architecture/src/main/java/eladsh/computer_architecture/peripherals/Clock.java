package eladsh.computer_architecture.peripherals;

import eladsh.computer_architecture.modules.Tickable;

/**
 * Simulates the hardware clock
 */
public class Clock implements Tickable {
	private int cycle = -1;
	
	/**
	 * @return current clock cycle
	 */
	public int get() {
		return cycle;
	}
	
	@Override
	public void preTick() {
		// do nothing
	}
	
	@Override
	public void tick() {
		cycle++;
	}
	
	@Override
	public void postTick() {
		// do nothing
	}

}
