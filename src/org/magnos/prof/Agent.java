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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;


public class Agent
{

   public static final String DEFAULT_CONFIGURATION_FILE = "theprof.xml";

   public static void premain( String agentArgs, Instrumentation inst ) throws Exception
   {
      InputStream input = findConfiguration( agentArgs );

      if (input == null)
      {
         throw new IOException( "The specified configuration was not found on the file-system in the current-working-directory, inside any JAR loaded by this class-path, or inside the TheProf JAR." );
      }

      Configuration config = XmlUtility.loadConfiguration( input );

      Logger.log( "adding shutdown hook and agent..." );

      try
      {
         Transformer transformer = new Transformer( config ); 
         
         inst.addTransformer( transformer );
         
         if (config.mode == AgentMode.CSV)
         {
            Runtime.getRuntime().addShutdownHook( new CsvWriter() );
            
            Logger.log( "success adding agent and hook" );
         }
      }
      catch (Exception e)
      {
         Logger.log( "error adding agent: %s", e.getMessage() );
      }
   }

   private static InputStream findConfiguration( String suggestedFile ) throws FileNotFoundException
   {
      if (suggestedFile == null || suggestedFile.isEmpty())
      {
         suggestedFile = DEFAULT_CONFIGURATION_FILE;
      }

      File file = new File( suggestedFile );

      if (file.isFile())
      {
         return new FileInputStream( file );
      }

      return Agent.class.getClassLoader().getResourceAsStream( suggestedFile );
   }

}
