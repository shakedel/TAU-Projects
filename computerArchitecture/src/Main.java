import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mem.Memory;
import misc.Props;
import state.CDB;
import state.CdbId;
import state.Clock;
import state.InstructionQueue;
import state.InstructionStatus;
import state.LoadBuffers;
import state.Registers;
import state.ReservationStation;
import state.StoreBuffers;
import data.instruction.Instruction;
import data.instruction.InstructionImpl;
import data.opcode.Opcode;


public class Main {
	public static void main(String[] args) throws IOException {
		if (args.length != 6) {
			throw new IllegalArgumentException("accepts exactly 6 arguments!");
		}
		
		// Set props
		Props.set(new File(args[0]));
		
		// init units
		CDB cdb = new CDB();
		Memory memory = new Memory(1<<16, new File(args[1]));
		Registers regs = new Registers(cdb);
		InstructionStatus instructionStatus = new InstructionStatus();
		ReservationStation addReservationStation = new ReservationStation(regs, instructionStatus, cdb, CdbId.Type.ADD, Props.get().getAddDelay(), Props.get().getNumAddReservations(), Props.get().getNumOfAdds());
		ReservationStation mulReservationStation = new ReservationStation(regs, instructionStatus, cdb, CdbId.Type.MUL, Props.get().getMulDelay(), Props.get().getNumMulReservations(), Props.get().getNumOfMuls());
		
		///////////////////////////////////////////////////////
		// TODO: how many memory functional units are there???
		///////////////////////////////////////////////////////
		StoreBuffers storeBuffers = new StoreBuffers(memory, regs, instructionStatus, cdb, Props.get().getMemDelay(), Props.get().getNumStoreBuffers(), 1); 
		LoadBuffers loadBuffers = new LoadBuffers(memory, regs, instructionStatus, cdb, Props.get().getMemDelay(), Props.get().getNumLoadBuffers(), 1); 
		
		// fill instructions;
		List<Instruction> instructions = new ArrayList<Instruction>();
		instructions.add(new InstructionImpl.InstructionR("1st inst", Opcode.ADDS, 3, 1, 4));
		instructions.add(new InstructionImpl.InstructionR("2nd inst", Opcode.ADDS, 2, 3, 6));
		
		InstructionQueue instructiuonQueue = new InstructionQueue(instructions, instructionStatus, addReservationStation, mulReservationStation, loadBuffers, storeBuffers);
		
		// tick
		while (!instructiuonQueue.isEmpty() || !addReservationStation.isEmpty() || !mulReservationStation.isEmpty()) {
			Clock.tick();
			addReservationStation.tick();
			mulReservationStation.tick();
			instructiuonQueue.tick();
		}
		
		// write output files
		memory.store(new File(args[2]));
		regs.store(new File(args[3]));
		instructionStatus.store(new File(args[4]), new File(args[5]));
		
		// TODO: calculate CPI and write to file
		
	}
	
}
