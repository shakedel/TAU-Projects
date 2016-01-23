package eladsh.computer_architecture.modules.impl.buffers;

import eladsh.computer_architecture.data.instruction.Instruction;
import eladsh.computer_architecture.data.register.Register;
import eladsh.computer_architecture.modules.impl.MemoryUnit;
import eladsh.computer_architecture.peripherals.cdb.CDB;
import eladsh.computer_architecture.peripherals.cdb.CdbId;
import eladsh.computer_architecture.peripherals.cdb.CdbTrans;

/**
 * Buffer for store instructions 
 */
public class StoreBuffer extends MemoryBuffer {

	private final MemoryUnit memoryUnit;
	Float Vj = null;
	CdbId Qj = null;

	public StoreBuffer(MemoryUnit memoryUnit, int idx, CdbId.Type cdbType, int delay, CDB cdb) {
		super(memoryUnit, MemoryBufferType.ST, idx, cdbType, delay, cdb);
		this.memoryUnit = memoryUnit;
	}

	@Override
	protected void reset() {
		super.reset();
		this.Vj = null;
		this.Qj = null;
	}

	@Override
	public void set(Instruction inst) {
		Register regJ = this.memoryUnit.regs[inst.getThreadIdx()].getReg(inst.getSrc1());
		switch (regJ.getState()) {
		case VAL:
			this.Vj = regJ.getVal();
			this.Qj = null;
			break;
		case CDB_ID:
			this.Vj = null;
			this.Qj = regJ.getCdbId();
			break;
		default:
			throw new IllegalArgumentException("unknown register state: "+regJ.getState());
		}

		this.state = (Qj!=null) ? BufferState.WAITING : BufferState.READY;
	}

	@Override
	protected void incomingCdbTrans(CdbTrans cdbTrans) {
		if (this.state != BufferState.WAITING) {
			return;
		}
		if (cdbTrans.getCdbId().equals(this.Qj)) {
			this.Qj = null;
			this.Vj = cdbTrans.getValue();
			this.state = BufferState.READY;
		}
	}

	@Override 
	protected CdbTrans generateCdbTrans() {
		if (this.state == BufferState.EXECUTING && this.time==1) {
			this.memoryUnit.incrementNumFunctionalUnits();
			this.memoryUnit.memory.writeFloat(inst.getImm(), this.Vj);
			return CdbTrans.NO_TRANS;
		}
		return null;
	}

	@Override
	public void preTick() {
		// do nothing
	}
	
	@Override 
	public void tick() {
		switch (this.state) {
		case IDLE:
			break;
		case WAITING:
			if (this.Vj!=null) {
				this.state = BufferState.READY;
			}
			break;
		case READY:
			if (this.memoryUnit.decrementNumFunctionalUnits()) {
				this.time = this.delay;
				this.memoryUnit.instructionStatus[this.inst.getThreadIdx()].setExecComp(this.inst);
				this.state = BufferState.EXECUTING;
			}
			break;
		case EXECUTING:
			this.time--;
			break;
		default: 
			throw new IllegalArgumentException("unknown state: "+this.state);
		}
	}
	
	@Override
	public void postTick() {
		// do nothing
	}

	@Override
	public String toString() {
		return "StoreBuffer[idx=" + idx + ", state=" + state + ", inst=" + inst + ", Vj=" + Vj
				+ ", Qj=" + Qj + ", addr=" + this.inst.getImm() +", time="+ time + "]";
	}

}