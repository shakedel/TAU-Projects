package eladsh.computer_architecture.peripherals.cdb;

import eladsh.computer_architecture.data.instruction.Instruction;

public class CdbTrans {
	
	public final static CdbTrans NO_TRANS = new CdbTrans(null, -1, null);
	
	private final CdbId cdbId;
	private final float value;
	private final Instruction inst;
	
	public CdbTrans(CdbId cdbId, float value, Instruction inst) {
		this.cdbId = cdbId;
		this.value = value;
		this.inst = inst;
	}

	public CdbId getCdbId() {
		return this.cdbId;
	}

	public float getValue() {
		return this.value;
	}
	
	public Instruction getInstruction() {
		return this.inst;
	}
	
}