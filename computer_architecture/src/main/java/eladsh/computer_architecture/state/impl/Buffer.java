package eladsh.computer_architecture.state.impl;

import java.util.Observable;
import java.util.Observer;

import eladsh.computer_architecture.peripherals.cdb.CDB;
import eladsh.computer_architecture.peripherals.cdb.CdbId;
import eladsh.computer_architecture.peripherals.cdb.CdbTrans;
import eladsh.computer_architecture.state.AcceptsInstructions;
import eladsh.computer_architecture.data.instruction.Instruction;

public abstract class Buffer implements AcceptsInstructions, Observer {

	protected final int delay;
	protected final CdbId cdbId;
	protected final CDB cdb;
	protected final int idx;
	
	protected BufferState state = BufferState.IDLE;
	protected Integer time = null;
	protected Instruction inst = null;
	private CdbTrans pendingCdbTrans = null;
	
	public Buffer(int idx, CdbId.Type cdbType, int delay, CDB cdb) {
		this.idx = idx;
		this.cdb = cdb;
		this.cdb.addObserver(this);
		this.delay = delay;
		this.cdbId = new CdbId(cdbType, idx);
	}
	
	protected void reset() {
		this.state = BufferState.IDLE;
		this.inst = null;
		this.time = null;
	}

	@Override
	public boolean acceptInstruction(Instruction instruction) {
		if (this.state == BufferState.IDLE) {
			this.inst = instruction;
			this.set(inst);
			return true;
		}
		return false;
	}
	
	public void update(Observable obs, Object data) {
		if (this.state != BufferState.WAITING) {
			return;
		}
		incomingCdbTrans((CdbTrans) data);
	}
	
	abstract protected void set(Instruction inst);
	abstract protected void incomingCdbTrans(CdbTrans cdbTrans);
	
	public boolean prepCdbTrans() {
		CdbTrans trans = generateCdbTrans();
		if (trans == null) {
			return false;
		}
		if (trans != CdbTrans.NO_TRANS) {
			this.pendingCdbTrans = trans;
		}
		this.reset();
		return true;
	}
	
	abstract protected CdbTrans generateCdbTrans();
	
	public void sendPendingCdbTrans() {
		if (this.pendingCdbTrans != null) {
			this.cdb.notifyObservers(this.pendingCdbTrans);
			this.pendingCdbTrans = null;
		}
	}
	
	@Override
	public boolean isEmpty() {
		return (this.state == BufferState.IDLE);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cdbId == null) ? 0 : cdbId.hashCode());
		result = prime * result + idx;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Buffer))
			return false;
		Buffer other = (Buffer) obj;
		if (cdbId == null) {
			if (other.cdbId != null)
				return false;
		} else if (!cdbId.equals(other.cdbId))
			return false;
		if (idx != other.idx)
			return false;
		return true;
	}
	
	
}
	