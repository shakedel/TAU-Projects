package eladsh.computer_architecture.modules.impl.buffers;

import eladsh.computer_architecture.data.instruction.Instruction;
import eladsh.computer_architecture.modules.impl.MemoryUnit;
import eladsh.computer_architecture.peripherals.cdb.CDB;
import eladsh.computer_architecture.peripherals.cdb.CdbId;
import eladsh.computer_architecture.peripherals.cdb.CdbTrans;

/**
 * Buffer for load instructions 
 */
public class LoadBuffer extends MemoryBuffer {

	private final MemoryUnit memoryUnit;

	public LoadBuffer(MemoryUnit memoryUnit, int idx, CdbId.Type cdbType, int delay, CDB cdb) {
		super(memoryUnit, MemoryBufferType.LD, idx, cdbType, delay, cdb);
		this.memoryUnit = memoryUnit;
	}

	@Override
	protected void reset() {
		super.reset();
	}

	@Override
	public void set(Instruction inst) {
		this.memoryUnit.regs[inst.getThreadIdx()].getReg(inst.getDst()).set(this.cdbId);
		this.state = BufferState.READY;
	}

	@Override
	protected void incomingCdbTrans(CdbTrans cdbTrans) {
		return;
	}

	@Override 
	protected CdbTrans generateCdbTrans() {
		if (this.state == BufferState.EXECUTING && this.time==1) {
			this.memoryUnit.incrementNumFunctionalUnits();

			float val = this.memoryUnit.memory.readFloat(inst.getImm());
			return new CdbTrans(this.cdbId, val, inst);
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
			throw new IllegalStateException();
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
		return "LoadBuffer [idx=" + idx + ", state=" + state + ", inst=" + inst + ", dst=" + inst.getDst()
				+ ", addr=" + inst.getImm() + ", time="+ time + "]";
	}

}