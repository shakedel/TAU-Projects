package state;

import data.instruction.Instruction;

public interface AcceptsInstructions extends Tickable {
	boolean acceptInstruction(Instruction instruction);
	boolean isEmpty();
}
