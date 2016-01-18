package peripherals;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import peripherals.cdb.CDB;
import peripherals.cdb.CdbId;
import peripherals.cdb.CdbTrans;



public class Registers implements Observer {
	
	private static Register[] initRegisters() {
		Register[] res = new Register[16];
		for (int i=0; i<res.length; i++) {
			res[i] = new Register(new Float(i));
		}
		return res;
	}
	
	
	private Register[] regs = initRegisters();
	
	public Registers(CDB cdb) {
		cdb.addObserver(this);
	}
	
	public Register get(int idx) {
		return this.regs[idx];
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		CdbTrans cdbTrans = (CdbTrans) arg1;
		for (Register reg: this.regs) {
			if (reg.state==Registers.State.CDB_ID && reg.getCdbId().equals(cdbTrans.getCdbId())) {
				reg.set(cdbTrans.getValue());
			}
		}
	}
	
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
	
	
	public static class Register {
		private Registers.State state;
		private Float val = null;
		private CdbId cdbId = null;
		
		private Register(Float val) {
			state = Registers.State.VAL;
			this.val = val;
		}
		
		public void set(float val) {
			this.state = Registers.State.VAL;
			this.val = val;
		}
		
		public void set(CdbId cdbId) {
			this.state = Registers.State.CDB_ID;
			this.cdbId = cdbId;
		}
		
		public Registers.State getState() {
			return this.state;
		}
		
		public float getVal() {
			return this.val;
		}
		
		public CdbId getCdbId() {
			return this.cdbId;
		}
		
	}


	public static enum State {
		VAL, CDB_ID
	}
	
}
