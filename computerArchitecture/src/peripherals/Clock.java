package peripherals;

public class Clock {
	private int cycle = -1;
	
	public int get() {
		return cycle;
	}
	
	public void tick() {
		cycle++;
	}

}
