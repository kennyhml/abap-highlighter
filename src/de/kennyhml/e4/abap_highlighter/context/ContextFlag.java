package de.kennyhml.e4.abap_highlighter.context;

public enum ContextFlag {
	NONE(0), 
	STRUCT_DECL(2), // begin of # [..context..] end of #.
	FN_DECL(3), // methods [context].
	CLS_DECL(4), // methods [context].
	FN_MULTI_DECL(5), // methods: [context].
	FMT_STRING(6), // | [context] |.
	DATA_DECL(7), // data [context].
	DATA_MULTI_DECL(8); // data: [context]
	
	public final int flag;

	ContextFlag(int id) {
		this.flag = 1 << id;
	}
}
