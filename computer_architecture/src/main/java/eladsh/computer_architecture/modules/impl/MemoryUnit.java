package eladsh.computer_architecture.modules.impl;

import java.util.LinkedList;
import java.util.List;

import eladsh.computer_architecture.peripherals.InstructionStatus;
import eladsh.computer_architecture.peripherals.Memory;
import eladsh.computer_architecture.peripherals.RegistersStatus;
import eladsh.computer_architecture.peripherals.cdb.CDB;
import eladsh.computer_architecture.peripherals.cdb.CdbId;
import eladsh.computer_architecture.data.instruction.Instruction;
import eladsh.computer_architecture.modules.AcceptsInstructions;
import eladsh.computer_architecture.modules.impl.buffers.Buffer;
import eladsh.computer_architecture.modules.impl.buffers.BufferState;
import eladsh.computer_architecture.modules.impl.buffers.LoadBuffer;
import eladsh.computer_architecture.modules.impl.buffers.StoreBuffer;


/**
 * module to simulate the memory functional unit
 */
public class MemoryUnit implements AcceptsInstructions {

	public final Memory memory;
	public final InstructionStatus[] instructionStatus;
	public final RegistersStatus[] regs;

	private LoadBuffer[] loadBuffers;
	private StoreBuffer[] storeBuffers;
	private int numFunctionalUnits;

	// we tick() the oldest station first
	List<Buffer> buffersAge = new LinkedList<Buffer>();

	public MemoryUnit(Memory memory, RegistersStatus[] regs, InstructionStatus[] instructionStatus, CDB cdb, int delay, int numLoadBuffers, int numStoreBuffers, int numFunctionalUnits) {
		this.memory = memory;
		this.instructionStatus = instructionStatus;
		this.regs = regs;

		this.loadBuffers = new LoadBuffer[numLoadBuffers];
		for (int i=0; i<loadBuffers.length; i++) {
			LoadBuffer buffer = new LoadBuffer(this, i, CdbId.Type.LD, delay, cdb);
			loadBuffers[i] = buffer;
		}
		this.storeBuffers = new StoreBuffer[numStoreBuffers];
		for (int i=0; i<storeBuffers.length; i++) {
			StoreBuffer buffer = new StoreBuffer(this, i, null, delay, cdb);
			storeBuffers[i] = buffer;
		}
		this.numFunctionalUnits = numFunctionalUnits;
	}
	
	public boolean decrementNumFunctionalUnits() {
		if (this.numFunctionalUnits > 0) {
			this.numFunctionalUnits--;
			return true;
		}
		return false;
	}
	
	public void incrementNumFunctionalUnits() {
		this.numFunctionalUnits++;
	}

	@Override
	public boolean isEmpty() {
		for (Buffer buffer: this.loadBuffers) {
			if (buffer.getState() != BufferState.IDLE) {
				return false;
			}
		}
		for (Buffer buffer: this.storeBuffers) {
			if (buffer.getState() != BufferState.IDLE) {
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
				buffer.acceptInstruction(instruction);
				this.buffersAge.add(buffer);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void preTick() {
		for (Buffer buffer: this.loadBuffers) {
			if (buffer.prepCdbTrans()) {
				if (!this.buffersAge.remove(buffer)) {
					throw new IllegalStateException();
				}
			}
		}
	}
	
	@Override
	public void tick() {
		for (Buffer buffer: this.storeBuffers) {
			if (buffer.prepCdbTrans()) {
				if (!this.buffersAge.remove(buffer)) {
					throw new IllegalStateException();
				}
			}
		}
		
		for (Buffer buffer: this.buffersAge) {
			buffer.tick();
		}

	}
	
	@Override
	public void postTick() {
		for (Buffer buffer: this.loadBuffers) {
			buffer.sendPendingCdbTrans();
		}
	}

}
