package eladsh.computer_architecture.peripherals;

import eladsh.computer_architecture.state.Tickable;

public class Clock implements Tickable {
	private int cycle = -1;
	
	public int get() {
		return cycle;
	}
	
	@Override
	public void preTick() {
		// do nothing
	}
	
	public void tick() {
		cycle++;
	}
	
	@Override
	public void postTick() {
		// do nothing
	}

}
