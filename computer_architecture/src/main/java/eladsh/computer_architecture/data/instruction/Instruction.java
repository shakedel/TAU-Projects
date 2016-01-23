package eladsh.computer_architecture.data.instruction;

import eladsh.computer_architecture.data.opcode.Opcode;

/**
 * interface for an instruction
 */
public interface Instruction {
	/**
	 * @return the {@link Opcode} of the {@link Instruction}
	 */
	Opcode getOpcode();
	
	/**
	 * @return the destination register index of the {@link Instruction}
	 */
	Integer getDst();
	
	/**
	 * @return the src0 source register index of the {@link Instruction}
	 */
	Integer getSrc0();
	
	/**
	 * @return the src1 source register index of the {@link Instruction}
	 */
	Integer getSrc1();
	
	/**
	 * @return the immediate value of the {@link Instruction}
	 */
	Integer getImm();
	
	/**
	 * @return a string with the {@link Instruction} info
	 */
	String toString();
	
	/**
	 * @return hex coding of this {@link Instruction}
	 */
	String toHex();
	
	/**
	 * @return the index of the thread this {@link Instruction} belongs to
	 */
	int getThreadIdx();
	
	/**
	 * @return the index of this {@link Instruction} in it's thread queue
	 */
	int getInstructionIdx();
}
