package eladsh.computer_architecture.modules.impl.buffers;

import eladsh.computer_architecture.data.instruction.Instruction;
import eladsh.computer_architecture.peripherals.cdb.CDB;

/**
 * Describes the state of a {@link Buffer}
 */
public enum BufferState {
	
	/**
	 * empty, can accept {@link Instruction}
	 */
	IDLE, 
	
	/**
	 * waiting for a value on {@link CDB}
	 */
	WAITING,
	
	/**
	 * ready to execute, waiting for a functional unit to become available
	 */
	READY, 
	
	/**
	 * executing occupying {@link Instruction}
	 */
	EXECUTING;
}