package state.impl;

import java.util.Observable;
import java.util.Observer;

import state.AcceptsInstructions;

import cdb.CDB;
import cdb.CdbId;
import cdb.CdbTrans;

import data.instruction.Instruction;

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
		if (this.state != BufferState.IDLE) {
			return false;
		}
		this.inst = instruction;
		this.set(inst);
		return true;
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
}
	