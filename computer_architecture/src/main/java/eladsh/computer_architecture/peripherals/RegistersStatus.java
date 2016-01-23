package eladsh.computer_architecture.peripherals;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import eladsh.computer_architecture.data.register.Register;
import eladsh.computer_architecture.data.register.RegisterState;
import eladsh.computer_architecture.peripherals.cdb.CDB;
import eladsh.computer_architecture.peripherals.cdb.CdbTrans;



/**
 * Simulates a register file for a thread
 * Gets updated from {@link CDB}
 */
public class RegistersStatus implements Observer {
	
	private static Register[] initRegisters() {
		Register[] res = new Register[16];
		for (int i=0; i<res.length; i++) {
			res[i] = new Register(i, new Float(i));
		}
		return res;
	}
	
	private final int threadIdx;
	private Register[] regs = initRegisters();
	
	public RegistersStatus(int threadIdx, CDB cdb) {
		this.threadIdx = threadIdx;
		cdb.addObserver(this);
	}
	
	/**
	 * @param idx index of register
	 * @return a {@link Register} with the requested index
	 */
	public Register getReg(int idx) {
		return this.regs[idx];
	}
	
	/**
	 * @return the thread index this {@link RegistersStatus} belong to
	 */
	public int getThreadIdx() {
		return this.threadIdx;
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		CdbTrans cdbTrans = (CdbTrans) arg1;
		for (Register reg: this.regs) {
			if (reg.getState()==RegisterState.CDB_ID && reg.getCdbId().equals(cdbTrans.getCdbId())) {
				reg.set(cdbTrans.getValue());
			}
		}
	}
	
	/**
	 * Store this object on disk
	 * @param f {@link File} to store to
	 * @throws IOException
	 */
	public void store(File f) throws IOException {
		f.getParentFile().mkdirs();
		
		boolean isFirstLine = true;
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
			for (Register reg: this.regs) {
				if (isFirstLine) {
					isFirstLine = false;
				} else {
					bw.newLine();
				}
				float val = reg.getVal();
				bw.write(Float.toString(val));
			}
		}
	}
	
}
