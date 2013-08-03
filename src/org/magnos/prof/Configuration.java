package org.magnos.prof;

import org.magnos.trie.Trie;


public class Configuration
{

   public AgentMode mode;
   public Trie<String, Boolean> inclusion;
   public Trie<String, Integer> delays;
   
}
