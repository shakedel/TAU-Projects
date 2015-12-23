package state;

import data.instruction.Instruction;

public interface AcceptsInstructions {
	boolean acceptInstruction(Instruction instruction);
	boolean isEmpty();
}
