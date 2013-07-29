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

import java.io.File;
import java.io.FileInputStream;
import java.lang.instrument.Instrumentation;
import java.util.Map.Entry;
import java.util.Properties;

import org.magnos.trie.Trie;


public class Agent
{

   public static void premain( String agentArgs, Instrumentation inst ) throws Exception
   {
      File inputFile = new File( agentArgs );
      
      if ( !inputFile.exists() )
      {
         Logger.log( "'%s' is not a valid profiler configuration", agentArgs );
         
         return;
      }
      
      Trie<String, Boolean> include = Trie.forStrings( Boolean.FALSE );
      
      Properties props = new Properties();
      FileInputStream inputStream = new FileInputStream( inputFile );
      
      try
      {
         props.load( inputStream );
         
         for (Entry<Object, Object> e : props.entrySet())
         {
            String query = e.getKey().toString();
            Boolean included = Boolean.valueOf( e.getValue().toString() );
            
            include.put( query, included );
         }
      }
      finally
      {
         inputStream.close();
      }

      Logger.log( "adding shutdown hook and agent..." );

      try
      {
         Runtime.getRuntime().addShutdownHook( Transformer.get( inst, include ) );

         Logger.log( "success adding agent and hook" );
      }
      catch (Exception e)
      {
         Logger.log( "error adding agent: %s", e.getMessage() );
      }
   }
}
