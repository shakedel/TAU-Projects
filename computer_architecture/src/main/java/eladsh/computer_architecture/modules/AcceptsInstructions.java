package eladsh.computer_architecture.modules;

import eladsh.computer_architecture.data.instruction.Instruction;

/**
 * An interface for modules that can accept instructions
 */
public interface AcceptsInstructions extends Tickable {
	/**
	 * @param instruction {@link Instruction} to accept
	 * @return <code>true</code> if instruction was accepted
	 */
	boolean acceptInstruction(Instruction instruction);
	
	/**
	 * @return <code>true</code> if this module has no pending instructions
	 */
	boolean isEmpty();
}
