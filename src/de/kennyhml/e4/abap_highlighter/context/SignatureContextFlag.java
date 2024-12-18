package de.kennyhml.e4.abap_highlighter.context;

public enum SignatureContextFlag {
	FN_DECL_IMPORTING(0),
	FN_DECL_CHANGING(1),
	FN_DECL_EXPORTING(2),
	FN_DECL_RETURNING(3),
	FN_DECL_RAISING(4),
	FN_DECL_EXCEPTIONS(5);
	
	public final int flag;
	
	SignatureContextFlag(int id) {
		this.flag = 1 << id;
	}
}
