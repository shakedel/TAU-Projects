package state;

import java.util.LinkedList;
import java.util.List;

import data.instruction.Instruction;
import data.instruction.InstructionImpl;
import data.opcode.Opcode;
import mem.Memory;

public class InstructionQueue {
	private static final int ISSUES_PER_CYCLE = 2;
	
	private List<Instruction> instructions = new LinkedList<Instruction>();
	private final ReservationStation addReservationStation;
	private final ReservationStation mulReservationStation;
	
	
	private InstructionQueue(ReservationStation addReservationStation, ReservationStation mulReservationStation) {
		this.addReservationStation = addReservationStation;
		this.mulReservationStation = mulReservationStation;
	}
	
	public InstructionQueue(Memory mem, int initAddr, ReservationStation addReservationStation, ReservationStation mulReservationStation) {
		this(addReservationStation, mulReservationStation);
		int addr = initAddr;
		Instruction inst;
		do {
			inst = InstructionImpl.parseInstruction(mem.read(addr));
			instructions.add(inst);
			addr+=2;
		} while (!inst.getOpcode().equals(Opcode.HALT));
	}
	
	public InstructionQueue(List<Instruction> instructions, ReservationStation addReservationStation, ReservationStation mulReservationStation) {
		this(addReservationStation, mulReservationStation);
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
				// TODO:
				throw new IllegalStateException();
//				break;
			case ST:
				// TODO:
				throw new IllegalStateException();
//				break;
			case NOP:
				// TODO:
				throw new IllegalStateException();
//				break;
			case HALT:
				// TODO:
				throw new IllegalStateException();
//				break;
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
