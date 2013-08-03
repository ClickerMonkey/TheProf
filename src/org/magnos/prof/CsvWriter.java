
package org.magnos.prof;

import java.io.File;
import java.io.PrintStream;
import java.util.Collection;


public class CsvWriter extends Thread
{

   public static final String COLUMN_FORMAT = "\"%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\"\n".replaceAll( ",", "\",\"" );
   
   public static final Object[] COLUMN_HEADERS = { 
      "Method", 
      "Delay", 
      "Invocations", 
      "Average (method)", "Variance (method)", "Importance (method)", "Min (method)", "Max (method)", 
      "Average (total)", "Variance (total)", "Importance (total)", "Min (total)", "Max (total)" 
   };

   @Override
   public void run()
   {
      Collection<StatClass> classes = StatClass.instances.values();

      Logger.log( "shutdown hook running, dumping %d profiles", classes.size() );

      File dir = new File( "stats" );
      if (!dir.isDirectory() && !dir.mkdir())
      {
         Logger.log( "error creating stats directory" );
      }

      PrintStream all = null;

      try
      {
         all = new PrintStream( "stats/TheProf.csv" );
         all.format( COLUMN_FORMAT, COLUMN_HEADERS );
      }
      catch (Exception e)
      {
         Logger.error( "Error creating TheProf.csv", e );
      }

      for (StatClass clazz : classes)
      {
         if (!clazz.hasStatistics())
         {
            Logger.log( "%s has no statistics", clazz.name );

            continue;
         }

         try
         {
            PrintStream out = new PrintStream( "stats/" + clazz.name + ".csv" );

            try
            {
               out.format( COLUMN_FORMAT, COLUMN_HEADERS );

               for (StatMethod stat : clazz.methods)
               {
                  StatInstance si = stat.getInstance();
                  
                  if (si.invocations > 0)
                  {
                     
                     Object[] arguments = {
                        /* method */
                        si.method.name.replaceAll( "\"", "\"\"" ),
                        /* delay */
                        si.method.delay,
                        /* invocations */
                        si.invocations,
                        /* method */
                        toTimeString( si.methodAverage ),
                        toTimeString( si.methodImportance ),
                        toTimeString( si.methodVariance ),
                        toTimeString( si.methodMin ),
                        toTimeString( si.methodMax ),
                        /* total */
                        toTimeString( si.totalAverage ),
                        toTimeString( si.totalImportance ),
                        toTimeString( si.totalVariance ),
                        toTimeString( si.totalMin ),
                        toTimeString( si.totalMax )
                     };

                     out.format( COLUMN_FORMAT, arguments );

                     if (all != null)
                     {
                        arguments[0] = clazz.name + "#" + arguments[0];

                        all.format( COLUMN_FORMAT, arguments );
                     }
                  }
               }

               Logger.log( "dumped %s", clazz.name );
            }
            finally
            {
               out.close();
            }
         }
         catch (Exception e)
         {
            Logger.error( "exception", e );
         }
      }

      if (all != null)
      {
         all.close();
      }
   }

   private String toTimeString( long nanoTime )
   {
      long nanos = nanoTime % 1000;
      long micros = (nanoTime /= 1000) % 1000;
      long millis = (nanoTime /= 1000) % 1000;
      long seconds = (nanoTime /= 1000);

      return String.format( "%d.%03d %03d %03d", seconds, millis, micros, nanos );
   }

}
