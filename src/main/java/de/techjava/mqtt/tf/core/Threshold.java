package de.techjava.mqtt.tf.core;

public class Threshold {

	private static final Threshold UNDEF = new Threshold((char) 0, -1, -1);
	final char operation;
	final int min;
	final int max;

	public char getOperation() {
		return operation;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	Threshold(char operation, int min, int max) {
		this.operation = operation;
		this.min = min;
		this.max = max;
	}

	public enum Operation {
		OFF("x"), LESS("<"), GREATER(">");

		private String operation;

		Operation(String operation) {
			this.operation = operation;
		}

		public static char parse(String string) {
			for (Operation op : values()) {
				if (op.operation.equals(string)) {
					return op.operation.charAt(0);
				}
			}
			return OFF.operation.charAt(0);
		}
	}

	public static Threshold parse(String property) {
		if (property == null) {
			return Threshold.UNDEF;
		}
		final String[] split = property.split(";");
		if (split == null || split.length != 3) {
			return Threshold.UNDEF;
		}
		return new Threshold(Operation.parse(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
	}

	public boolean isValid() {
		return Threshold.UNDEF != this;
	}
}
