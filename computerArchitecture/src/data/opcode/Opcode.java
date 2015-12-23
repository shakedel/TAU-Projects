package data.opcode;

import java.util.Map;
import java.util.TreeMap;

public enum Opcode {
	NOP(OpcodeType.EMPTY, 0),
	LD(OpcodeType.LD, 1),
	ST(OpcodeType.ST, 2),
	ADDS(OpcodeType.R, 3),
	SUBS(OpcodeType.R, 4),
	MULTS(OpcodeType.R, 5),
	DIVS(OpcodeType.R, 6),
	HALT(OpcodeType.EMPTY, 7);
	
	
	private static Map<Integer, Opcode> map = null;
	
	public static Opcode parseOpcode(int code) {
		if (map == null) {
			map = new TreeMap<Integer, Opcode>();
			for (Opcode opcode: Opcode.values()) {
				map.put(opcode.code, opcode);
			}
		}
		Opcode res = map.get(code);
		if (res == null) {
			throw new IllegalArgumentException("no matching opcode for code: "+code);
		}
		return res;
	}
	
	private OpcodeType type;
	private int code;
	
	private Opcode(OpcodeType type, int code) {
		this.type = type;
		if (code > 1<<4 || code < 0) {
			throw new IllegalArgumentException("code must be an unsigned 4bit value");
		}
		this.code = code;
	}
	
	public OpcodeType getOpcodeType() {
		return this.type;
	}
}
