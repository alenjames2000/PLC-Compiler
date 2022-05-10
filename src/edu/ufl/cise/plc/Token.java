package edu.ufl.cise.plc;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ILexer;
import edu.ufl.cise.plc.IToken;
import java.util.*;

public class Token implements IToken{

    private final Kind kind;
    private final String text; 

    private SourceLocation position;  

    public Token(Kind kind, String text, int line, int column) {
        this.kind = kind;
        this.text = text;
        this.position = new SourceLocation(line, column);
    }

	//returns the token kind
	public Kind getKind() 
    {
        return kind;
    }

	//returns the characters in the source code that correspond to this token
	//if the token is a STRING_LIT, this returns the raw characters, including delimiting "s and unhandled escape sequences.
	public String getText()
    {
        return text; 
    }
	
	//returns the location in the source code of the first character of the token.
	public SourceLocation getSourceLocation()
    {
        return position; 
    }

	//returns the int value represented by the characters of this token if kind is INT_LIT
	public int getIntValue() {
        if(this.kind == Kind.INT_LIT) {
            return Integer.parseInt(this.text);
        }
        throw new UnsupportedOperationException();
    }

	//returns the float value represented by the characters of this token if kind is FLOAT_LIT
	public float getFloatValue()
    {
        if(this.kind == Kind.FLOAT_LIT) {
            return Float.parseFloat(this.text);
        }
        throw new UnsupportedOperationException();
    }

	//returns the boolean value represented by the characters of this token if kind is BOOLEAN_LIT
	public boolean getBooleanValue()
    {
       if(this.kind == Kind.BOOLEAN_LIT){
           if(this.text.equals("true")){
               return true;
           }
           return false;
       }
        throw new UnsupportedOperationException();
    }
	
	//returns the String represented by the characters of this token if kind is STRING_LIT
	//The delimiters should be removed and escape sequences replaced by the characters they represent.  
	public String getStringValue()
    {
        if(this.kind == Kind.STRING_LIT){
            String ret = this.text.replace("\\b", "\b");
            ret = ret.replace("\\t", "\t");
            ret = ret.replace("\\n", "\n");
            ret = ret.replace("\\f", "\f");
            ret = ret.replace("\\r", "\r");
            ret = ret.replace("\\\"", "\"");
            ret = ret.replace("\\\'", "\'");
            ret = ret.replace("\\\\", "\\");
            return ret.substring(1, ret.length() - 1);
        }
        throw new UnsupportedOperationException();
    }



}