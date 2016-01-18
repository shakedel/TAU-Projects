package peripherals;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import data.instruction.Instruction;

public class InstructionStatus {
	
	private final Clock clock;

	public InstructionStatus(Clock clock) {
		this.clock = clock;
	}
	
	IdentityHashMap<Instruction, Entry> table = new IdentityHashMap<Instruction, InstructionStatus.Entry>();
	List<Instruction> instructionsAge = new LinkedList<Instruction>();
	
	int instructionCounter = 0;
	int lastCycle = 0;
	
	private class Entry {
		private final Instruction instruction;
		private final int issue;
		private boolean execCompSet;
		private int execComp = -1;
		private boolean writeResultSet;
		private int writeResult = -1;
		
		public Entry(Instruction instruction, int issueCycle) {
			this.instruction = instruction;
			this.issue = issueCycle;
			if (issueCycle > lastCycle) {
				lastCycle = issueCycle;
			}
			reportEvent(issueCycle, "issue", instruction);
		}
		public void setExecComp(int cycle) {
			if (this.execCompSet) {
				throw new IllegalStateException("execComp already set in cycle: "+this.execComp);
			}
			this.execComp = cycle;
			if (cycle > lastCycle) {
				lastCycle = cycle;
			}
			reportEvent(cycle, "exec", instruction);
		}
		public void setWriteResult(int cycle) {
			if (this.writeResultSet) {
				throw new IllegalStateException("writeResult already set in cycle: "+this.execComp);
			}
			this.writeResult = cycle;
			if (cycle > lastCycle) {
				lastCycle = cycle;
			}
			reportEvent(cycle, "cdb", instruction);
		}
		
		
		
		
	}
	
	private static void reportEvent(int cycle, String eventName, Instruction inst) {
		String str = String.format("cycle: %3d, %s, %s", cycle, padRight(eventName, 5), inst.toString());
		System.out.println(str);
	}
	
	private static String padRight(String s, int n) {
	    return String.format("%1$-" + n + "s", s);
	  }
	
	public void add(Instruction instruction) {
		this.instructionCounter++;
		Entry entry = new Entry(instruction, this.clock.get());
		if (this.table.put(instruction, entry) != null) {
			throw new IllegalStateException();
		}
		this.instructionsAge.add(instruction);
	}
	
	public void setExecComp(Instruction inst) {
		this.table.get(inst).setExecComp(this.clock.get());
	}
	
	public void setWriteResult(Instruction inst) {
		this.table.get(inst).setWriteResult(this.clock.get());
	}

	public void store(File traceFile, File cpiFile) throws IOException {
		traceFile.getParentFile().mkdirs();
		try (PrintStream ps = new PrintStream(traceFile)) {
			for (Iterator<Instruction> it = this.instructionsAge.iterator(); it.hasNext();) {
				Instruction currInst = it.next();
				Entry entry = this.table.get(currInst);
				ps.print(currInst.toHex()+" "+entry.issue+" "+entry.execComp+" "+entry.writeResult);
			    if (it.hasNext()) {
			        ps.println();
			    }
			}
		}
		
		cpiFile.getParentFile().mkdirs();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(cpiFile))) {
			float cpi = ((float) this.lastCycle+1.0f)/this.instructionCounter;
			bw.write(Float.toString(cpi));
		}
	}

}
