// RANDOM THING I MADE TO JUST TEST RANDOM THINGS
package edu.ufl.cise.plc;

import edu.ufl.cise.plc.Lexer;

public class Test {
    public static void main(String args[]) throws LexicalException{
        Lexer test = new Lexer("""
				&,/()[]%|+^;* 
				""");
		IToken iter = test.next();
		while(iter.getKind() != IToken.Kind.ERROR && iter.getKind() != IToken.Kind.EOF){
			System.out.println(iter.getKind().name());
			iter = test.next();
		}
    }
}
// & = ! , / == >= > << <- <= ( [ < - % != | + >> -> ^ ) ] ; *