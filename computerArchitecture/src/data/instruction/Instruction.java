package data.instruction;

import data.opcode.Opcode;

public interface Instruction {
	Opcode getOpcode();
	Integer getDst();
	Integer getSrc0();
	Integer getSrc1();
	Integer getImm();
	String toString();
}
