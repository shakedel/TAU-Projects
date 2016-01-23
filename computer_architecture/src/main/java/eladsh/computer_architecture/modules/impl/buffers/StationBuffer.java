package eladsh.computer_architecture.modules.impl.buffers;

import eladsh.computer_architecture.data.instruction.Instruction;
import eladsh.computer_architecture.data.instruction.InstructionImpl.InstructionR;
import eladsh.computer_architecture.data.register.Register;
import eladsh.computer_architecture.modules.impl.ReservationStation;
import eladsh.computer_architecture.peripherals.RegistersStatus;
import eladsh.computer_architecture.peripherals.cdb.CDB;
import eladsh.computer_architecture.peripherals.cdb.CdbId;
import eladsh.computer_architecture.peripherals.cdb.CdbTrans;

/**
 * Buffer to be used in reservation stations
 */
public class StationBuffer extends Buffer {
	
	private final ReservationStation reservationStation;
	Float Vj = null;
	Float Vk = null;
	CdbId Qj = null;
	CdbId Qk = null;
	
	public StationBuffer(ReservationStation reservationStation, int idx, CdbId.Type cdbType, int delay, CDB cdb, RegistersStatus[] regs) {
		super(idx, cdbType, delay, cdb);
		this.reservationStation = reservationStation;
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
		Register regJ = this.reservationStation.regs[inst.getThreadIdx()].getReg(inst.getSrc0());
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
		
		Register regK = this.reservationStation.regs[inst.getThreadIdx()].getReg(inst.getSrc1());
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
		
		this.reservationStation.regs[inst.getThreadIdx()].getReg(inst.getDst()).set(this.cdbId);
		
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
			this.reservationStation.incrementNumFunctionalUnits();
			return new CdbTrans(this.cdbId, ((InstructionR) inst).calc(this.Vj, this.Vk), inst);
		}
		return null;
	}

	@Override
	public void preTick() {
		// do nothing
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
			if (this.reservationStation.decrementNumFunctionalUnits()) {
				this.time = this.delay;
				this.reservationStation.instructionStatus[this.inst.getThreadIdx()].setExecComp(this.inst);
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
	public void postTick() {
		// do nothing
	}

	@Override
	public String toString() {
		return "Station [stationIdx=" + idx + ", state=" + state + ", inst=" + inst + ", Vj=" + Vj
				+ ", Vk=" + Vk + ", Qj=" + Qj + ", Qk=" + Qk + ", time="
				+ time + "]";
	}

}