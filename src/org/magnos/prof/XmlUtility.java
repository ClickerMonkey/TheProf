
package org.magnos.prof;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.magnos.trie.Tries;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


public class XmlUtility
{

   public static final String TAG_ROOT = "theprof";
   public static final String TAG_PROPERTY_GROUP = "properties";
   public static final String TAG_PROPERTY = "property";
   public static final String TAG_INCLUSION_GROUP = "inclusion-filters";
   public static final String TAG_INCLUSION = "inclusion";
   public static final String TAG_EXCLUSION_GROUP = "exclusion-filters";
   public static final String TAG_EXCLUSION = "exclusion";

   public static final String ATTR_PATTERN = "pattern";
   public static final String ATTR_NAME = "name";
   public static final String ATTR_VALUE = "value";
   public static final String ATTR_DELAY = "delay";

   public static final String CONFIG_MODE = "mode";

   private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
   private static DocumentBuilder documentBuilder;

   static
   {
      try
      {
         documentBuilder = documentBuilderFactory.newDocumentBuilder();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public static Element read( InputStream stream ) throws Exception
   {
      Document doc = documentBuilder.parse( new InputSource( stream ) );

      return doc.getDocumentElement();
   }

   public static Configuration loadConfiguration( InputStream input ) throws Exception
   {
      Configuration config = new Configuration();
      config.delays = Tries.forInsensitiveStrings( 0 );
      config.inclusion = Tries.forInsensitiveStrings( Boolean.FALSE );
      config.mode = AgentMode.CSV;

      Element root = XmlUtility.read( input );

      validateTagName( root, TAG_ROOT );

      /* INCLUSIONS */
      Element inclusionGroup = findElement( root, TAG_INCLUSION_GROUP );
      XmlFilter<Element> inclusions = createFilter( inclusionGroup, TAG_INCLUSION, true );

      for (Element e : inclusions)
      {
         String pattern = getAttribute( e, ATTR_PATTERN, null );
         Integer delay = Integer.parseInt( getAttribute( e, ATTR_DELAY, 0 ) );

         config.inclusion.put( pattern, Boolean.TRUE );
         config.delays.put( pattern, delay );
      }

      /* EXCLUSIONS */
      Element exclusionGroup = findElement( root, TAG_EXCLUSION_GROUP );
      XmlFilter<Element> exclusions = createFilter( exclusionGroup, TAG_EXCLUSION, false );

      for (Element e : exclusions)
      {
         String pattern = getAttribute( e, ATTR_PATTERN, null );

         config.inclusion.put( pattern, Boolean.FALSE );
      }

      /* PROPERTIES */
      Element propertyGroup = findElement( root, TAG_PROPERTY_GROUP );
      XmlFilter<Element> properties = createFilter( propertyGroup, TAG_PROPERTY, false );

      for (Element e : properties)
      {
         String name = getAttribute( e, ATTR_NAME, null );
         String value = getAttribute( e, ATTR_VALUE, null );

         if (name.equalsIgnoreCase( CONFIG_MODE ))
         {
            config.mode = AgentMode.valueOf( value );
         }
         else
         {
            throw new IOException( "'" + name + "' is an unknown property" );
         }
      }

      return config;
   }

   private static void validateTagName( Element n, String expected ) throws IOException
   {
      if (!n.getTagName().equalsIgnoreCase( expected ))
      {
         throw new IOException( "Expected tag name '" + expected + "' but was '" + n.getTagName() + "'." );
      }
   }

   private static Element findElement( Element root, String tag ) throws IOException
   {
      NodeList elements = root.getElementsByTagName( tag );

      if (elements.getLength() == 0)
      {
         throw new IOException( "Tag " + tag + " was expected and not found." );
      }
      else if (elements.getLength() != 1)
      {
         throw new IOException( "Tag " + tag + " cannot have multiple definitions." );
      }

      return (Element)elements.item( 0 );
   }

   private static XmlFilter<Element> createFilter( Element root, String tag, boolean required ) throws IOException
   {
      XmlFilter<Element> filter = new XmlFilter<Element>( root.getElementsByTagName( tag ), Node.ELEMENT_NODE );

      if (required && !filter.hasNext())
      {
         throw new IOException( "Tag " + tag + " was expected and not found." );
      }

      return filter;
   }

   private static String getAttribute( Element e, String name, Object defaultValue ) throws IOException
   {
      String value = e.getAttribute( name );

      if (value.isEmpty())
      {
         if (defaultValue == null)
         {
            throw new IOException( "Required attribute '" + name + "' for element '" + e.getTagName() + "' missing." );
         }

         value = defaultValue.toString();
      }

      return value;
   }

}
