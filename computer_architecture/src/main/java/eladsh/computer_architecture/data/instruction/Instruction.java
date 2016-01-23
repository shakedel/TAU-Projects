package eladsh.computer_architecture.data.instruction;

import eladsh.computer_architecture.data.opcode.Opcode;

public interface Instruction {
	Opcode getOpcode();
	Integer getDst();
	Integer getSrc0();
	Integer getSrc1();
	Integer getImm();
	String toString();
	String toHex();
	int getThreadIdx();
	int getInstructionIdx();
}
