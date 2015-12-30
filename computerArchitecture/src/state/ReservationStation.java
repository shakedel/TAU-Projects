package state;

import java.util.Observable;
import java.util.Observer;

import state.CDB.CdbTrans;
import state.Registers.Register;
import sun.awt.util.IdentityLinkedList;
import data.instruction.Instruction;
import data.instruction.InstructionImpl.InstructionR;

public class ReservationStation implements AcceptsInstructions {

	private final Registers[] regs;
	private final InstructionStatus[] instructionStatus;
	
	private Station[] stations;
	private int numFunctionalUnits;
	// we tick() the oldest station first
	private IdentityLinkedList<Station> stationsAge = new IdentityLinkedList<Station>();
	
	public ReservationStation(Registers[] regs, InstructionStatus[] instructionStatus, CDB cdb, CdbId.Type cdbType, int delay, int numStations, int numFunctionalUnits) {
		if (regs.length != 2) {
			throw new IllegalArgumentException("expectes 2 register files but got: "+regs.length);
		}
		this.regs = regs;
		this.instructionStatus = instructionStatus;
		
		this.stations = new Station[numStations];
		for (int i=0; i<stations.length; i++) {
			Station station = new Station(i, cdbType, delay, cdb);
			stations[i] = station;
			this.stationsAge.add(station);
		}
		this.numFunctionalUnits = numFunctionalUnits;
	}
	
	@Override
	public boolean acceptInstruction(Instruction instruction) {
		InstructionR instR = (InstructionR) instruction;
		
		for (Station station: this.stations) {
			if (station.state == EntryState.IDLE) {
				station.set(instR);
				regs[instR.getThreadIdx()].get(instR.getDst()).set(station.cdbId);
				// update station age
				this.stationsAge.remove(station);
				this.stationsAge.push(station);
				
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean isEmpty() {
		for (Station station: this.stations) {
			if (station.state != EntryState.IDLE) {
				return false;
			}
		}
		return true;
	}
	
	public void tick() {
		for (Station station: this.stationsAge) {
			station.tick();
		}
	}
	
	private static enum EntryState {
		IDLE, WAITING, READY, EXECUTING;
	}
	
	private class Station implements Observer {
		
		private final int delay;
		private final CdbId cdbId;
		private final CDB cdb;
		
		EntryState state = EntryState.IDLE;
		InstructionR inst;
		Float Vj = null;
		Float Vk = null;
		CdbId Qj = null;
		CdbId Qk = null;
		
		Integer time;
		
		public Station(int idx, CdbId.Type cdbType, int delay, CDB cdb) {
			this.cdb = cdb;
			this.cdb.addObserver(this);
			this.delay = delay;
			this.cdbId = new CdbId(cdbType, idx);
		}
		
		public void set(InstructionR inst) {
			this.time = delay;
			this.inst = inst;
			
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
			this.state = (Qj!=null || Qk!=null) ? EntryState.WAITING : EntryState.READY;
			instructionStatus[this.inst.getThreadIdx()].add(this.inst);
		}
		
		public void tick() {
			switch (this.state) {
			case IDLE:
				break;
			case WAITING:
				if (this.Vj!=null && this.Vk!=null) {
					this.state = EntryState.READY;
				}
				break;
			case READY:
				if (numFunctionalUnits > 0) {
					numFunctionalUnits--;
					instructionStatus[this.inst.getThreadIdx()].setExecComp(this.inst);
					this.state = EntryState.EXECUTING;
				}
				break;
			case EXECUTING:
				if (--this.time == 0) {
					numFunctionalUnits++;
					cdb.notifyObservers(new CdbTrans(this.cdbId, inst.calc(this.Vj, this.Vk)));
					instructionStatus[inst.getThreadIdx()].setWriteResult(inst);
					this.state = EntryState.IDLE;
				}
				break;
			default: 
				throw new IllegalArgumentException("unknown state: "+this.state);
			}
		}

		@Override
		public void update(Observable obs, Object data) {
			if (this.state != EntryState.WAITING) {
				return;
			}
			CdbTrans cdbTrans = (CdbTrans) data;
			if (cdbTrans.getCdbId().equals(this.Qj)) {
				this.Qj = null;
				this.Vj = cdbTrans.getValue();
			}
			if (cdbTrans.getCdbId().equals(this.Qk)) {
				this.Qk = null;
				this.Vk = cdbTrans.getValue();
			}
		}
	}

}
