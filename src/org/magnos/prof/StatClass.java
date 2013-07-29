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