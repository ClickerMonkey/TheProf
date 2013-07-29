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

import java.util.Arrays;

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
		
		if (++depth == trace.length) 
		{
		   int newLength = depth + (depth >> 1);
		   
		   trace = Arrays.copyOf( trace, newLength );
		   
		   for (int i = depth; i < newLength; i++) 
		   {
		      trace[i] = new Trace();
		   }
		}
		
		trace[depth].start( stat );
	}

	public void pop()
	{
		trace[depth].stop();
		trace[--depth].restart();
	}

}
