package state;

import mem.Memory;
import state.CDB.CdbTrans;
import sun.awt.util.IdentityLinkedList;
import data.instruction.Instruction;
import data.instruction.InstructionImpl.InstructionLD;

public class LoadBuffers implements AcceptsInstructions {

	private final Memory memory;
	private final Registers regs;
	private final InstructionStatus instructionStatus;
	
	private LoadBuffer[] buffers;
	private int numFunctionalUnits;
	// we tick() the oldest station first
	private IdentityLinkedList<LoadBuffer> buffersAge = new IdentityLinkedList<LoadBuffer>();
	
	public LoadBuffers(Memory memory, Registers regs, InstructionStatus instructionStatus, CDB cdb, int delay, int numBuffers, int numFunctionalUnits) {
		this.memory = memory;
		this.regs = regs;
		this.instructionStatus = instructionStatus;
		
		this.buffers = new LoadBuffer[numBuffers];
		for (int i=0; i<buffers.length; i++) {
			LoadBuffer buffer = new LoadBuffer(i, delay, cdb);
			buffers[i] = buffer;
			this.buffersAge.add(buffer);
		}
		this.numFunctionalUnits = numFunctionalUnits;
	}
	
	@Override
	public boolean acceptInstruction(Instruction instruction) {
		InstructionLD instLD = (InstructionLD) instruction;
		
		for (LoadBuffer buffer: this.buffers) {
			if (buffer.state == EntryState.IDLE) {
				buffer.set(instLD);
				regs.get(instLD.getDst()).set(buffer.cdbId);
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
		for (LoadBuffer buffer: this.buffers) {
			if (buffer.state != EntryState.IDLE) {
				return false;
			}
		}
		return true;
	}
	
	public void tick() {
		for (LoadBuffer buffer: this.buffersAge) {
			buffer.tick();
		}
	}
	
	private static enum EntryState {
		IDLE, READY, EXECUTING;
	}
	
	private class LoadBuffer {
		
		private final int delay;
		private final CdbId cdbId;
		private final CDB cdb;
		
		EntryState state = EntryState.IDLE;
		InstructionLD inst;
		
		Integer time;
		
		public LoadBuffer(int idx, int delay, CDB cdb) {
			this.cdb = cdb;
			this.delay = delay;
			this.cdbId = new CdbId(CdbId.Type.MEM, idx);
		}
		
		public void set(InstructionLD inst) {
			this.time = delay;
			this.inst = inst;
			
			this.state = EntryState.READY;
			instructionStatus.add(this.inst);
		}
		
		public void tick() {
			switch (this.state) {
			case IDLE:
				break;
			case READY:
				if (numFunctionalUnits > 0) {
					numFunctionalUnits--;
					instructionStatus.setExecComp(this.inst);
					this.state = EntryState.EXECUTING;
				}
				break;
			case EXECUTING:
				if (--this.time == 0) {
					numFunctionalUnits++;
					int intVal = memory.read(inst.getImm());
					float val = Float.intBitsToFloat(intVal);
					
					cdb.notifyObservers(new CdbTrans(this.cdbId, val));
					instructionStatus.setWriteResult(inst);
					this.state = EntryState.IDLE;
				}
				break;
			default: 
				throw new IllegalArgumentException("unknown state: "+this.state);
			}
		}

	}

}
