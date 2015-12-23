import java.util.ArrayList;
import java.util.List;

import misc.Props;
import state.CDB;
import state.CdbId;
import state.Clock;
import state.InstructionQueue;
import state.InstructionStatus;
import state.Registers;
import state.ReservationStation;
import data.instruction.Instruction;
import data.instruction.InstructionImpl;
import data.opcode.Opcode;


public class Main {
	public static void main(String[] args) {
		Props.set(3, 2, 2, 10, 0, 5, 5, 0, 0);
		CDB cdb = new CDB();
		Registers regs = new Registers(cdb);
		InstructionStatus instructionStatus = new InstructionStatus();
		ReservationStation addReservationStation = new ReservationStation(regs, instructionStatus, cdb, CdbId.Type.ADD, Props.get().getAddDelay(), Props.get().getNumAddReservations());
		ReservationStation mulReservationStation = new ReservationStation(regs, instructionStatus, cdb, CdbId.Type.MUL, Props.get().getMulDelay(), Props.get().getNumMulReservations());
		
		List<Instruction> instructions = new ArrayList<Instruction>();
		instructions.add(new InstructionImpl.InstructionR(Opcode.ADDS, 3, 1, 4));
		instructions.add(new InstructionImpl.InstructionR(Opcode.ADDS, 2, 3, 6));
		
		InstructionQueue instructiuonQueue = new InstructionQueue(instructions, addReservationStation, mulReservationStation);
		
		while (!instructiuonQueue.isEmpty() || !addReservationStation.isEmpty() || !mulReservationStation.isEmpty()) {
			Clock.tick();
			addReservationStation.tick();
			mulReservationStation.tick();
			instructiuonQueue.tick();
		}
	}
	
}
