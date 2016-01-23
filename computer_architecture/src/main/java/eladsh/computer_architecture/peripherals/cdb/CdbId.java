package eladsh.computer_architecture.peripherals.cdb;

import eladsh.computer_architecture.data.instruction.Instruction;

/**
 * identifier of reservation station/memory buffers when {@link Instruction} result is put on {@link CDB}
 */
public class CdbId {
	public static enum Type {
		ADD, MUL, LD
	}

	private final Type type;
	private final int idx;
	
	public CdbId(Type type, int idx) {
		this.type = type;
		this.idx = idx;
	}
	
	public Type getType() {
		return type;
	}
	
	public int getIdx() {
		return idx;
	}
	
	@Override
	public String toString() {
		return this.type+" "+this.idx;
	}
}
