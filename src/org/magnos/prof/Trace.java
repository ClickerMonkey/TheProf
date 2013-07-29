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

public final class Trace
{

   public long lastTime;
   public long start;
   public long durationMethod;
   public long durationTotal;
   public StatMethod stat;

   public void start( StatMethod traceStat )
   {
      stat = traceStat;
      lastTime = start = System.nanoTime();
      durationMethod = 0;
   }

   public void pause()
   {
      final long t0 = System.nanoTime();
      final long t1 = System.nanoTime();
      final long measureTime = (t1 - t0) << 1;
      
      durationMethod += Math.max( 0, (t0 - lastTime) - measureTime);
   }

   public void restart()
   {
      lastTime = System.nanoTime();
   }

   public void stop()
   {
      final long t0 = System.nanoTime();
      final long t1 = System.nanoTime();
      final long measureTime = ( t1 - t0 ) << 1;
      
      durationMethod += Math.max( 0, (t0 - lastTime) - measureTime );
      durationTotal = Math.max( 0, (t0 - start) - measureTime );
      
      stat.accum( this );
   }

}
