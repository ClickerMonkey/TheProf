
package org.magnos.prof;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javassist.ByteArrayClassPath;
import javassist.CannotCompileException;
import javassist.ClassMap;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

import org.magnos.trie.Trie;
import org.magnos.trie.TrieMatch;


public class Transformer extends Thread implements ClassFileTransformer
{

   public static final String CLASS_STAT = StatMethod.class.getCanonicalName();
   public static final String CLASS_STAT_CLASS = StatClass.class.getCanonicalName();
   public static final String CLASS_PROF = Profiler.class.getCanonicalName();

   private static Transformer instance;

   public static Transformer get()
   {
      return instance;
   }

   public static Transformer get( Instrumentation inst, Trie<String, Boolean> include )
   {
      return (instance = new Transformer( inst, include ));
   }

   private final ClassPool classes;
   private final Instrumentation inst;
   private final Trie<String, Boolean> include;
   private final AtomicInteger methodNumber;

   private Transformer( Instrumentation inst, Trie<String, Boolean> include )
   {
      this.inst = inst;
      this.inst.addTransformer( this );
      this.classes = ClassPool.getDefault();
      this.include = include;
      this.methodNumber = new AtomicInteger();
   }

   @Override
   public byte[] transform( ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer )
   {
      className = className.replace( '/', '.' );

      if (include.get( className, TrieMatch.STARTS_WITH ) != Boolean.TRUE)
      {
         return null;
      }

      byte[] byteCode = null;

      try
      {
         classes.insertClassPath( new ByteArrayClassPath( className, classfileBuffer ) );

         CtClass cc = classes.get( className );

         if (cc.isInterface() || cc.isEnum() || cc.isAnnotation())
         {
            return null;
         }

         Logger.log( "transforming %s", className );

         CtClass classStatClass = classes.get( CLASS_STAT_CLASS );
         CtClass classStat = classes.get( CLASS_STAT );

         CtMethod[] methods = cc.getDeclaredMethods();
         ClassMap classMap = new ClassMap();
         int profiled = 0;

         CtField fieldStatClass = new CtField( classStatClass, "STAT_CLASS", cc );
         fieldStatClass.setModifiers( Modifier.STATIC | Modifier.PUBLIC | Modifier.FINAL );
         cc.addField( fieldStatClass, "new " + CLASS_STAT_CLASS + "(\"" + className + "\");" );

         for (int i = 0; i < methods.length; i++)
         {
            CtMethod m = methods[i];

            if (m.getMethodInfo().getCodeAttribute() != null)
            {
               try
               {
                  CtField fieldStat = new CtField( classStat, "STAT_" + i, cc );
                  fieldStat.setModifiers( Modifier.STATIC | Modifier.PUBLIC | Modifier.FINAL );
                  cc.addField( fieldStat, "new " + CLASS_STAT + "(STAT_CLASS, \"" + getMethodName( cc, m ) + "\");" );

                  String implName = "m" + methodNumber.getAndIncrement();
                  CtMethod implMethod = CtNewMethod.copy( m, implName, cc, classMap );
                  cc.addMethod( implMethod );

                  String code = "" +
                     "{" +
                     "	%s.start(STAT_%d);" +
                     "	try {" +
                     "		%s%s($$);" +
                     "	} finally {" +
                     "		%s.stop();" +
                     "	}" +
                     "}";

                  String returner = (m.getReturnType() == CtClass.voidType ? "" : "return ");

                  m.setBody( String.format( code, CLASS_PROF, i, returner, implName, CLASS_PROF ) );

                  profiled++;
               }
               catch (Exception t)
               {
                  Logger.log( "method(%s) with modifiers(%s) failed compilation with message %s", m.getName(), Modifier.toString( m.getModifiers() ), t.getMessage() );
               }
            }
         }

         Logger.log( "%d/%d methods profiled for %s.", profiled, methods.length, className );

         byteCode = cc.toBytecode();
      }
      catch (CannotCompileException e)
      {
         Logger.error( "error compiling", e );
      }
      catch (NotFoundException e)
      {
         Logger.error( "class not found", e );
      }
      catch (IOException e)
      {
         Logger.error( "I/O exception", e );
      }
      catch (Exception e)
      {
         Logger.error( "error occurred", e );
      }

      return byteCode;
   }

   private String getMethodName( CtClass clazz, CtMethod method ) throws NotFoundException
   {
      final CtClass[] types = method.getParameterTypes();
      final StringBuilder sb = new StringBuilder();

      sb.append( method.getName() ).append( '(' );

      for (int k = 0; k < types.length; k++)
      {
         if (k > 0)
         {
            sb.append( ',' );
         }

         sb.append( types[k].getSimpleName() );
      }

      sb.append( ')' );

      return sb.toString();
   }

   public void run()
   {
      List<StatClass> classes = StatClass.instances;

      Logger.log( "shutdown hook running, dumping %d profiles", classes.size() );

      File dir = new File( "stats" );
      if (!dir.isDirectory() && !dir.mkdir())
      {
         Logger.log( "error creating stats directory" );
      }

      final String COLUMN_FORMAT = "\"%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\"\n".replaceAll( ",", "\",\"" );
      final Object[] COLUMN_HEADERS = { "Method", "Invocations", "Average (method)", "Variance (method)", "Importance (method)", "Min (method)", "Max (method)", "Average (total)", "Variance (total)", "Importance (total)", "Min (total)", "Max (total)" }; 
      
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
                  if (stat.times != 0)
                  {
                     long avgTotal = stat.durationTotal / stat.times;
                     long avgMethod = stat.durationMethod / stat.times;

                     Object[] arguments = {
                        /* method */
                        stat.name.replaceAll( "\"", "\"\"" ), 
                        /* invocations */
                        stat.times,
                        /* method */
                        toTimeString( avgMethod ),
                        toTimeString( avgMethod * stat.times ),
                        toTimeString( ((avgMethod - stat.minMethod) + (stat.maxMethod - avgMethod)) / 2 ),
                        toTimeString( stat.minMethod ),
                        toTimeString( stat.maxMethod ),
                        /* total */
                        toTimeString( avgTotal ),
                        toTimeString( avgTotal * stat.times ),
                        toTimeString( ((avgTotal - stat.minTotal) + (stat.maxTotal - avgTotal)) / 2 ),
                        toTimeString( stat.minTotal ),
                        toTimeString( stat.maxTotal ) 
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
   
   private static String toTimeString(long nanoTime)
   {
      long nanos = nanoTime % 1000;
      long micros = (nanoTime /= 1000) % 1000;
      long millis = (nanoTime /= 1000) % 1000;
      long seconds = (nanoTime /= 1000);
      
      return String.format("%d.%03d %03d %03d", seconds, millis, micros, nanos);
   }

}
