package state;

import data.instruction.Instruction;

public class CdbTrans {
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