package eladsh.computer_architecture.modules.impl;

import java.util.LinkedList;
import java.util.List;

import eladsh.computer_architecture.peripherals.InstructionStatus;
import eladsh.computer_architecture.peripherals.RegistersStatus;
import eladsh.computer_architecture.peripherals.cdb.CDB;
import eladsh.computer_architecture.peripherals.cdb.CdbId;
import eladsh.computer_architecture.data.instruction.Instruction;
import eladsh.computer_architecture.modules.AcceptsInstructions;
import eladsh.computer_architecture.modules.impl.buffers.Buffer;
import eladsh.computer_architecture.modules.impl.buffers.BufferState;
import eladsh.computer_architecture.modules.impl.buffers.StationBuffer;

/**
 * Module to simulate the reservation stations and the float functional units
 */
public class ReservationStation implements AcceptsInstructions {

	public final InstructionStatus[] instructionStatus;
	public final RegistersStatus[] regs;
	public final String name;
	
	private StationBuffer[] stations;
	int numFunctionalUnits;
	// we tick() the oldest station first
	private List<StationBuffer> stationsAge = new LinkedList<StationBuffer>();
	
	public ReservationStation(String name, RegistersStatus[] regs, InstructionStatus[] instructionStatus, CDB cdb, CdbId.Type cdbType, int delay, int numStations, int numFunctionalUnits) {
		this.name = name;
		this.instructionStatus = instructionStatus;
		this.regs = regs;
		
		this.stations = new StationBuffer[numStations];
		for (int i=0; i<stations.length; i++) {
			StationBuffer station = new StationBuffer(this, i, cdbType, delay, cdb, regs);
			stations[i] = station;
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
		for (Buffer station: this.stations) {
			if (station.getState() != BufferState.IDLE) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean acceptInstruction(Instruction instruction) {
		for (StationBuffer station: this.stations) {
			if (station.acceptInstruction(instruction)) {
				this.stationsAge.add(station);
				return true;
			}
		}
		return false;
	}

	@Override
	public void preTick() {
		for (StationBuffer station: this.stations) {
			if (station.prepCdbTrans()) {
				this.stationsAge.remove(station);
			}
		}
	}
	
	@Override
	public void tick() {
		for (StationBuffer station: stationsAge) {
			station.tick();
		}
	}
	
	@Override
	public void postTick() {
		for (StationBuffer station: this.stations) {
			station.sendPendingCdbTrans();
		}
	}

}
