package state;

public interface Tickable {
	public void preTick();
	public void tick();
	public void postTick();
}
