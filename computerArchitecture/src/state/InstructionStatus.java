package state;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;

import data.instruction.Instruction;

public class InstructionStatus {
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
			System.out.println(issueCycle+": issued "+instruction);
		}
		public void setExecComp(int cycle) {
			if (this.execCompSet) {
				throw new IllegalStateException("execComp already set in cycle: "+this.execComp);
			}
			this.execComp = cycle;
			if (cycle > lastCycle) {
				lastCycle = cycle;
			}
			System.out.println(cycle+": execComp "+instruction);
		}
		public void setWriteResult(int cycle) {
			if (this.writeResultSet) {
				throw new IllegalStateException("writeResult already set in cycle: "+this.execComp);
			}
			this.writeResult = cycle;
			if (cycle > lastCycle) {
				lastCycle = cycle;
			}
			System.out.println(cycle+": writeResult "+instruction);
		}
		
		
	}
	
	public void add(Instruction instruction) {
		this.instructionCounter++;
		Entry entry = new Entry(instruction, Clock.get());
		if (this.table.put(instruction, entry) != null) {
			throw new IllegalStateException();
		}
		this.instructionsAge.add(instruction);
	}
	
	public void setExecComp(Instruction inst) {
		this.table.get(inst).setExecComp(Clock.get());
	}
	
	public void setWriteResult(Instruction inst) {
		this.table.get(inst).setWriteResult(Clock.get());
	}

	public void store(File traceFile, File cpiFile) throws IOException {
		traceFile.getParentFile().mkdirs();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(traceFile))) {
			for (Instruction inst: this.instructionsAge) {
				Entry entry = this.table.get(inst);
				bw.write(inst.toHex()+" "+entry.issue+" "+entry.execComp+" "+entry.writeResult);
				bw.newLine();
			}
		}
		
		cpiFile.getParentFile().mkdirs();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(cpiFile))) {
			float cpi = ((float) this.lastCycle)/this.instructionCounter;
			bw.write(Float.toString(cpi));
		}
	}

}
