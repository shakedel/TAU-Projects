package state;

import java.util.LinkedList;
import java.util.List;

import mem.Memory;
import data.instruction.Instruction;
import data.instruction.InstructionImpl;
import data.opcode.Opcode;

public class InstructionQueue {
	private static final int ISSUES_PER_CYCLE = 2;
	
	private final InstructionStatus instructionStatus;
	
	private final ReservationStation addReservationStation;
	private final ReservationStation mulReservationStation;
	private final LoadBuffers loadBuffers;
	private final StoreBuffers storeBuffers;
	
	private List<Instruction> instructions = new LinkedList<Instruction>();
	private AcceptsInstructions voidInsructionTarget = new AcceptsInstructions() {
		
		@Override
		public boolean isEmpty() {
			return true;
		}
		
		@Override
		public boolean acceptInstruction(Instruction instruction) {
			instructionStatus.add(instruction);
			return true;
		}
	};
	
	private InstructionQueue(InstructionStatus instructionStatus, ReservationStation addReservationStation, ReservationStation mulReservationStation, LoadBuffers loadBuffers, StoreBuffers storeBuffers) {
		this.instructionStatus = instructionStatus;
		
		this.addReservationStation = addReservationStation;
		this.mulReservationStation = mulReservationStation;
		this.loadBuffers = loadBuffers;
		this.storeBuffers = storeBuffers;
	}
	
	public InstructionQueue(Memory mem, InstructionStatus instructionStatus, int initAddr, ReservationStation addReservationStation, ReservationStation mulReservationStation, LoadBuffers loadBuffers, StoreBuffers storeBuffers) {
		this(instructionStatus, addReservationStation, mulReservationStation, loadBuffers, storeBuffers);
		int addr = initAddr;
		Instruction inst;
		do {
			inst = InstructionImpl.parseInstruction(mem.read(addr));
			instructions.add(inst);
			addr+=2;
		} while (!inst.getOpcode().equals(Opcode.HALT));
	}
	
	public InstructionQueue(List<Instruction> instructions, InstructionStatus instructionStatus, ReservationStation addReservationStation, ReservationStation mulReservationStation, LoadBuffers loadBuffers, StoreBuffers storeBuffers) {
		this(instructionStatus, addReservationStation, mulReservationStation, loadBuffers, storeBuffers);
		this.instructions = instructions;
	}
	
	public boolean isEmpty() {
		return this.instructions.isEmpty();
	}
	
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
				target = this.loadBuffers;
				break;
			case ST:
				target = this.storeBuffers;
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
			}
		}
	}
	
}
