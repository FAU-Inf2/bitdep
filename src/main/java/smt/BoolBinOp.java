package smt;



public enum BoolBinOp {
	EQUALS   ("="),
	DISTINCT ("distinct"),
	IMPLIES  ("=>"),
	AND      ("and"),
	OR       ("or");



	private final String funName;



	BoolBinOp(final String funName) {
		this.funName = funName;
	}



	@Override
	public String toString() {
		return this.funName;
	}
}

