package main;
import java.io.File;
import java.io.IOException;

import mem.Memory;
import misc.Props;
import state.CDB;
import state.CdbId;
import state.Clock;
import state.InstructionQueue;
import state.InstructionStatus;
import state.MemoryUnit;
import state.Registers;
import state.ReservationStation;


public class Main {
	public static void main(String[] args) throws IOException {
		if (args.length != 9) {
			throw new IllegalArgumentException("accepts exactly 9 arguments!");
		}
		
		new Main(new File(args[0]), new File(args[1]), new File(args[2]), 
				new File(args[3]), new File(args[4]), new File(args[5]),
				new File(args[6]), new File(args[7]), new File(args[8]));
	}
	
	
	
	public Main(File cfgFile, File memInFile, File memOutFile, File regOut0File, File trace0File, File cpi0File, File regOut1File, File trace1File, File cpi1File) throws IOException {
		Props.set(cfgFile);
		
		// init units
		InstructionStatus[] instructionStatus = {new InstructionStatus(), new InstructionStatus()};
		CDB cdb = new CDB(instructionStatus);
		Memory memory = new Memory(1<<16, memInFile);
		Registers[] regs = {new Registers(cdb), new Registers(cdb)};
		ReservationStation addReservationStation = new ReservationStation("add", regs, instructionStatus, cdb, CdbId.Type.ADD, Props.get().getAddDelay(), Props.get().getNumAddReservations(), Props.get().getNumOfAdds());
		ReservationStation mulReservationStation = new ReservationStation("mul", regs, instructionStatus, cdb, CdbId.Type.MUL, Props.get().getMulDelay(), Props.get().getNumMulReservations(), Props.get().getNumOfMuls());
		MemoryUnit memoryUnit = new MemoryUnit(memory, regs, instructionStatus, cdb, Props.get().getMemDelay(), Props.get().getNumLoadBuffers(), Props.get().getNumStoreBuffers(), 1);
		
		InstructionQueue instructiuonQueue0 = new InstructionQueue(memory, instructionStatus[0], 0, addReservationStation, mulReservationStation, memoryUnit, 0);
		InstructionQueue instructiuonQueue1 = new InstructionQueue(memory, instructionStatus[1], 1, addReservationStation, mulReservationStation, memoryUnit, 1);
		
		// tick
		while (!instructiuonQueue0.isEmpty() || !instructiuonQueue1.isEmpty() || !addReservationStation.isEmpty() || !mulReservationStation.isEmpty() || !memoryUnit.isEmpty()) {
			Clock.tick();
			// first tick units
			addReservationStation.tick();
			mulReservationStation.tick();
			memoryUnit.tick();
			
			// then tick queues
			instructiuonQueue0.tick();
			instructiuonQueue1.tick();
		}
		
		// write output files
		memory.store(memOutFile);
		regs[0].store(regOut0File);
		regs[1].store(regOut1File);
		instructionStatus[0].store(trace0File, cpi0File);
		instructionStatus[1].store(trace1File, cpi1File);

	}

}