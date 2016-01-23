package eladsh.computer_architecture.modules.impl.buffers;

import eladsh.computer_architecture.modules.impl.MemoryUnit;
import eladsh.computer_architecture.peripherals.cdb.CDB;
import eladsh.computer_architecture.peripherals.cdb.CdbId.Type;

/**
 * Buffer to be used by the memory functional unit (store/load)
 */
abstract class MemoryBuffer extends Buffer {

	private final MemoryUnit memoryUnit;
	private final MemoryBufferType type;
	
	public MemoryBuffer(MemoryUnit memoryUnit, MemoryBufferType type, int idx, Type cdbType, int delay, CDB cdb) {
		super(idx, cdbType, delay, cdb);
		this.memoryUnit = memoryUnit;
		this.type = type;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof MemoryBuffer)) {
			return false;
		}
		MemoryBuffer other = (MemoryBuffer) obj;
		if (!getOuterType().equals(other.getOuterType())) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		return true;
	}

	private MemoryUnit getOuterType() {
		return this.memoryUnit;
	}

	
}