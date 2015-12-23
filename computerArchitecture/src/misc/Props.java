package misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Props {
	
	private static Props inst = null;
	
	public static void set(int numOfAdds, int numOfMuls, int addDelay, int multDelay,
			int memDelay, int numAddReservations, int numMulReservations,
			int numMemLoadBuffers, int numMemStoreBuffers) {
		inst = new Props(numOfAdds, numOfMuls, addDelay, multDelay, memDelay, numAddReservations, numMulReservations, numMemLoadBuffers, numMemStoreBuffers);
	}
	
	public static void set(File f) {
		try {
			inst = new Props(f);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public static Props get() {
		if (inst == null) {
			throw new IllegalStateException("Properties have not been set");
		}
		return inst;
	}
	
	private Props(int numOfAdds, int numOfMuls, int addDelay, int multDelay,
			int memDelay, int numAddReservations, int numMulReservations,
			int numMemLoadBuffers, int numMemStoreBuffers) {
		super();
		this.numOfAdds = numOfAdds;
		this.numOfMuls = numOfMuls;
		this.addDelay = addDelay;
		this.multDelay = multDelay;
		this.memDelay = memDelay;
		this.numAddReservations = numAddReservations;
		this.numMulReservations = numMulReservations;
		this.numMemLoadBuffers = numMemLoadBuffers;
		this.numMemStoreBuffers = numMemStoreBuffers;
	}

	private Props(File f) throws IOException {
		Properties props = new Properties();
		props.load(new FileInputStream(f));
		
		this.numOfAdds = Integer.parseInt(props.getProperty("add_nr"));
		this.numOfMuls = Integer.parseInt(props.getProperty("mul_nr"));
		this.addDelay = Integer.parseInt(props.getProperty("add_delay"));
		this.multDelay = Integer.parseInt(props.getProperty("mul_delay"));
		this.memDelay = Integer.parseInt(props.getProperty("mem_delay"));
		this.numAddReservations = Integer.parseInt(props.getProperty("add_nr_reservation"));
		this.numMulReservations = Integer.parseInt(props.getProperty("mul_nr_reservation"));
		this.numMemLoadBuffers = Integer.parseInt(props.getProperty("mem_nr_load_buffers"));
		this.numMemStoreBuffers = Integer.parseInt(props.getProperty("mem_nr_store_buffers"));
	}
	
	private int numOfAdds;
	private int numOfMuls;
	private int addDelay;
	private int multDelay;
	private int memDelay;
	private int numAddReservations;
	private int numMulReservations;
	private int numMemLoadBuffers;
	private int numMemStoreBuffers;
	
	public int getNumOfAdds() {
		return numOfAdds;
	}
	public int getNumOfMults() {
		return numOfMuls;
	}
	public int getAddDelay() {
		return addDelay;
	}
	public int getMulDelay() {
		return multDelay;
	}
	public int getMemDelay() {
		return memDelay;
	}
	public int getNumAddReservations() {
		return numAddReservations;
	}
	public int getNumMulReservations() {
		return numMulReservations;
	}
	public int getNumMemLoadBuffers() {
		return numMemLoadBuffers;
	}
	public int getNumMemStoreBuffers() {
		return numMemStoreBuffers;
	}
	
	
}
