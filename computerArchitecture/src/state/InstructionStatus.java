package state;

import java.util.IdentityHashMap;

import data.instruction.Instruction;

public class InstructionStatus {
	IdentityHashMap<Instruction, Entry> table = new IdentityHashMap<Instruction, InstructionStatus.Entry>();
	
	private static class Entry {
		private final Instruction instruction;
		private final int issue;
		private boolean execCompSet;
		private int execComp;
		private boolean writeResultSet;
		private int writeResult;
		
		public Entry(Instruction instruction, int issueCycle) {
			this.instruction = instruction;
			this.issue = issueCycle;
			System.out.println(issueCycle+": issued "+instruction);
		}
		public void setExecComp(int cycle) {
			if (this.execCompSet) {
				throw new IllegalStateException("execComp already set in cycle: "+this.execComp);
			}
			this.execComp = cycle;
			System.out.println(cycle+": execComp "+instruction);
		}
		public void setWriteResult(int cycle) {
			if (this.writeResultSet) {
				throw new IllegalStateException("writeResult already set in cycle: "+this.execComp);
			}
			this.writeResult = cycle;
			System.out.println(cycle+": writeResult "+instruction);
		}
		
		
	}
	
	public void add(Instruction instruction) {
		Entry entry = new Entry(instruction, Clock.get());
		if (this.table.put(instruction, entry) != null) {
			throw new IllegalStateException();
		}
	}
	
	public void setExecComp(Instruction inst) {
		this.table.get(inst).setExecComp(Clock.get());
	}
	
	public void setWriteResult(Instruction inst) {
		this.table.get(inst).setWriteResult(Clock.get());
	}


}
