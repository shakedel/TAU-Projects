package eladsh.computer_architecture.data.register;

import eladsh.computer_architecture.peripherals.cdb.CdbId;

/**
 * data structure simulating a 32-bit register in the register status 
 */
public class Register {
	private final int idx;
	private RegisterState state;
	private Float val = null;
	private CdbId cdbId = null;
	
	/**
	 * @param idx index of register
	 * @param val initial value
	 */
	public Register(int idx, Float val) {
		state = RegisterState.VAL;
		this.idx = idx;
		this.val = val;
	}
	
	/**
	 * @param val float value to set 
	 */
	public void set(float val) {
		this.state = RegisterState.VAL;
		this.val = val;
	}
	
	/**
	 * @param cdbId CDB identifier value to set
	 */
	public void set(CdbId cdbId) {
		this.state = RegisterState.CDB_ID;
		this.cdbId = cdbId;
	}
	
	/**
	 * @return the current {@link RegisterState}
	 */
	public RegisterState getState() {
		return this.state;
	}
	
	/**
	 * @return float value of this {@link Register}
	 */
	public float getVal() {
		return this.val;
	}
	
	/**
	 * @return CDB identifier value of this {@link Register}
	 */
	public CdbId getCdbId() {
		return this.cdbId;
	}
	
	/**
	 * @return this {@link Register} index
	 */
	public int getIdx() {
		return this.idx;
	}
	
}