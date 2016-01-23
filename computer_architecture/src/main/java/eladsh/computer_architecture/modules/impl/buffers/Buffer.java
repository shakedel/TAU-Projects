package eladsh.computer_architecture.modules.impl.buffers;

import java.util.Observable;
import java.util.Observer;

import eladsh.computer_architecture.data.instruction.Instruction;
import eladsh.computer_architecture.data.register.Register;
import eladsh.computer_architecture.modules.AcceptsInstructions;
import eladsh.computer_architecture.peripherals.cdb.CDB;
import eladsh.computer_architecture.peripherals.cdb.CdbId;
import eladsh.computer_architecture.peripherals.cdb.CdbTrans;

/**
 * A base class for all buffers to inherit from
 */
public abstract class Buffer implements AcceptsInstructions, Observer {

	protected final int delay;
	protected final CdbId cdbId;
	protected final CDB cdb;
	protected final int idx;
	
	protected BufferState state = BufferState.IDLE;
	protected Integer time = null;
	protected Instruction inst = null;
	private CdbTrans pendingCdbTrans = null;
	
	/**
	 * @param idx index
	 * @param cdbType cdb type
	 * @param delay number of cycles a functional unit operates on this {@link Buffer}
	 * @param cdb {@link CDB} to transmit results to
	 */
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
	
	public BufferState getState() {
		return this.state;
	}
	
	/**
	 * if waiting for data on CDB, update value
	 */
	public void update(Observable obs, Object data) {
		if (this.state != BufferState.WAITING) {
			return;
		}
		incomingCdbTrans((CdbTrans) data);
	}
	
	/**
	 * @param inst {@link Instruction} to occupy this {@link Register}
	 */
	abstract protected void set(Instruction inst);
	
	/**
	 * @param cdbTrans {@link CdbTrans} to handle
	 */
	abstract protected void incomingCdbTrans(CdbTrans cdbTrans);
	
	/**
	 * @return <code>false</code> if the is no ready {@link CdbTrans}. Otherwise prepare it and return <code>true</code>.
	 */
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
	
	/**
	 * @return a {@link CdbTrans} which is the result of the computation on the occupying {@link Instruction}
	 */
	abstract protected CdbTrans generateCdbTrans();
	
	/**
	 * send the pending {@link CdbTrans} if one is prepared
	 */
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
	