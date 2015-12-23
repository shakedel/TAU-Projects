package data.instruction;

import misc.Const;
import data.opcode.Opcode;
import data.opcode.OpcodeType;

public class InstructionImpl implements Instruction {

	public static Instruction parseInstruction(long val) {
		if (val < 0 ||val >= Const.MAX_WORD) {
			throw new IllegalArgumentException("instruction value must be a 32bit value, but got: "+val);
		}
		String hexStr = Long.toHexString(val);
		
		Opcode opcode = Opcode.parseOpcode(Integer.parseUnsignedInt(hexStr.substring(0, 1), 16));
		int dst = Integer.parseUnsignedInt(hexStr.substring(1, 2), 16);
		int src0 = Integer.parseUnsignedInt(hexStr.substring(2, 3), 16);
		int src1 = Integer.parseUnsignedInt(hexStr.substring(3, 4), 16);
		int imm = Integer.parseUnsignedInt(hexStr.substring(4, 8), 16);
		
		switch (opcode.getOpcodeType()) {
		case EMPTY:
			return new InstructionEmpty(opcode);
		case R:
			return new InstructionR(opcode, dst, src0, src1);
		case LD:
			return new InstructionLD(opcode, dst, imm);
		case ST:
			return new InstructionST(opcode, src1, imm);
		default:
			throw new IllegalArgumentException("unknown opcode: "+opcode);
		}
	}
	
	Opcode opcode;
	private Integer dst;
	private Integer src0;
	private Integer src1;
	private Integer imm;
	
	public InstructionImpl(Opcode opcode, Integer dst, Integer src0, Integer src1, Integer imm) {
		if (dst!=null && (dst >= Const.MAX_NIBBLE || dst < 0)) {
			throw new IllegalArgumentException("must be a 4bit number, but got: "+dst);
		}
		if (src0!=null && (src0 >= Const.MAX_NIBBLE || src0 < 0)) {
			throw new IllegalArgumentException("must be a 4bit number, but got: "+src0);
		}
		if (src1!=null && (src1 >= Const.MAX_NIBBLE || src1 < 0)) {
			throw new IllegalArgumentException("must be a 4bit number, but got: "+src1);
		}
		if (imm!=null && (imm >= Const.MAX_HALF_WORD || imm < 0)) {
			throw new IllegalArgumentException("must be a 16bit number, but got: "+imm);
		}
		
		this.opcode = opcode;
		this.dst = dst;
		this.src0 = src0;
		this.src1 = src1;
		this.imm = imm;
	}
	
	@Override
	public Opcode getOpcode() {
		return this.opcode;
	}

	@Override
	public Integer getDst() {
		return this.dst;
	}

	@Override
	public Integer getSrc0() {
		return this.src0;
	}

	@Override
	public Integer getSrc1() {
		return this.src1;
	}

	@Override
	public Integer getImm() {
		return this.imm;
	}
	
	@Override
	public String toString() {
		return this.opcode.name();
	}

	public static class InstructionR extends InstructionImpl {

		public InstructionR(Opcode opcode, Integer dst, Integer src0, Integer src1) {
			super(opcode, dst, src0, src1, null);
			if (opcode.getOpcodeType() != OpcodeType.R) {
				throw new IllegalArgumentException("must be called with an R type opcode, but was called with: "+opcode);
			}
		}
		
		@Override
		public Integer getImm() {
			throw new IllegalStateException("R type instructions dont have imm values");
		}
		
		@Override
		public String toString() {
			return " F"+this.getDst()+" = F"+this.getSrc0()+" + F"+this.getSrc1();
		};
		
		public float calc(float arg0, float arg1) {
			switch (this.opcode) {
			case ADDS:
				return arg0+arg1;
			case SUBS:
				return arg0-arg1;
			case MULTS:
				return arg0*arg1;
			case DIVS:
				return arg0/arg1;
			default:
				throw new IllegalArgumentException("unknown opcode: "+this.opcode);
			}
		}
		

	}
	
	public static class InstructionLD extends InstructionImpl {

		public InstructionLD(Opcode opcode, Integer dst, Integer imm) {
			super(opcode, dst, null, null, imm);
			if (opcode.getOpcodeType() != OpcodeType.LD) {
				throw new IllegalArgumentException("must be called with an LD type opcode, but was called with: "+opcode);
			}
		}
		
		@Override
		public Integer getSrc0() {
			 throw new IllegalStateException("LD type instructions dont have src0 values");
		}
		
		@Override
		public Integer getSrc1() {
			throw new IllegalStateException("LD type instructions dont have src1 values");
		}
		
		@Override
		public String toString() {
			return "F"+this.getDst()+ " = MEM["+this.getImm()+"]";
		}
	
	}
	
	public static class InstructionST extends InstructionImpl {

		public InstructionST(Opcode opcode, int src1, int imm) {
			super(opcode, null, null, src1, imm);
			if (opcode.getOpcodeType() != OpcodeType.ST) {
				throw new IllegalArgumentException("must be called with an ST type opcode, but was called with: "+opcode);
			}
		}
		
		@Override
		public Integer getSrc0() {
			 throw new IllegalStateException("LD type instructions dont have src0 values");
		}
		
		@Override
		public Integer getDst() {
			throw new IllegalStateException("LD type instructions dont have dst values");
		}
		
		@Override
		public String toString() {
			return "MEM["+this.getImm()+"] = F"+this.getSrc1();
		}

	}
	
	public static class InstructionEmpty extends InstructionImpl {
		
		public InstructionEmpty(Opcode opcode) {
			super(opcode, null, null, null, null);
			if (opcode.getOpcodeType() != OpcodeType.ST) {
				throw new IllegalArgumentException("must be called with an ST type opcode, but was called with: "+opcode);
			}
		}
		
		@Override
		public Integer getSrc0() {
			throw new IllegalStateException("EMPTY type instructions dont have src0 values");
		}
		
		@Override
		public Integer getSrc1() {
			throw new IllegalStateException("EMPTY type instructions dont have src1 values");
		}
		
		@Override
		public Integer getDst() {
			throw new IllegalStateException("EMPTY type instructions dont have dst values");
		}
		
		@Override
		public Integer getImm() {
			throw new IllegalStateException("EMPTY type instructions dont have imm values");
		}
		
		
	}
	
	
}
