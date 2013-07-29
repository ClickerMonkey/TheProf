package org.magnos.prof;

import java.io.PrintStream;

public class Logger 
{

	public static PrintStream out = System.out;
	
	public static synchronized void log(String message, Object ... args)
	{
		out.print("TheProf (LOG) [] ");
		out.format(message, args);
		out.println();
	}
	
	public static synchronized void error(String message, Exception e)
	{
		out.print("TheProf (ERROR) [] ");
		out.println(message);
		e.printStackTrace(out);
	}
	
}
