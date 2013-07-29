
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
