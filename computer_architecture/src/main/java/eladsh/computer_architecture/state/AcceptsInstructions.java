package eladsh.computer_architecture.state;

import eladsh.computer_architecture.data.instruction.Instruction;

public interface AcceptsInstructions extends Tickable {
	boolean acceptInstruction(Instruction instruction);
	boolean isEmpty();
}
