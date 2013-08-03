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

   public long totalDuration;
   public long totalMin;
   public long totalMax;
   public long methodDuration;
   public long methodMin;
   public long methodMax;
   public long invocations;

   public final String name;
   public final StatClass clazz;
   public final int delay;

   public StatMethod( StatClass statClazz, String methodName, int initialDelay )
   {
      clazz = statClazz;
      name = methodName;
      delay = initialDelay;
      clazz.methods.add( this );
      reset();
   }

   public synchronized void accum( Trace trace )
   {
      if (invocations >= 0)
      {
         final long total = trace.durationTotal;
         final long method = trace.durationMethod;

         totalDuration += total;
         totalMin = Math.min( totalMin, total );
         totalMax = Math.max( totalMax, total );

         methodDuration += method;
         methodMin = Math.min( methodMin, method );
         methodMax = Math.max( methodMax, method );
      }

      invocations++;
   }

   public StatInstance getInstance()
   {
      final long totalDuration = this.totalDuration;
      final long methodDuration = this.methodDuration;
      final long invocations = this.invocations;

      StatInstance s = new StatInstance();

      s.method = this;
      
      if (invocations > 0) 
      {
         s.invocations = invocations;
         s.methodAverage = methodDuration / invocations;
         s.methodImportance = methodDuration + (s.methodAverage * delay);
         s.methodVariance = ((s.methodAverage - methodMin) + (methodMax - s.methodAverage)) / 2;
         s.methodMin = methodMin;
         s.methodMax = methodMax;
         s.totalAverage = totalDuration / invocations;
         s.totalImportance = totalDuration + (s.totalAverage * delay);
         s.totalVariance = ((s.totalAverage - totalMin) + (totalMax - s.totalAverage)) / 2;
         s.totalMin = totalMin;
         s.totalMax = totalMax;
      }

      return s;
   }

   public void reset()
   {
      totalDuration = 0;
      totalMin = Long.MAX_VALUE;
      totalMax = Long.MIN_VALUE;
      methodDuration = 0;
      methodMin = Long.MAX_VALUE;
      methodMax = Long.MIN_VALUE;
      invocations = -delay;
   }

}
