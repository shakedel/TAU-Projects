package eladsh.computer_architecture.state;

public interface Tickable {
	public void preTick();
	public void tick();
	public void postTick();
}
