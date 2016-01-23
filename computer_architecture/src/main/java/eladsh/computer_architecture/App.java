package eladsh.computer_architecture;
import java.io.File;
import java.io.IOException;

import eladsh.computer_architecture.peripherals.Clock;
import eladsh.computer_architecture.peripherals.InstructionStatus;
import eladsh.computer_architecture.peripherals.Memory;
import eladsh.computer_architecture.peripherals.Registers;
import eladsh.computer_architecture.peripherals.cdb.CDB;
import eladsh.computer_architecture.peripherals.cdb.CdbId;


import eladsh.computer_architecture.misc.Props;
import eladsh.computer_architecture.state.impl.InstructionQueue;
import eladsh.computer_architecture.state.impl.MemoryUnit;
import eladsh.computer_architecture.state.impl.ReservationStation;


public class App {
	public static void main(String[] args) throws IOException {
		if (args.length != 9) {
			throw new IllegalArgumentException("accepts exactly 9 arguments!");
		}
		
		new App(new File(args[0]), new File(args[1]), new File(args[2]), 
				new File(args[3]), new File(args[4]), new File(args[5]),
				new File(args[6]), new File(args[7]), new File(args[8]));
	}
	
	
	
	public App(File cfgFile, File memInFile, File memOutFile, File regOut0File, File trace0File, File cpi0File, File regOut1File, File trace1File, File cpi1File) throws IOException {
		Props props = new Props(cfgFile);
		Clock clock = new Clock();
		
		// init units
		InstructionStatus[] instructionStatus = {new InstructionStatus(clock), new InstructionStatus(clock)};
		CDB cdb = new CDB(instructionStatus);
		Memory memory = new Memory(1<<16, memInFile);
		Registers[] regs = {new Registers(0, cdb), new Registers(1, cdb)};
		ReservationStation addReservationStation = new ReservationStation("add", regs, instructionStatus, cdb, CdbId.Type.ADD, props.getAddDelay(), props.getNumAddReservations(), props.getNumOfAdds());
		ReservationStation mulReservationStation = new ReservationStation("mul", regs, instructionStatus, cdb, CdbId.Type.MUL, props.getMulDelay(), props.getNumMulReservations(), props.getNumOfMuls());
		MemoryUnit memoryUnit = new MemoryUnit(memory, regs, instructionStatus, cdb, props.getMemDelay(), props.getNumLoadBuffers(), props.getNumStoreBuffers(), 1);
		
		InstructionQueue instructiuonQueue0 = new InstructionQueue(memory, instructionStatus[0], 0, addReservationStation, mulReservationStation, memoryUnit, 0);
		InstructionQueue instructiuonQueue1 = new InstructionQueue(memory, instructionStatus[1], 1, addReservationStation, mulReservationStation, memoryUnit, 1);
		
		// tick
		while (!instructiuonQueue0.isEmpty() || !instructiuonQueue1.isEmpty() || !addReservationStation.isEmpty() || !mulReservationStation.isEmpty() || !memoryUnit.isEmpty()) {
			clock.tick();
			
			// preTick
			addReservationStation.preTick();
			mulReservationStation.preTick();
			memoryUnit.preTick();
			instructiuonQueue0.preTick();
			instructiuonQueue1.preTick();

			// tick
			addReservationStation.tick();
			mulReservationStation.tick();
			memoryUnit.tick();
			instructiuonQueue0.tick();
			instructiuonQueue1.tick();
			
			// postTick
			addReservationStation.postTick();
			mulReservationStation.postTick();
			memoryUnit.postTick();
			instructiuonQueue0.postTick();
			instructiuonQueue1.postTick();
		}
		//
		
		// write output files
		memory.store(memOutFile);
		regs[0].store(regOut0File);
		regs[1].store(regOut1File);
		instructionStatus[0].store(trace0File, cpi0File);
		instructionStatus[1].store(trace1File, cpi1File);

	}

}