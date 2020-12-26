package edu.bjut.util;

/******************************************************************************
 *  Compilation:  javac Out.java
 *  Execution:    java Out
 *
 *  Writes data of various types to: stdout, file, or socket.
 *
 ******************************************************************************/

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Out {
	private PrintWriter out;

	// for stdout
	public Out(OutputStream os) {
		out = new PrintWriter(os, true);
	}

	public Out() {
		this(System.out);
	}

	// for Socket output
	public Out(Socket socket) {
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	// for file output
	public Out(String s) {
		try {
			out = new PrintWriter(new FileOutputStream(s), true);
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
			System.out.println("experiment running time : " + df.format(new Date()));// newDate()为获取当前系统时间
			out.println("experiment running time : " + df.format(new Date()));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void close() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
		System.out.println("experiment running time : " + df.format(new Date()));// newDate()为获取当前系统时间
		out.println("experiment running time : " + df.format(new Date()));
		out.close();
	}

	public void println() {
		out.println();
		out.flush();
	}

	public void println(Object x) {
		out.println(x);
		out.flush();
	}

	public void println(boolean x) {
		out.println(x);
		out.flush();
	}

	public void println(char x) {
		out.println(x);
		out.flush();
	}

	public void println(double x) {
		out.println(x);
	}

	public void println(float x) {
		out.println(x);
	}

	public void println(int x) {
		out.println(x);
	}

	public void println(long x) {
		out.println(x);
	}

	public void print() {
		out.flush();
	}

	public void print(Object x) {
		out.print(x);
		out.flush();
	}

	public void print(boolean x) {
		out.print(x);
		out.flush();
	}

	public void print(char x) {
		out.print(x);
		out.flush();
	}

	public void print(double x) {
		out.print(x);
		out.flush();
	}

	public void print(float x) {
		out.print(x);
		out.flush();
	}

	public void print(int x) {
		out.print(x);
		out.flush();
	}

	public void print(long x) {
		out.print(x);
		out.flush();
	}

	// This method is just here to test the class
	public static void main(String[] args) {
		Out out;

		// write to stdout
		out = new Out();
		out.println("Test 1");
		out.close();

		// write to a file
		out = new Out("test.txt");
		out.println("Test 2");
		out.close();
	}

}