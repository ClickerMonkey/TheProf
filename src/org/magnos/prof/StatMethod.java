/* 
 * NOTICE OF LICENSE
 * 
 * This source file is subject to the Open Software License (OSL 3.0) that is 
 * bundled with this package in the file LICENSE.txt. It is also available 
 * through the world-wide-web at http://opensource.org/licenses/osl-3.0.php
 * If you did not receive a copy of the license and are unable to obtain it 
 * through the world-wide-web, please send an email to magnos.software@gmail.com 
 * so we can send you a copy immediately. If you use any of this software please
 * notify me via our website or email, your feedback is much appreciated. 
 * 
 * @copyright   Copyright (c) 2011 Magnos Software (http://www.magnos.org)
 * @license     http://opensource.org/licenses/osl-3.0.php
 *              Open Software License (OSL 3.0)
 */

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