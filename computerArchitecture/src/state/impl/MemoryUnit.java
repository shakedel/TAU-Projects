package state.impl;

import peripherals.InstructionStatus;
import peripherals.Memory;
import peripherals.Registers;
import peripherals.Registers.Register;
import cdb.CDB;
import cdb.CdbId;
import cdb.CdbTrans;
import state.AcceptsInstructions;
import sun.awt.util.IdentityLinkedList;
import data.instruction.Instruction;

public class MemoryUnit implements AcceptsInstructions {

	private final Memory memory;
	private final Registers[] regs;
	private final InstructionStatus[] instructionStatus;

	private LoadBuffer[] loadBuffers;
	private StoreBuffer[] storeBuffers;
	private int numFunctionalUnits;

	// we tick() the oldest station first
	IdentityLinkedList<Buffer> buffersAge = new IdentityLinkedList<Buffer>();

	public MemoryUnit(Memory memory, Registers[] regs, InstructionStatus[] instructionStatus, CDB cdb, int delay, int numLoadBuffers, int numStoreBuffers, int numFunctionalUnits) {
		this.memory = memory;
		this.regs = regs;
		this.instructionStatus = instructionStatus;

		this.loadBuffers = new LoadBuffer[numLoadBuffers];
		for (int i=0; i<loadBuffers.length; i++) {
			LoadBuffer buffer = new LoadBuffer(i, CdbId.Type.LD, delay, cdb);
			loadBuffers[i] = buffer;
			this.buffersAge.add(buffer);
		}
		this.storeBuffers = new StoreBuffer[numStoreBuffers];
		for (int i=0; i<storeBuffers.length; i++) {
			StoreBuffer buffer = new StoreBuffer(i, null, delay, cdb);
			storeBuffers[i] = buffer;
			this.buffersAge.add(buffer);
		}
		this.numFunctionalUnits = numFunctionalUnits;
	}

	@Override
	public boolean isEmpty() {
		for (Buffer buffer: this.loadBuffers) {
			if (buffer.state != BufferState.IDLE) {
				return false;
			}
		}
		for (Buffer buffer: this.storeBuffers) {
			if (buffer.state != BufferState.IDLE) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean acceptInstruction(Instruction instruction) {
		Buffer[] target;
		switch (instruction.getOpcode()) {
		case LD:
			target = this.loadBuffers;
			break;
		case ST:
			target = this.storeBuffers;
			break;
		default:
			throw new IllegalArgumentException("unknown opcode: "+instruction.getOpcode());
		}
		for (Buffer buffer: target) {
			if (buffer.acceptInstruction(instruction)) {
				this.buffersAge.add(buffer);
				return true;
			}
		}
		return false;
	}

	@Override
	public void tick() {
		for (Buffer buffer: this.loadBuffers) {
			if (buffer.prepCdbTrans()) {
				this.buffersAge.remove(buffer);
			}
		}
		for (Buffer buffer: this.storeBuffers) {
			if (buffer.prepCdbTrans()) {
				this.buffersAge.remove(buffer);
			}
		}

		for (Buffer buffer: this.buffersAge) {
			buffer.tick();
		}

		for (Buffer buffer: this.loadBuffers) {
			buffer.sendPendingCdbTrans();
		}

	}

	private class LoadBuffer extends Buffer {

		public LoadBuffer(int idx, CdbId.Type cdbType, int delay, CDB cdb) {
			super(idx, cdbType, delay, cdb);
		}

		@Override
		protected void reset() {
			super.reset();
		}

		@Override
		public void set(Instruction inst) {
			regs[inst.getThreadIdx()].get(inst.getDst()).set(this.cdbId);
			this.state = BufferState.READY;
		}

		@Override
		protected void incomingCdbTrans(CdbTrans cdbTrans) {
			return;
		}

		@Override 
		protected CdbTrans generateCdbTrans() {
			if (this.state == BufferState.EXECUTING && this.time==1) {
				numFunctionalUnits++;

				int intVal = memory.read(inst.getImm());
				float val = Float.intBitsToFloat(intVal);
				return new CdbTrans(this.cdbId, val, inst);
			}
			return null;
		}

		@Override 
		public void tick() {
			switch (this.state) {
			case IDLE:
				break;
			case WAITING:
				throw new IllegalStateException();
			case READY:
				if (numFunctionalUnits > 0) {
					numFunctionalUnits--;
					this.time = this.delay;
					instructionStatus[this.inst.getThreadIdx()].setExecComp(this.inst);
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
		public String toString() {
			return "LoadBuffer [idx=" + idx + ", state=" + state + ", inst=" + inst + ", dst=" + inst.getDst()
					+ ", addr=" + inst.getImm() + ", time="+ time + "]";
		}

	}

	private class StoreBuffer extends Buffer {

		Float Vj = null;
		CdbId Qj = null;

		public StoreBuffer(int idx, CdbId.Type cdbType, int delay, CDB cdb) {
			super(idx, cdbType, delay, cdb);
		}

		@Override
		protected void reset() {
			super.reset();
			this.Vj = null;
			this.Qj = null;
		}

		@Override
		public void set(Instruction inst) {
			Register regJ = regs[inst.getThreadIdx()].get(inst.getSrc1());
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
				numFunctionalUnits++;
				int intVal = Float.floatToRawIntBits(this.Vj);
				memory.write(inst.getImm(), intVal);
				return CdbTrans.NO_TRANS;
			}
			return null;
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
				if (numFunctionalUnits > 0) {
					numFunctionalUnits--;
					this.time = this.delay;
					instructionStatus[this.inst.getThreadIdx()].setExecComp(this.inst);
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
		public String toString() {
			return "StoreBuffer[idx=" + idx + ", state=" + state + ", inst=" + inst + ", Vj=" + Vj
					+ ", Qj=" + Qj + ", addr=" + this.inst.getImm() +", time="+ time + "]";
		}

	}

}
