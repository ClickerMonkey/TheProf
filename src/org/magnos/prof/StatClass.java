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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class StatClass
{
	
	public static final List<StatClass> instances = Collections.synchronizedList( new ArrayList<StatClass>() );
	
	public final String name;
	public final List<StatMethod> methods;
	
	public StatClass(String className)
	{
		name = className;
		methods = new ArrayList<StatMethod>();
		instances.add( this );
	}
	
	public boolean hasStatistics()
	{
	   for (StatMethod m : methods)
	   {
	      if (m.times != 0)
	      {
	         return true;
	      }
	   }
	   
	   return false;
	}
	
}