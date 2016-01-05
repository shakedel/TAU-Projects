package state;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import state.CDB.CdbTrans;
import state.Registers.Register;
import data.instruction.Instruction;
import data.instruction.InstructionImpl.InstructionR;

public class ReservationStation implements AcceptsInstructions {

	private final Registers[] regs;
	private final InstructionStatus[] instructionStatus;
	public final String name;
	private final CDB cdb;
	
	private Station[] stations;
	private int numFunctionalUnits;
	// we tick() the oldest station first
	private List<Integer> stationsAge = new LinkedList<Integer>();
	private LinkedList<CdbTrans> pendingCdbTrans = new LinkedList<CdbTrans>();
	
	public ReservationStation(String name, Registers[] regs, InstructionStatus[] instructionStatus, CDB cdb, CdbId.Type cdbType, int delay, int numStations, int numFunctionalUnits) {
		if (regs.length != 2) {
			throw new IllegalArgumentException("expectes 2 register files but got: "+regs.length);
		}
		this.name = name;
		this.regs = regs;
		this.instructionStatus = instructionStatus;
		this.cdb = cdb;
		
		this.stations = new Station[numStations];
		for (int i=0; i<stations.length; i++) {
			Station station = new Station(i, cdbType, delay, cdb);
			stations[i] = station;
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
				this.stationsAge.add(station.stationIdx);
				
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
		for (int i=0; i<this.stations.length; i++) {
			this.stations[i].tickPrepCdbTrans();
		}
		
		int[] tickingOrder = new int[this.stationsAge.size()];
		{
		int i=0;
		for (int stationIdx: this.stationsAge) {
			tickingOrder[i++] = stationIdx;
		}
		}
		for (int i=0; i<tickingOrder.length; i++) {
			this.stations[tickingOrder[i]].tickReservationStation();
		}
		while (!this.pendingCdbTrans.isEmpty()) {
			this.cdb.notifyObservers(this.pendingCdbTrans.pop());
		}
		
	}
	
	private static enum EntryState {
		IDLE, WAITING, READY, EXECUTING;
	}
	
	private class Station implements Observer {
		
		private final int delay;
		private final CdbId cdbId;
		private final CDB cdb;
		
		private final int stationIdx;
		
		EntryState state = EntryState.IDLE;
		InstructionR inst = null;
		Float Vj = null;
		Float Vk = null;
		CdbId Qj = null;
		CdbId Qk = null;
		Integer time = null;
		
		public Station(int idx, CdbId.Type cdbType, int delay, CDB cdb) {
			this.stationIdx = idx;
			this.cdb = cdb;
			this.cdb.addObserver(this);
			this.delay = delay;
			this.cdbId = new CdbId(cdbType, idx);
		}
		
		private void reset() {
			this.state = EntryState.IDLE;
			this.inst = null;
			this.Vj = null;
			this.Vk = null;
			this.Qj = null;
			this.Qk = null;
			this.time = null;
		}

		public void set(InstructionR inst) {
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
		
		public void tickPrepCdbTrans() {
			if (this.state == EntryState.EXECUTING && this.time==1) {
				numFunctionalUnits++;
				pendingCdbTrans.add(new CdbTrans(this.cdbId, inst.calc(this.Vj, this.Vk)));
				instructionStatus[inst.getThreadIdx()].setWriteResult(inst);
				this.reset();
				stationsAge.remove(stationsAge.indexOf(this.stationIdx));
			}
		}

		public void tickReservationStation() {
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
					this.time = this.delay;
					instructionStatus[this.inst.getThreadIdx()].setExecComp(this.inst);
					this.state = EntryState.EXECUTING;
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
			if (this.Vj!=null && this.Vk!=null) {
				this.state = EntryState.READY;
			}
		}

		@Override
		public String toString() {
			return "Station [stationIdx=" + stationIdx + ", state=" + state + ", inst=" + inst + ", Vj=" + Vj
					+ ", Vk=" + Vk + ", Qj=" + Qj + ", Qk=" + Qk + ", time="
					+ time + "]";
		}
		
	}

}
