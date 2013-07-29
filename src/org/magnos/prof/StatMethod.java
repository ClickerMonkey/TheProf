package org.magnos.prof;

public final class StatMethod
{
	
	public long durationTotal;
	public long minTotal = Long.MAX_VALUE;
	public long maxTotal = Long.MIN_VALUE;
	public long durationMethod;
	public long minMethod = Long.MAX_VALUE;
	public long maxMethod = Long.MIN_VALUE;
	public long times;

	public final String name;
	public final StatClass clazz;
	
	public StatMethod( StatClass statClazz, String methodName )
	{
		clazz = statClazz;
		name = methodName;
		clazz.methods.add( this );
	}

	public synchronized void accum( Trace trace )
	{
		final long total = trace.durationTotal;
		final long method = trace.durationMethod;

		durationTotal += total;
		minTotal = Math.min( minTotal, total );
		maxTotal = Math.max( maxTotal, total );
		
		durationMethod += method;
		minMethod = Math.min( minMethod, method );
		maxMethod = Math.max( maxMethod, method );
		
		times++;
	}
	
}