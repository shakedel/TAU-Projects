package state;

public class Clock {
	private static int cycle = 0;
	
	public static int get() {
		return cycle;
	}
	
	public static void tick() {
		cycle++;
	}

}
