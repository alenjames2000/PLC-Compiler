package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.Declaration;
import java.util.HashMap;

public class SymbolTable {
    HashMap<String, Declaration> entries = new HashMap<String, Declaration>();

    public boolean insertEntry(String name, Declaration dec) {
        boolean hasKey = entries.containsKey(name);
        if(!hasKey){
            entries.put(name, dec);
        }

        return hasKey;
    }

    public Declaration lookup(String name){
        return entries.get(name);
    }

    public void remove(String name){
        entries.remove(name);
    }
}
