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
import state.LoadBuffers;
import state.Registers;
import state.ReservationStation;
import state.StoreBuffers;


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
		CDB cdb = new CDB();
		Memory memory = new Memory(1<<16, memInFile);
		Registers[] regs = {new Registers(cdb), new Registers(cdb)};
		InstructionStatus[] instructionStatus = {new InstructionStatus(), new InstructionStatus()};
		ReservationStation addReservationStation = new ReservationStation(regs, instructionStatus, cdb, CdbId.Type.ADD, Props.get().getAddDelay(), Props.get().getNumAddReservations(), Props.get().getNumOfAdds());
		ReservationStation mulReservationStation = new ReservationStation(regs, instructionStatus, cdb, CdbId.Type.MUL, Props.get().getMulDelay(), Props.get().getNumMulReservations(), Props.get().getNumOfMuls());
		
		StoreBuffers storeBuffers = new StoreBuffers(memory, regs, instructionStatus, cdb, Props.get().getMemDelay(), Props.get().getNumStoreBuffers(), 1); 
		LoadBuffers loadBuffers = new LoadBuffers(memory, regs, instructionStatus, cdb, Props.get().getMemDelay(), Props.get().getNumLoadBuffers(), 1); 
		
		InstructionQueue instructiuonQueue0 = new InstructionQueue(memory, instructionStatus[0], 0, addReservationStation, mulReservationStation, loadBuffers, storeBuffers, 0);
		InstructionQueue instructiuonQueue1 = new InstructionQueue(memory, instructionStatus[1], 1, addReservationStation, mulReservationStation, loadBuffers, storeBuffers, 1);
		
		// tick
		while (!instructiuonQueue0.isEmpty() || !instructiuonQueue1.isEmpty() || !addReservationStation.isEmpty() || !mulReservationStation.isEmpty()) {
			Clock.tick();
			addReservationStation.tick();
			mulReservationStation.tick();
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