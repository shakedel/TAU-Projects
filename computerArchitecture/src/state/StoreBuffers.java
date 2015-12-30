package state;

import java.util.Observable;
import java.util.Observer;

import mem.Memory;
import state.CDB.CdbTrans;
import state.Registers.Register;
import sun.awt.util.IdentityLinkedList;
import data.instruction.Instruction;
import data.instruction.InstructionImpl.InstructionLD;

public class StoreBuffers implements AcceptsInstructions {

	private final Memory memory;
	private final Registers[] regs;
	private final InstructionStatus[] instructionStatus;
	
	private StoreBuffer[] buffers;
	private int numFunctionalUnits;
	// we tick() the oldest station first
	private IdentityLinkedList<StoreBuffer> buffersAge = new IdentityLinkedList<StoreBuffer>();
	
	public StoreBuffers(Memory memory, Registers[] regs, InstructionStatus[] instructionStatus, CDB cdb, int delay, int numBuffers, int numFunctionalUnits) {
		this.memory = memory;
		this.regs = regs;
		this.instructionStatus = instructionStatus;
		
		this.buffers = new StoreBuffer[numBuffers];
		for (int i=0; i<buffers.length; i++) {
			StoreBuffer buffer = new StoreBuffer(i, delay, cdb);
			buffers[i] = buffer;
			this.buffersAge.add(buffer);
		}
		this.numFunctionalUnits = numFunctionalUnits;
	}
	
	@Override
	public boolean acceptInstruction(Instruction instruction) {
		InstructionLD instLD = (InstructionLD) instruction;
		
		for (StoreBuffer buffer: this.buffers) {
			if (buffer.state == EntryState.IDLE) {
				buffer.set(instLD);
				regs[instLD.getThreadIdx()].get(instLD.getDst()).set(buffer.cdbId);
				// update station age
				this.buffersAge.remove(buffer);
				this.buffersAge.push(buffer);
				
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean isEmpty() {
		for (StoreBuffer buffer: this.buffers) {
			if (buffer.state != EntryState.IDLE) {
				return false;
			}
		}
		return true;
	}
	
	public void tick() {
		for (StoreBuffer buffer: this.buffersAge) {
			buffer.tick();
		}
	}
	
	private static enum EntryState {
		IDLE, WAITING, READY, EXECUTING;
	}
	
	private class StoreBuffer implements Observer {
		
		private final int delay;
		private final CdbId cdbId;
		private final CDB cdb;
		
		EntryState state = EntryState.IDLE;
		InstructionLD inst;
		Float Vk = null;
		CdbId Qk = null;
		
		Integer time;
		
		public StoreBuffer(int idx, int delay, CDB cdb) {
			this.cdb = cdb;
			this.cdb.addObserver(this);
			this.delay = delay;
			this.cdbId = new CdbId(CdbId.Type.MEM, idx);
		}
		
		public void set(InstructionLD inst) {
			this.time = delay;
			this.inst = inst;
			
			Register regK = regs[inst.getThreadIdx()].get(inst.getSrc1());
			switch (regK.getState()) {
			case VAL:
				this.Vk = regK.getVal();
				this.Qk = null;
				break;
			case CDB_ID:
				this.Vk = null;
				this.Qk = regK.getCdbId();
				break;
			default:
				throw new IllegalArgumentException("unknown register state: "+regK.getState());
			}
			this.state = Qk!=null ? EntryState.WAITING : EntryState.READY;
			instructionStatus[this.inst.getThreadIdx()].add(this.inst);
		}
		
		public void tick() {
			switch (this.state) {
			case IDLE:
				break;
			case WAITING:
				if (this.Vk!=null) {
					this.state = EntryState.READY;
				}
				break;
			case READY:
				if (numFunctionalUnits > 0) {
					numFunctionalUnits--;
					instructionStatus[this.inst.getThreadIdx()].setExecComp(this.inst);
					this.state = EntryState.EXECUTING;
				}
				break;
			case EXECUTING:
				if (--this.time == 0) {
					numFunctionalUnits++;
					int intVal = Float.floatToRawIntBits(this.Vk);
					memory.write(inst.getImm(), intVal);
					instructionStatus[inst.getThreadIdx()].setWriteResult(inst);
					this.state = EntryState.IDLE;
				}
				break;
			default: 
				throw new IllegalArgumentException("unknown state: "+this.state);
			}
		}

		@Override
		public void update(Observable obs, Object data) {
			if (this.state != EntryState.WAITING) {
				return;
			}
			CdbTrans cdbTrans = (CdbTrans) data;
			if (cdbTrans.getCdbId().equals(this.Qk)) {
				this.Qk = null;
				this.Vk = cdbTrans.getValue();
			}
		}
	}

}
