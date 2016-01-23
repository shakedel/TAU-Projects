package eladsh.computer_architecture.modules.impl;

import java.util.LinkedList;
import java.util.List;

import eladsh.computer_architecture.peripherals.InstructionStatus;
import eladsh.computer_architecture.peripherals.Memory;
import eladsh.computer_architecture.data.instruction.Instruction;
import eladsh.computer_architecture.data.instruction.InstructionImpl;
import eladsh.computer_architecture.data.opcode.Opcode;
import eladsh.computer_architecture.modules.AcceptsInstructions;
import eladsh.computer_architecture.modules.Tickable;

/**
 * module to simulate an instruction queue for a thread
 */
public class InstructionQueue implements Tickable, AcceptsInstructions {
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

		public void preTick() {
			// do nothing
		};
		
		@Override
		public void tick() {
			// do nothing
		}
		
		@Override
		public void postTick() {
			// do nothing
		}
	};
	
	private InstructionQueue(InstructionStatus instructionStatus, ReservationStation addReservationStation, ReservationStation mulReservationStation, MemoryUnit memoryUnit, int threadIdx) {
		this.instructionStatus = instructionStatus;
		
		this.addReservationStation = addReservationStation;
		this.mulReservationStation = mulReservationStation;
		this.memoryUnit = memoryUnit;
	}
	
	/**
	 * @param mem {@link Memory} to read/write from
	 * @param instructionStatus {@link InstructionStatus} to update trace
	 * @param initAddr initial address in memory to read {@link Instruction}s from
	 * @param addReservationStation add/sub {@link ReservationStation}
	 * @param mulReservationStation mul/div {@link ReservationStation}
	 * @param memoryUnit {@link MemoryUnit}
	 * @param threadIdx index of thread
	 */
	public InstructionQueue(Memory mem, InstructionStatus instructionStatus, int initAddr, ReservationStation addReservationStation, ReservationStation mulReservationStation, MemoryUnit memoryUnit, int threadIdx) {
		this(instructionStatus, addReservationStation, mulReservationStation, memoryUnit, threadIdx);
		int addr = initAddr;
		Instruction inst;
		int i=0;
		do {
			inst = InstructionImpl.parseInstruction(mem.readInt(addr), threadIdx, i++);
			instructions.add(inst);
			addr+=2;
		} while (!inst.getOpcode().equals(Opcode.HALT));
	}
	
	@Override
	public boolean isEmpty() {
		return this.instructions.isEmpty();
	}
	
	@Override
	public void preTick() {
		// do nothing
	}
	
	@Override
	public void tick() {
		// do nothing
	}
	
	@Override
	public void postTick() {
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
			if (target.acceptInstruction(instruction)) {
				this.instructionStatus.add(instruction);
				this.instructions.remove(0);
			} else {
				break;
			}
		}
		
	}

	@Override
	public boolean acceptInstruction(Instruction instruction) {
		return false;
	}
	
}
