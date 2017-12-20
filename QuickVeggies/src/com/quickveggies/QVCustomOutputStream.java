package com.quickveggies;

import java.io.OutputStream;
import java.io.PrintStream;

public class QVCustomOutputStream extends PrintStream {
	
	public QVCustomOutputStream(OutputStream out) {
		super(out);
	}
}
