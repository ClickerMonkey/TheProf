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

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
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

import org.magnos.trie.TrieMatch;


public class Transformer implements ClassFileTransformer
{

   public static final String CLASS_STAT = StatMethod.class.getCanonicalName();
   public static final String CLASS_STAT_CLASS = StatClass.class.getCanonicalName();
   public static final String CLASS_PROF = Profiler.class.getCanonicalName();

   private final Configuration config;
   private final ClassPool classes;
   private final AtomicInteger methodNumber;

   public Transformer( Configuration config )
   {
      this.config = config;
      this.classes = ClassPool.getDefault();
      this.methodNumber = new AtomicInteger();
   }

   @Override
   public byte[] transform( ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer )
   {
      className = className.replace( '/', '.' );

      if (config.inclusion.get( className, TrieMatch.STARTS_WITH ) != Boolean.TRUE)
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
                  String methodName = getMethodName( cc, m );
                  String fullMethodName = className + "#" + methodName;
                  Integer delay = config.delays.get( fullMethodName );
                  
                  if (delay == null) 
                  {
                     delay = 0;
                  }
                  
                  CtField fieldStat = new CtField( classStat, "STAT_" + i, cc );
                  fieldStat.setModifiers( Modifier.STATIC | Modifier.PUBLIC | Modifier.FINAL );
                  cc.addField( fieldStat, "new " + CLASS_STAT + "(STAT_CLASS, \"" + methodName + "\", " + delay + ");" );

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

}
