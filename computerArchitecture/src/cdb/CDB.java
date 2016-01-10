package cdb;

import java.util.Observable;

import peripherals.InstructionStatus;



public class CDB extends Observable {
	
	private final InstructionStatus[] instructionStatus;
	
	public CDB(InstructionStatus[] instructionStatus) {
		this.instructionStatus = instructionStatus;
	}
	
	@Override
	public void notifyObservers(Object cdbTrans) {
		CdbTrans trans = (CdbTrans) cdbTrans;
		this.instructionStatus[trans.getInstruction().getThreadIdx()].setWriteResult(trans.getInstruction());
		this.setChanged();
		super.notifyObservers(cdbTrans);
	}
}
