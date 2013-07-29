package org.magnos.prof;


public class Profiler
{

	private static final ThreadLocal<Profiler> local = new ThreadLocal<Profiler>()
	{
		protected Profiler initialValue()
		{
			return new Profiler();
		}
	};
	
	public static void start(StatMethod stat)
	{
		local.get().push( stat );
	}
	
	public static void stop()
	{
		local.get().pop();
	}
	
	public static final int DEFAULT_MAX_STACK_DEPTH = 1024;
	
	private Trace[] trace = new Trace[DEFAULT_MAX_STACK_DEPTH];
	private int depth = 0;

	public Profiler()
	{
		for (int i = 0; i < trace.length; i++)
		{
			trace[i] = new Trace();
		}
	}
	
	public void push( StatMethod stat )
	{
		trace[depth].pause();
		if (++depth == trace.length) {
			
		}
		trace[depth].start( stat );
	}

	public void pop()
	{
		trace[depth].stop();
		trace[--depth].restart();
	}
	
//	long avgTotal = s.durationTotal / s.times;
//	long avgMethod = s.durationMethod / s.times;
//	
//	System.out.format( "Stat '%30s' tracked %6d times with total time %.9fs � %.9fs [%.9fs,%.9fs] and method time %.9fs � %.9fs [%.9fs,%.9fs].\n", 
//		s.name, 
//		s.times, 
//		avgTotal * 0.000000001, 
//		((avgTotal - s.minTotal) + (s.maxTotal - avgTotal)) * 0.5 * 0.000000001, 
//		s.minTotal * 0.000000001, 
//		s.maxTotal * 0.000000001, 
//		avgMethod * 0.000000001,
//		((avgMethod - s.minMethod) + (s.maxMethod - avgMethod)) * 0.5 * 0.000000001,
//		s.minMethod * 0.000000001, 
//		s.maxMethod * 0.000000001
//	);

}
