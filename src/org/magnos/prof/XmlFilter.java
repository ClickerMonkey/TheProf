
package org.magnos.prof;

import java.util.Iterator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlFilter<T extends Node> implements Iterable<T>, Iterator<T>
{

   private final NodeList nodes;
   private final int type;
   private int prev;
   private int curr;

   public XmlFilter( NodeList nodes, int type )
   {
      this.nodes = nodes;
      this.type = type;
      this.prev = -1;
      this.curr = getNextNode( prev );
   }

   @Override
   public Iterator<T> iterator()
   {
      return this;
   }

   @Override
   public boolean hasNext()
   {
      return (curr != -1);
   }

   @SuppressWarnings ("unchecked" )
   @Override
   public T next()
   {
      prev = curr;
      curr = getNextNode( prev );
      
      return (T)nodes.item( prev );
   }

   private int getNextNode( int i )
   {
      while (++i < nodes.getLength())
      {
         if (nodes.item( i ).getNodeType() == type)
         {
            return i;
         }
      }
      return -1;
   }

   @Override
   public void remove()
   {
      throw new UnsupportedOperationException();
   }

}
