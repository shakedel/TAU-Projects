package state.impl;

import java.util.LinkedList;
import java.util.List;

import peripherals.InstructionStatus;
import peripherals.Registers;
import peripherals.Registers.Register;
import peripherals.cdb.CDB;
import peripherals.cdb.CdbId;
import peripherals.cdb.CdbTrans;

import state.AcceptsInstructions;



import data.instruction.Instruction;
import data.instruction.InstructionImpl.InstructionR;

public class ReservationStation implements AcceptsInstructions {

	private final Registers[] regs;
	private final InstructionStatus[] instructionStatus;
	public final String name;
	
	private Station[] stations;
	private int numFunctionalUnits;
	// we tick() the oldest station first
	private List<Station> stationsAge = new LinkedList<Station>();
	
	public ReservationStation(String name, Registers[] regs, InstructionStatus[] instructionStatus, CDB cdb, CdbId.Type cdbType, int delay, int numStations, int numFunctionalUnits) {
		this.name = name;
		this.regs = regs;
		this.instructionStatus = instructionStatus;
		
		this.stations = new Station[numStations];
		for (int i=0; i<stations.length; i++) {
			Station station = new Station(i, cdbType, delay, cdb);
			stations[i] = station;
		}
		this.numFunctionalUnits = numFunctionalUnits;
	}
	
	@Override
	public boolean isEmpty() {
		for (Buffer station: this.stations) {
			if (station.state != BufferState.IDLE) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean acceptInstruction(Instruction instruction) {
		for (Station station: this.stations) {
			if (station.acceptInstruction(instruction)) {
				this.stationsAge.add(station);
				return true;
			}
		}
		return false;
	}

	@Override
	public void tick() {
		for (Station station: this.stations) {
			if (station.prepCdbTrans()) {
				this.stationsAge.remove(station);
			}
		}
		
		for (Station station: stationsAge) {
			station.tick();
		}
		
		for (Station station: this.stations) {
			station.sendPendingCdbTrans();
		}
		
	}
	
	private class Station extends Buffer {
		
		Float Vj = null;
		Float Vk = null;
		CdbId Qj = null;
		CdbId Qk = null;
		
		public Station(int idx, CdbId.Type cdbType, int delay, CDB cdb) {
			super(idx, cdbType, delay, cdb);
		}

		@Override
		protected void reset() {
			super.reset();
			this.Vj = null;
			this.Vk = null;
			this.Qj = null;
			this.Qk = null;
		}
		
		@Override
		public void set(Instruction inst) {
			Register regJ = regs[inst.getThreadIdx()].get(inst.getSrc0());
			switch (regJ.getState()) {
			case VAL:
				this.Vj = regJ.getVal();
				this.Qj = null;
				break;
			case CDB_ID:
				this.Vj = null;
				this.Qj = regJ.getCdbId();
				break;
			default:
				throw new IllegalArgumentException("unknown register state: "+regJ.getState());
			}
			
			Register regK = regs[inst.getThreadIdx()].get(inst.getSrc1());
			switch (regK.getState()) {
			case VAL:
				this.Vk = regK.getVal();
				this.Qk = null;
				break;
			case CDB_ID:
				this.Vk = null;
				this.Qk = regK.getCdbId();
				break;
			default:
				throw new IllegalArgumentException("unknown register state: "+regK.getState());
			}
			
			regs[inst.getThreadIdx()].get(inst.getDst()).set(this.cdbId);
			
			this.state = (Qj!=null || Qk!=null) ? BufferState.WAITING : BufferState.READY;
		}
		
		@Override
		protected void incomingCdbTrans(CdbTrans cdbTrans) {
			if (this.state != BufferState.WAITING) {
				return;
			}
			if (cdbTrans.getCdbId().equals(this.Qj)) {
				this.Qj = null;
				this.Vj = cdbTrans.getValue();
			}
			if (cdbTrans.getCdbId().equals(this.Qk)) {
				this.Qk = null;
				this.Vk = cdbTrans.getValue();
			}
			if (this.Vj!=null && this.Vk!=null) {
				this.state = BufferState.READY;
			}
		}
			
		@Override 
		protected CdbTrans generateCdbTrans() {
			if (this.state == BufferState.EXECUTING && this.time==1) {
				numFunctionalUnits++;
				return new CdbTrans(this.cdbId, ((InstructionR) inst).calc(this.Vj, this.Vk), inst);
			}
			return null;
		}

		@Override 
		public void tick() {
			switch (this.state) {
			case IDLE:
				break;
			case WAITING:
				if (this.Vj!=null && this.Vk!=null) {
					this.state = BufferState.READY;
				}
				break;
			case READY:
				if (numFunctionalUnits > 0) {
					numFunctionalUnits--;
					this.time = this.delay;
					instructionStatus[this.inst.getThreadIdx()].setExecComp(this.inst);
					this.state = BufferState.EXECUTING;
				}
				break;
			case EXECUTING:
				this.time--;
				break;
			default: 
				throw new IllegalArgumentException("unknown state: "+this.state);
			}
		}

		@Override
		public String toString() {
			return "Station [stationIdx=" + idx + ", state=" + state + ", inst=" + inst + ", Vj=" + Vj
					+ ", Vk=" + Vk + ", Qj=" + Qj + ", Qk=" + Qk + ", time="
					+ time + "]";
		}

	}

}
