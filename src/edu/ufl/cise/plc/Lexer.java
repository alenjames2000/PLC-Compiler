package edu.ufl.cise.plc;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ILexer;
import edu.ufl.cise.plc.IToken;
import edu.ufl.cise.plc.Token;
import edu.ufl.cise.plc.Lexer;
import java.util.*;

public class Lexer implements ILexer{
    private int position = 0;
    private List<IToken> list = new ArrayList<IToken>(); //However we are storing it
    private enum State{START, IDENT, HAS_ZERO, INT_LIT, HAS_DOT, FLOAT_LIT, HAS_BSLASH, STRING_LIT, COMMENT, HAS_GT, HAS_LT, HAS_BANG, HAS_MINUS, HAS_EQUAL}
    
    private java.util.Map<String, IToken.Kind> reserve = new LinkedHashMap<String, IToken.Kind>();

    public Lexer(String input){
        //MAP FOR RESERVED WORDS
        reserve.put("string", IToken.Kind.TYPE);
        reserve.put("int", IToken.Kind.TYPE);
        reserve.put("float", IToken.Kind.TYPE);
        reserve.put("boolean", IToken.Kind.TYPE);
        reserve.put("color", IToken.Kind.TYPE);
        reserve.put("image", IToken.Kind.TYPE);

        reserve.put("getWidth", IToken.Kind.IMAGE_OP);
        reserve.put("getHeight", IToken.Kind.IMAGE_OP);

        reserve.put("getRed", IToken.Kind.COLOR_OP);
        reserve.put("getGreen", IToken.Kind.COLOR_OP);
        reserve.put("getBlue", IToken.Kind.COLOR_OP);

        reserve.put("BLACK", IToken.Kind.COLOR_CONST);
        reserve.put("BLUE", IToken.Kind.COLOR_CONST);
        reserve.put("CYAN", IToken.Kind.COLOR_CONST);
        reserve.put("DARK_GRAY", IToken.Kind.COLOR_CONST);
        reserve.put("GRAY", IToken.Kind.COLOR_CONST);
        reserve.put("GREEN", IToken.Kind.COLOR_CONST);
        reserve.put("LIGHT_GRAY", IToken.Kind.COLOR_CONST);
        reserve.put("MAGENTA", IToken.Kind.COLOR_CONST);
        reserve.put("ORANGE", IToken.Kind.COLOR_CONST);
        reserve.put("PINK", IToken.Kind.COLOR_CONST);
        reserve.put("RED", IToken.Kind.COLOR_CONST);
        reserve.put("WHITE", IToken.Kind.COLOR_CONST);
        reserve.put("YELLOW", IToken.Kind.COLOR_CONST);

        reserve.put("true", IToken.Kind.BOOLEAN_LIT);
        reserve.put("false", IToken.Kind.BOOLEAN_LIT);

        reserve.put("if", IToken.Kind.KW_IF);
        reserve.put("else", IToken.Kind.KW_ELSE);
        reserve.put("fi", IToken.Kind.KW_FI);
        reserve.put("write", IToken.Kind.KW_WRITE);
        reserve.put("console", IToken.Kind.KW_CONSOLE);
        reserve.put("void", IToken.Kind.KW_VOID);

        createTokens(input);
    }
    //Vince

    private void createTokens(String input){
        State current = State.START;
        int pos = 0;
        int start = 0;
        int line = 0;
        int lastline = 0;
        input = input.concat("\n");

        while(pos < input.length()){
            switch(current){
                case START: {
                    start = pos;
                    //Main Tokens
                    char x = input.charAt(pos);
                    if(Character.isJavaIdentifierStart(x)){
                        current = State.IDENT;
                    }
                    else if(Character.isDigit(x) && (Character.compare(x, '0') != 0)){
                        current = State.INT_LIT;
                    }
                    else if(Character.compare(x, '0') == 0){
                        current = State.HAS_ZERO;
                        pos++;
                    }
                    else if(Character.compare(x, '\"') == 0) {
                        current = State.STRING_LIT;
                        pos++;
                    }
                    //Empty spaces
                    else if((Character.compare(x, ' ') == 0) || (Character.compare(x, '\t') == 0) || (Character.compare(x, '\r') == 0) || (Character.compare(x, '\n') == 0)){
                        if(Character.compare(x, '\n') == 0){
                            //System.out.println("Hello");
                            line++;
                            lastline = pos + 1;
                        }
                        pos++;
                    }
                    //Operators
                    else if(Character.compare(x, '#') == 0){
                        current = State.COMMENT;
                        pos++;
                    }
                    else if(Character.compare(x, '>') == 0){
                        current = State.HAS_GT;
                        pos++;
                    }
                    else if(Character.compare(x, '<') == 0){
                        current = State.HAS_LT;
                        pos++;
                    }
                    else if(Character.compare(x, '!') == 0){
                        current = State.HAS_BANG;
                        pos++;
                    }
                    else if(Character.compare(x, '-') == 0){
                        current = State.HAS_MINUS;
                        pos++;
                    }
                    else if(Character.compare(x, '=') == 0){
                        current = State.HAS_EQUAL;
                        pos++;
                    }
                    //Single Path Operators
                    else if(Character.compare(x, '&') == 0){
                        //TODO: AND TOKEN
                        list.add(new Token(IToken.Kind.AND, input.substring(start, pos+1), line, start-lastline));
                        //System.out.println("AND\nLength= 1\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                        pos++;
                    }
                    else if(Character.compare(x, ',') == 0){
                        //TODO: COMMA TOKEN
                        list.add(new Token(IToken.Kind.COMMA, input.substring(start, pos+1), line, start-lastline));
                        //System.out.println("COMMA\nLength= 1\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                        pos++;
                    }
                    else if(Character.compare(x, '/') == 0){
                        //TODO: DIV TOKEN
                        list.add(new Token(IToken.Kind.DIV, input.substring(start, pos+1), line, start-lastline));
                        //System.out.println("DIV\nLength= 1\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                        pos++;
                    }
                    else if(Character.compare(x, '(') == 0){
                        //TODO: LPAREN TOKEN
                        list.add(new Token(IToken.Kind.LPAREN, input.substring(start, pos+1), line, start-lastline));
                        //System.out.println("LPAREN\nLength= 1\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                        pos++;
                    }
                    else if(Character.compare(x, '[') == 0){
                        //TODO: LSQUARE TOKEN
                        list.add(new Token(IToken.Kind.LSQUARE, input.substring(start, pos+1), line, start-lastline));
                        //System.out.println("LSQUARE\nLength= 1\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                        pos++;
                    }
                    else if(Character.compare(x, '%') == 0){
                        //TODO: MOD TOKEN
                        list.add(new Token(IToken.Kind.MOD, input.substring(start, pos+1), line, start-lastline));
                        //System.out.println("MOD\nLength= 1\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                        pos++;
                    }
                    else if(Character.compare(x, '|') == 0){
                        //TODO: OR TOKEN
                        list.add(new Token(IToken.Kind.OR, input.substring(start, pos+1), line, start-lastline));
                        //System.out.println("OR\nLength= 1\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                        pos++;
                    }
                    else if(Character.compare(x, '+') == 0){
                        //TODO: PLUS TOKEN
                        list.add(new Token(IToken.Kind.PLUS, input.substring(start, pos+1), line, start-lastline));
                        //System.out.println("PLUS\nLength= 1\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                        pos++;
                    }
                    else if(Character.compare(x, '^') == 0){
                        //TODO: RETURN TOKEN
                        list.add(new Token(IToken.Kind.RETURN, input.substring(start, pos+1), line, start-lastline));
                        //System.out.println("RETURN\nLength= 1\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                        pos++;
                    }
                    else if(Character.compare(x, ')') == 0){
                        //TODO: RPAREN TOKEN
                        list.add(new Token(IToken.Kind.RPAREN, input.substring(start, pos+1), line, start-lastline));
                        //System.out.println("RPAREN\nLength= 1\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                        pos++;
                    }
                    else if(Character.compare(x, ']') == 0){
                        //TODO: RSQUARE TOKEN
                        list.add(new Token(IToken.Kind.RSQUARE, input.substring(start, pos+1), line, start-lastline));
                        //System.out.println("RSQUARE\nLength= 1\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                        pos++;
                    }
                    else if(Character.compare(x, ';') == 0){
                        //TODO: SEMI TOKEN
                        list.add(new Token(IToken.Kind.SEMI, input.substring(start, pos+1), line, start-lastline));
                        //System.out.println("SEMI\nLength= 1\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                        pos++;
                    }
                    else if(Character.compare(x, '*') == 0){
                        //TODO: TIMES TOKEN
                        list.add(new Token(IToken.Kind.TIMES, input.substring(start, pos+1), line, start-lastline));
                        //System.out.println("TIMES\nLength= 1\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                        pos++;
                    }
                    else{
                        //TODO: Throw LexicalException, placed UnsupportedOperationException to get passed testError0()
                        list.add(new Token(Kind.ERROR, "Unexpected Character " + input.substring(pos, pos+1), line, start-lastline));
                        //System.out.println("ERROR\nLength= 1\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                        pos = input.length();
                    }
                    break;
                }
                case HAS_GT : {
                    char x = input.charAt(pos);
                    if(Character.compare(x, '=') == 0){
                        //TODO: GE TOKEN
                        list.add(new Token(IToken.Kind.GE, input.substring(start, pos+1), line, start-lastline));
                        //System.out.println("GE\nLength= " + (pos - start) + "\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                        pos++;
                    }
                    else if(Character.compare(x, '>') == 0){
                        //TODO RANGLE TOKEN
                        list.add(new Token(IToken.Kind.RANGLE, input.substring(start, pos+1), line, start-lastline));
                        //System.out.println("RANGLE\nLength= " + (pos - start) + "\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                        pos++;
                    }
                    else{
                        //TODO GT TOKEN
                        list.add(new Token(IToken.Kind.GT, input.substring(start, pos), line, start-lastline));
                        //System.out.println("GT\nLength= " + (pos - start) + "\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                    }
                    current = State.START;
                    break;
                }
                case HAS_LT : {
                    char x = input.charAt(pos);
                    if(Character.compare(x, '=') == 0){
                        //TODO: LE TOKEN
                        list.add(new Token(IToken.Kind.LE, input.substring(start, pos+1), line, start-lastline));
                        //System.out.println("LE\nLength= " + (pos - start) + "\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                        pos++;
                    }
                    else if(Character.compare(x, '<') == 0){
                        //TODO LANGLE TOKEN
                        list.add(new Token(IToken.Kind.LANGLE, input.substring(start, pos+1), line, start-lastline));
                        //System.out.println("LANGLE\nLength= " + (pos - start) + "\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                        pos++;
                    }
                    else if(Character.compare(x, '-') == 0){
                        //TODO LARROW TOKEN
                        list.add(new Token(IToken.Kind.LARROW, input.substring(start, pos+1), line, start-lastline));
                        //System.out.println("LARROW\nLength= " + (pos - start) + "\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                        pos++;
                    }
                    else{
                        //TODO LT TOKEN
                        list.add(new Token(IToken.Kind.LT, input.substring(start, pos), line, start-lastline));
                        //System.out.println("LT\nLength= " + (pos - start) + "\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                    }
                    current = State.START;
                    break;
                }
                case HAS_BANG : {
                    char x = input.charAt(pos);
                    if(Character.compare(x, '=') == 0){
                        //TODO: NOT_EQUALS TOKEN
                        list.add(new Token(IToken.Kind.NOT_EQUALS, input.substring(start, pos+1), line, start-lastline));
                        //System.out.println("NOT_EQUALS\nLength= " + (pos - start) + "\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                        pos++;
                    }
                    else{
                        //TODO BANG TOKEN
                        list.add(new Token(IToken.Kind.BANG, input.substring(start, pos), line, start-lastline));
                        //System.out.println("BANG\nLength= " + (pos - start) + "\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                    }
                    current = State.START;
                    break;
                }
                case HAS_MINUS : {
                    char x = input.charAt(pos);
                    if(Character.compare(x, '>') == 0){
                        //TODO: RARROW TOKEN
                        list.add(new Token(IToken.Kind.RARROW, input.substring(start, pos+1), line, start-lastline));
                        //System.out.println("RARROW\nLength= " + (pos - start) + "\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                        pos++;
                    }
                    else{
                        //TODO MINUS TOKEN
                        list.add(new Token(IToken.Kind.MINUS, input.substring(start, pos), line, start-lastline));
                        //System.out.println("MINUS\nLength= " + (pos - start) + "\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                    }
                    current = State.START;
                    break;
                }
                case HAS_EQUAL : {
                    char x = input.charAt(pos);
                    if(Character.compare(x, '=') == 0){
                        //TODO: EQUALS TOKEN
                        list.add(new Token(IToken.Kind.EQUALS, input.substring(start, pos+1), line, start-lastline));
                        //System.out.println("EQUALS\nLength= " + (pos - start) + "\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                        pos++;
                    }
                    else{
                        //TODO ASSIGN TOKEN
                        list.add(new Token(IToken.Kind.ASSIGN, input.substring(start, pos), line, start-lastline));
                        //System.out.println("ASSIGN\nLength= " + (pos - start) + "\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");

                    }
                    current = State.START;
                    break;
                }
                case IDENT :{
                    while(current == State.IDENT){
                        char x = input.charAt(pos);
                        if(Character.isJavaIdentifierStart(x) || Character.isDigit(x)){
                            pos++;
                        }
                        else{
                            if(reserve.containsKey(input.substring(start, pos))){
                                //TODO CREATE TOKEN WITH MAP
                                //System.out.println("RESERVED\nLength= " + (pos - start) + "\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                                list.add(new Token(reserve.get(input.substring(start, pos)), input.substring(start, pos), line, start-lastline));
                            }
                            else{
                                //TODO: Create IDENT and check for reserved words
                                list.add(new Token(IToken.Kind.IDENT, input.substring(start, pos), line, start-lastline));
                                //System.out.println("IDENT\nLength= " + (pos - start) + "\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                            }
                            current = State.START;
                        }
                    }
                    break;
                }
                case HAS_ZERO :{
                    char x = input.charAt(pos);
                    if(Character.compare(x, '.') == 0){
                        current = State.HAS_DOT;
                        pos++;
                    }
                    else{
                        //TODO: CREATE INT_LIT
                        list.add(new Token(IToken.Kind.INT_LIT, input.substring(start, pos), line, start-lastline));
                        //System.out.println("INT_LIT\nLength= " + (pos - start) + "\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                        current = State.START;
                    }
                    break;
                }
                case HAS_DOT :{
                    char x = input.charAt(pos);
                    if(Character.isDigit(x)){
                        current = State.FLOAT_LIT;
                    }
                    else{
                        //TODO: ERROR token
                        list.add(new Token(Kind.ERROR, "Expected Number(s) After Decimal", line, pos));
                        //System.out.println("ERROR\nLength= " + (pos - start) + "\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                        pos = input.length();
                        current = State.START;
                    }
                    break;
                }
                case INT_LIT :{
                    while(current == State.INT_LIT){
                        char x = input.charAt(pos);
                        if(Character.isDigit(x)){
                            pos++;
                        }
                        else if(Character.compare(x, '.') == 0){
                            current = State.HAS_DOT;
                            pos++;
                        }
                        else{
                            //TODO: create INT_LIT
                            try {
                                Integer.parseInt(input.substring(start, pos));
                                list.add(new Token(IToken.Kind.INT_LIT, input.substring(start, pos), line, start-lastline));
                                //System.out.println("INT_LIT\nLength= " + (pos - start) + "\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                            }
                            catch(NumberFormatException e){
                                list.add(new Token(Kind.ERROR, "Failed to Parse Integer", line, start-lastline));
                                //System.out.println("ERROR\nLength= " + (pos - start) + "\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                                pos = input.length();
                            }
                            current = State.START;
                        }
                    }
                    break;
                }
                case FLOAT_LIT :{
                    while(current == State.FLOAT_LIT){
                        char x = input.charAt(pos);
                        if(Character.isDigit(x)){
                            pos++;
                        }
                        else{
                            //TODO: create FLOAT_LIT
                            try {
                                Float.parseFloat(input.substring(start, pos));
                                list.add(new Token(Kind.FLOAT_LIT, input.substring(start, pos), line, start-lastline));
                                //System.out.println("FLOAT_LIT\nLength= " + (pos - start) + "\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                            }
                            catch(NumberFormatException e){
                                list.add(new Token(Kind.ERROR, "Failed to Parse Float", line, start-lastline));
                                //System.out.println("ERROR\nLength= " + (pos - start) + "\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                                pos = input.length();
                            }
                            current = State.START;
                        }
                    }
                    break;
                }
                case STRING_LIT :{
                    while(current == State.STRING_LIT){
                        char x = input.charAt(pos);
                        if(Character.compare(x, '\\') == 0){
                            current = State.HAS_BSLASH;
                            pos++;
                        }
                        else if(Character.compare(x, '\"') == 0){
                            //TODO: create STRING_LIT
                            list.add(new Token(IToken.Kind.STRING_LIT, input.substring(start, pos+1), line, start-lastline));
                            //System.out.println("STRING_LIT\nLength= " + (pos - start -2 ) + "\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                            current = State.START;
                            pos++;
                            
                        }
                        else if(pos + 1 == input.length()){
                            //TODO: Error token
                            list.add(new Token(Kind.ERROR, "Expected \" at End of String Literal", line, pos));
                            //System.out.println("ERROR\nLength= " + (pos - start) + "\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                            pos = input.length();
                            current = State.START;
                        }
                        else{
                            pos++;
                        }
                    }
                    break;
                }
                case HAS_BSLASH : {
                    char x = input.charAt(pos);

                    if((Character.compare(x, 'b') == 0) || (Character.compare(x, 't') == 0) || (Character.compare(x, 'n') == 0) || (Character.compare(x, 'f') == 0) ||
                            (Character.compare(x, 'r') == 0) || (Character.compare(x, '\"') == 0) || (Character.compare(x, '\'') == 0) || (Character.compare(x, '\\') == 0)){
                        pos++;
                        current = State.STRING_LIT;
                    }
                    else{
                        //TODO: ERROR TOKEN
                        list.add(new Token(Kind.ERROR, "Expected Valid Escape Character after \\", line, pos));
                        //System.out.println("ERROR\nLength= " + (pos - start) + "\nPosition = " + start + "\nCoords = (" + line + "," + (start-lastline) + ")\n");
                        pos = input.length();
                        current = State.START;
                    }
                    break;
                }
                case COMMENT : {
                    while(current == State.COMMENT){
                        char x = input.charAt(pos);
                        if(!((Character.compare(x, '\r') == 0) || (Character.compare(x, '\n') == 0))){
                            pos++;
                        }
                        else if(pos + 1 == input.length()){
                            current = State.START;
                        }
                        else if(Character.compare(x, '\n') == 0){
                            current = State.START;
                        }
                        else{
                            current = State.START;
                        }
                    }
                    break;
                }
                default : throw new IllegalStateException("Lexer Bug");
            }
        }
        if(list.size() == 0 || list.get(list.size()-1).getKind() != IToken.Kind.ERROR){
            list.add(new Token(IToken.Kind.EOF, "", line, start-lastline));
        }

    }
    //return next token and advance internal position
    @Override public IToken next() throws LexicalException{
        IToken ret = list.get(position++);
        if(ret.getKind() == Kind.ERROR){
            throw new LexicalException(ret.getText(), ret.getSourceLocation());
        }
        return ret;
    }
    //return next token but do not advance internal position
    @Override public IToken peek() throws LexicalException{
        IToken ret = list.get(position);
        if(ret.getKind() == Kind.ERROR){
            throw new LexicalException(ret.getText(), ret.getSourceLocation());
        }
        return ret;
    }
}
