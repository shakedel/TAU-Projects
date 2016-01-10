package state.impl;

import java.util.LinkedList;
import java.util.List;

import peripherals.InstructionStatus;
import peripherals.Memory;

import state.AcceptsInstructions;
import state.Tickable;

import data.instruction.Instruction;
import data.instruction.InstructionImpl;
import data.opcode.Opcode;

public class InstructionQueue implements Tickable {
	private static final int ISSUES_PER_CYCLE = 2;
	
	private final InstructionStatus instructionStatus;
	
	private final ReservationStation addReservationStation;
	private final ReservationStation mulReservationStation;
	private final MemoryUnit memoryUnit;
	
	private List<Instruction> instructions = new LinkedList<Instruction>();
	private AcceptsInstructions voidInsructionTarget = new AcceptsInstructions() {
		
		@Override
		public boolean isEmpty() {
			return true;
		}
		
		@Override
		public boolean acceptInstruction(Instruction instruction) {
			return true;
		}

		@Override
		public void tick() {
			// do nothing
		}
	};
	
	private InstructionQueue(InstructionStatus instructionStatus, ReservationStation addReservationStation, ReservationStation mulReservationStation, MemoryUnit memoryUnit, int threadIdx) {
		this.instructionStatus = instructionStatus;
		
		this.addReservationStation = addReservationStation;
		this.mulReservationStation = mulReservationStation;
		this.memoryUnit = memoryUnit;
	}
	
	public InstructionQueue(Memory mem, InstructionStatus instructionStatus, int initAddr, ReservationStation addReservationStation, ReservationStation mulReservationStation, MemoryUnit memoryUnit, int threadIdx) {
		this(instructionStatus, addReservationStation, mulReservationStation, memoryUnit, threadIdx);
		int addr = initAddr;
		Instruction inst;
		int i=0;
		do {
			inst = InstructionImpl.parseInstruction(mem.read(addr), threadIdx, i++);
			instructions.add(inst);
			addr+=2;
		} while (!inst.getOpcode().equals(Opcode.HALT));
	}
	
	public InstructionQueue(List<Instruction> instructions, InstructionStatus instructionStatus, ReservationStation addReservationStation, ReservationStation mulReservationStation, MemoryUnit memoryUnit, int threadIdx) {
		this(instructionStatus, addReservationStation, mulReservationStation, memoryUnit, threadIdx);
		this.instructions = instructions;
	}
	
	public boolean isEmpty() {
		return this.instructions.isEmpty();
	}
	
	@Override
	public void tick() {
		for (int i=0; i<ISSUES_PER_CYCLE; i++) {
			if (this.instructions.isEmpty()) {
				return;
			}
			
			AcceptsInstructions target;
			
			Instruction instruction = instructions.get(0);
			switch (instruction.getOpcode()) {
			case ADDS:
			case SUBS:
				target = this.addReservationStation;
				break;
			case MULTS:
			case DIVS:
				target = this.mulReservationStation;
				break;
			case LD: 
			case ST:
				target = this.memoryUnit;
				break;
			case NOP:
			case HALT:
				target = this.voidInsructionTarget;
				break;
			default:
				throw new IllegalArgumentException("unknown opcode: "+instruction.getOpcode());
			}
			
			boolean issued = target.acceptInstruction(instruction);
			if (!issued) {
				break;
			} else {
				this.instructions.remove(0);
				this.instructionStatus.add(instruction);
			}
		}
	}
	
}
