/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
/*
 * This class was taken from the StackOverflow answer by StackOverflow
 * user "Holger", version from 2018-02-16, retrieved on 2020-05-17:
 * URL: https://stackoverflow.com/revisions/48806265/4
 * 
 * Small modifications (autoformatting, bugfix, getter) were made.
 */
package de.uni_stuttgart.iaas.saes.code_analysis.plugin.java;

import static org.objectweb.asm.Opcodes.*;
import java.util.List;
import java.util.Objects;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

public class ConstantTracker extends Interpreter<ConstantTracker.ConstantValue> {
	public static final ConstantValue NULL = new ConstantValue(BasicValue.REFERENCE_VALUE, null);

	public static final class ConstantValue implements Value {
		private final Object value; // null if unknown or NULL
		private final BasicValue type;

		ConstantValue(BasicValue type, Object value) {
			this.value = value;
			this.type = Objects.requireNonNull(type);
		}

		@Override
		public int getSize() {
			return getType().getSize();
		}

		@Override
		public String toString() {
			Type t = getType().getType();
			if (t == null)
				return "uninitialized";
			String typeName = getType() == BasicValue.REFERENCE_VALUE ? "a reference type" : t.getClassName();
			return this == NULL ? "null"
					: getValue() == null ? "unknown value of " + typeName : getValue() + " (" + typeName + ")";
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (this == NULL || obj == NULL || !(obj instanceof ConstantValue))
				return false;
			ConstantValue that = (ConstantValue) obj;
			return Objects.equals(this.getValue(), that.getValue()) && Objects.equals(this.getType(), that.getType());
		}

		@Override
		public int hashCode() {
			if (this == NULL)
				return ~0;
			return (getValue() == null ? 7 : getValue().hashCode()) + getType().hashCode() * 31;
		}

		public Object getValue() {
			return value;
		}

		public BasicValue getType() {
			return type;
		}
	}

	BasicInterpreter basic = new BasicInterpreter(ASM5) {
		@Override
		public BasicValue newValue(Type type) {
			return type != null && (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY)
					? new BasicValue(type)
					: super.newValue(type);
		}

		@Override
		public BasicValue merge(BasicValue a, BasicValue b) {
			if (a.equals(b))
				return a;
			if (a.isReference() && b.isReference())
				// this is the place to consider the actual type hierarchy if you want
				return BasicValue.REFERENCE_VALUE;
			return BasicValue.UNINITIALIZED_VALUE;
		}
	};

	public ConstantTracker() {
		super(ASM5);
	}

	@Override
	public ConstantValue newOperation(AbstractInsnNode insn) throws AnalyzerException {
		switch (insn.getOpcode()) {
		case ACONST_NULL:
			return NULL;
		case ICONST_M1:
		case ICONST_0:
		case ICONST_1:
		case ICONST_2:
		case ICONST_3:
		case ICONST_4:
		case ICONST_5:
			return new ConstantValue(BasicValue.INT_VALUE, insn.getOpcode() - ICONST_0);
		case LCONST_0:
		case LCONST_1:
			return new ConstantValue(BasicValue.LONG_VALUE, (long) (insn.getOpcode() - LCONST_0));
		case FCONST_0:
		case FCONST_1:
		case FCONST_2:
			return new ConstantValue(BasicValue.FLOAT_VALUE, (float) (insn.getOpcode() - FCONST_0));
		case DCONST_0:
		case DCONST_1:
			return new ConstantValue(BasicValue.DOUBLE_VALUE, (double) (insn.getOpcode() - DCONST_0));
		case BIPUSH:
		case SIPUSH:
			return new ConstantValue(BasicValue.INT_VALUE, ((IntInsnNode) insn).operand);
		case LDC:
			return new ConstantValue(basic.newOperation(insn), ((LdcInsnNode) insn).cst);
		default:
			BasicValue v = basic.newOperation(insn);
			return v == null ? null : new ConstantValue(v, null);
		}
	}

	@Override
	public ConstantValue copyOperation(AbstractInsnNode insn, ConstantValue value) {
		return value;
	}

	@Override
	public ConstantValue newValue(Type type) {
		BasicValue v = basic.newValue(type);
		return v == null ? null : new ConstantValue(v, null);
	}

	@Override
	public ConstantValue unaryOperation(AbstractInsnNode insn, ConstantValue value) throws AnalyzerException {
		BasicValue v = basic.unaryOperation(insn, value.getType());
		return v == null ? null : new ConstantValue(v, insn.getOpcode() == CHECKCAST ? value.getValue() : null);
	}

	@Override
	public ConstantValue binaryOperation(AbstractInsnNode insn, ConstantValue a, ConstantValue b)
			throws AnalyzerException {
		BasicValue v = basic.binaryOperation(insn, a.getType(), b.getType());
		return v == null ? null : new ConstantValue(v, null);
	}

	@Override
	public ConstantValue ternaryOperation(AbstractInsnNode insn, ConstantValue a, ConstantValue b, ConstantValue c) {
		return null;
	}

	@Override
	public ConstantValue naryOperation(AbstractInsnNode insn, List<? extends ConstantValue> values)
			throws AnalyzerException {
		List<BasicValue> unusedByBasicInterpreter = null;
		BasicValue v = basic.naryOperation(insn, unusedByBasicInterpreter);
		return v == null ? null : new ConstantValue(v, null);
	}

	@Override
	public void returnOperation(AbstractInsnNode insn, ConstantValue value, ConstantValue expected) {
	}

	@Override
	public ConstantValue merge(ConstantValue a, ConstantValue b) {
		if (a == b)
			return a;
		BasicValue t = basic.merge(a.getType(), b.getType());
		return t.equals(a.getType()) && (a.getValue() == null && a != NULL || Objects.equals(a.getValue(), b.getValue())) ? a
				: t.equals(b.getType()) && b.getValue() == null && b != NULL ? b : new ConstantValue(t, null);
	}
}