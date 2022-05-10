package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.*;
import edu.ufl.cise.plc.ast.Types.Type;
import static edu.ufl.cise.plc.CompilerComponentFactory.getLexer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class Parser implements IParser{
    private String input;
    private ILexer tokens;
    private IToken current;
    private java.util.Map<String, ArrayList<IToken.Kind>> predictSets = new LinkedHashMap<String, ArrayList<IToken.Kind>>();
    private java.util.Map<String, ArrayList<IToken.Kind>> firstSets = new LinkedHashMap<String, ArrayList<IToken.Kind>>();

    public Parser(String input) {
        this.input = input;

        predictSets.put("Program", new ArrayList<IToken.Kind>(
                Arrays.asList(IToken.Kind.TYPE, IToken.Kind.KW_VOID)));

        firstSets.put("NameDef", new ArrayList<IToken.Kind>(
                Arrays.asList(IToken.Kind.TYPE)));

        firstSets.put("Declaration", new ArrayList<IToken.Kind>(
                Arrays.asList(IToken.Kind.TYPE)));

        firstSets.put("Statement", new ArrayList<IToken.Kind>(
                Arrays.asList(IToken.Kind.IDENT, IToken.Kind.KW_WRITE, IToken.Kind.RETURN)));

        firstSets.put("Dimension", new ArrayList<IToken.Kind>(
                Arrays.asList(IToken.Kind.LSQUARE)));

        firstSets.put("Expr", new ArrayList<IToken.Kind>(
                Arrays.asList(IToken.Kind.KW_IF, IToken.Kind.BANG, IToken.Kind.MINUS, IToken.Kind.COLOR_OP, IToken.Kind.IMAGE_OP, IToken.Kind.BOOLEAN_LIT,
                IToken.Kind.STRING_LIT, IToken.Kind.INT_LIT, IToken.Kind.FLOAT_LIT, IToken.Kind.IDENT, IToken.Kind.LPAREN, IToken.Kind.COLOR_CONST, IToken.Kind.KW_CONSOLE,
                        IToken.Kind.LANGLE)));

        firstSets.put("ConditionalExpr", new ArrayList<IToken.Kind>(
                Arrays.asList(IToken.Kind.KW_IF)));

        firstSets.put("LogicalOrExpr", new ArrayList<IToken.Kind>(
                Arrays.asList(IToken.Kind.BANG, IToken.Kind.MINUS, IToken.Kind.COLOR_OP, IToken.Kind.IMAGE_OP, IToken.Kind.BOOLEAN_LIT,
                        IToken.Kind.STRING_LIT, IToken.Kind.INT_LIT, IToken.Kind.FLOAT_LIT, IToken.Kind.IDENT, IToken.Kind.LPAREN, IToken.Kind.COLOR_CONST, IToken.Kind.KW_CONSOLE,
                        IToken.Kind.LANGLE)));

        firstSets.put("comparisonExprWhile", new ArrayList<IToken.Kind>(
                Arrays.asList(IToken.Kind.LT, IToken.Kind.GT, IToken.Kind.EQUALS, IToken.Kind.NOT_EQUALS, IToken.Kind.LE, IToken.Kind.GE)
        ));

        firstSets.put("additiveExprWhile", new ArrayList<IToken.Kind>(
                Arrays.asList(IToken.Kind.MINUS, IToken.Kind.PLUS)
        ));

        firstSets.put("multiplicativeExprWhile", new ArrayList<IToken.Kind>(
                Arrays.asList(IToken.Kind.TIMES, IToken.Kind.DIV, IToken.Kind.MOD)
        ));

        firstSets.put("unaryExpr1", new ArrayList<IToken.Kind>(
                Arrays.asList(IToken.Kind.BANG, IToken.Kind.MINUS, IToken.Kind.COLOR_OP, IToken.Kind.IMAGE_OP)
        ));

        firstSets.put("unaryExprPostfix", new ArrayList<IToken.Kind>(
                Arrays.asList(IToken.Kind.BOOLEAN_LIT, IToken.Kind.STRING_LIT, IToken.Kind.INT_LIT, IToken.Kind.FLOAT_LIT, IToken.Kind.IDENT, IToken.Kind.LPAREN, IToken.Kind.COLOR_CONST, IToken.Kind.KW_CONSOLE,
                        IToken.Kind.LANGLE)
        ));
    }

    @Override
    public ASTNode parse() throws PLCException {
        ASTNode ret = null;
        tokens = getLexer(input);
        current = tokens.next();
        if(predictSets.get("Program").contains(current.getKind())){
            ret =  programAst();
        }
        else{
            throw new SyntaxException("Expected string or int or float or boolean or color or image or void not: " + current.getKind().name());
        }
        if(current.getKind()!= IToken.Kind.EOF){
            throw new SyntaxException("Unexpected Token: " + current.getKind().name() + " at end of program.");
        }
        return ret;
    }

    public IToken match(IToken.Kind check) throws PLCException{
        if(current.getKind() == check){
            IToken ret = current;
            current = tokens.next();
            return  ret;
        }
        else{
            throw new SyntaxException("Expected Token: " + check.name() + " Received: " + current.getKind().name());
        }
    }

    public void consume() throws PLCException{
        current = tokens.next();
    }

    public Program programAst() throws PLCException{
        IToken first = current;
        Type a = Type.toType(current.getText());
        ArrayList<NameDef> params = new ArrayList<NameDef>();
        ArrayList<ASTNode> decsAndStatements = new ArrayList<ASTNode>();
        consume();
        String name = match(IToken.Kind.IDENT).getText();
        match(IToken.Kind.LPAREN);
        if(firstSets.get("NameDef").contains(current.getKind())){
            params.add(nameDefAst());
            while(current.getKind() == IToken.Kind.COMMA){
                consume();
                params.add(nameDefAst());
            }
        }
        match(IToken.Kind.RPAREN);
        while(firstSets.get("Declaration").contains(current.getKind()) || firstSets.get("Statement").contains(current.getKind())){
            if(firstSets.get("Declaration").contains(current.getKind())){
                decsAndStatements.add(declarationAst());
            }
            else{
                decsAndStatements.add(statementAst());
            }
            match(IToken.Kind.SEMI);
        }
        return new Program(first, a, name, params, decsAndStatements);
    }

    public NameDef nameDefAst() throws PLCException{
        IToken first = current;
        IToken name = null;
        Dimension x = null;
        NameDef a = null;
        consume();
        if(current.getKind() == IToken.Kind.IDENT){
            name = current;
            consume();
            a = new NameDef(first, first, name);
        }
        else if(firstSets.get("Dimension").contains(current.getKind())){
            x = dimensionAst();
            name = match(IToken.Kind.IDENT);
            a = new NameDefWithDim(first, first, name, x);
        }
        else{
            throw new SyntaxException("Expected IDENT or [ not: " + current.getKind().name());
        }
        return a;
    }

    public Declaration declarationAst() throws PLCException{
        IToken first = current;
        NameDef nameDef = null;
        IToken op = null;
        Expr expr = null;
        nameDef = nameDefAst();
        if(current.getKind() == IToken.Kind.ASSIGN || current.getKind() == IToken.Kind.LARROW){
            op = current;
            consume();
            expr = exprAst();
        }
        return new VarDeclaration(first, nameDef, op, expr);
    }

    public Expr exprAst() throws PLCException{
        IToken first = current;
        Expr a = null;
        if(firstSets.get("ConditionalExpr").contains(current.getKind())){
            a = conditionalExprAst();
        }
        else if(firstSets.get("LogicalOrExpr").contains(current.getKind())){
            a = LogicalOrExprAst();
        }
        else{
            throw new SyntaxException("Expected if or ! or - or COLOR_OP or IMAGE_OP or BOOLEAN or STRING or INT or FLOAT or IDENT or ( not: " + current.getKind().name());
        }
        return a;
    }

    public Expr conditionalExprAst() throws PLCException{
        IToken first = current;
        Expr a = null;
        Expr b = null;
        Expr c = null;
        consume();
        match(IToken.Kind.LPAREN);
        a = exprAst();
        match(IToken.Kind.RPAREN);
        b = exprAst();
        match(IToken.Kind.KW_ELSE);
        c = exprAst();
        match(IToken.Kind.KW_FI);
        return new ConditionalExpr(first, a, b ,c);
    }

    public Expr LogicalOrExprAst() throws PLCException{
        IToken first = current;
        Expr left = null;
        Expr right = null;
        left = LogicalAndExprAst();
        while(current.getKind() == IToken.Kind.OR){
            IToken op = current;
            consume();
            right = LogicalAndExprAst();
            left = new BinaryExpr(first, left, op, right);
        }
        return left;
    }

    public Expr LogicalAndExprAst() throws PLCException{
        IToken first = current;
        Expr left = null;
        Expr right = null;
        left = comparisonExprAst();
        while(current.getKind() == IToken.Kind.AND){
            IToken op = current;
            consume();
            right = comparisonExprAst();
            left = new BinaryExpr(first, left, op, right);
        }
        return left;
    }

    public Expr comparisonExprAst() throws PLCException{
        IToken first = current;
        Expr left = null;
        Expr right = null;
        left = additiveExprAst();
        while(firstSets.get("comparisonExprWhile").contains(current.getKind())) {
            IToken op = current;
            consume();
            right = additiveExprAst();
            left = new BinaryExpr(first, left, op, right);
        }
        return left;
    }

    public Expr additiveExprAst() throws PLCException{
        IToken first = current;
        Expr left = null;
        Expr right = null;
        left = multiplicativeExprAst();
        while(firstSets.get("additiveExprWhile").contains(current.getKind())) {
            IToken op = current;
            consume();
            right = multiplicativeExprAst();
            left = new BinaryExpr(first, left, op, right);
        }
        return left;
    }

    public Expr multiplicativeExprAst() throws PLCException{
        IToken first = current;
        Expr left = null;
        Expr right = null;
        left = unaryExprAst();
        while(firstSets.get("multiplicativeExprWhile").contains(current.getKind())) {
            IToken op = current;
            consume();
            right = unaryExprAst();
            left = new BinaryExpr(first, left, op, right);
        }
        return left;
    }

    public Expr unaryExprAst() throws PLCException{
        IToken first = current;
        Expr a = null;
        if(firstSets.get("unaryExpr1").contains(current.getKind())){
            IToken op = current;
            consume();
            Expr b = unaryExprAst();
            a = new UnaryExpr(first, op, b);
        }
        else if(firstSets.get("unaryExprPostfix").contains(current.getKind())){
            a = unaryExprPostfixAst();
        }
        else{
            throw new SyntaxException("Expected ! or - or COLOR_OP or IMAGE_OP or BOOLEAN or STRING or INT or FLOAT or IDENT or ( not: " + current.getKind().name());
        }
        return a;
    }

    public Expr unaryExprPostfixAst() throws PLCException{
        IToken first = current;
        Expr a = primaryExprAst();
        PixelSelector b = null;
        if(current.getKind() == IToken.Kind.LSQUARE){
            b = pixelSelectorAst();
            return new UnaryExprPostfix(first, a , b);
        }
        return a;
    }

    public Expr primaryExprAst() throws PLCException{
        IToken first = current;
        Expr a, b, c = null;
        if (current.getKind() == IToken.Kind.BOOLEAN_LIT) {
            a = new BooleanLitExpr(first);
            consume();
        } else if (current.getKind() == IToken.Kind.STRING_LIT) {
            a = new StringLitExpr(first);
            consume();
        } else if (current.getKind() == IToken.Kind.INT_LIT) {
            a = new IntLitExpr(first);
            consume();
        } else if (current.getKind() == IToken.Kind.FLOAT_LIT) {
            a = new FloatLitExpr(first);
            consume();
        } else if (current.getKind() == IToken.Kind.IDENT) {
            a = new IdentExpr(first);
            consume();
        } else if (current.getKind() == IToken.Kind.LPAREN) {
            consume();
            a = exprAst();
            match(IToken.Kind.RPAREN);
        } else if (current.getKind() == IToken.Kind.COLOR_CONST) {
            consume();
            a = new ColorConstExpr(first);
        } else if (current.getKind() == IToken.Kind.LANGLE) {
            consume();
            a = exprAst();
            match(IToken.Kind.COMMA);
            b = exprAst();
            match(IToken.Kind.COMMA);
            c = exprAst();
            match(IToken.Kind.RANGLE);
            a = new ColorExpr(first, a, b, c);
        } else if (current.getKind() == IToken.Kind.KW_CONSOLE) {
            consume();
            a = new ConsoleExpr(first);
        } else {
            throw new SyntaxException("Expected BOOLEAN or STRING or INT or FLOAT or IDENT or ( or ColorConst or << or console not: " + current.getKind().name());
        }
        return a;
    }

    public PixelSelector pixelSelectorAst() throws PLCException{
        IToken first = current;
        Expr a = null;
        Expr b = null;
        consume();
        a = exprAst();
        match(IToken.Kind.COMMA);
        b = exprAst();
        match(IToken.Kind.RSQUARE);
        return new PixelSelector(first, a, b);
    }

    public Dimension dimensionAst() throws PLCException{
        IToken first = current;
        Expr a = null;
        Expr b = null;
        consume();
        a = exprAst();
        match(IToken.Kind.COMMA);
        b = exprAst();
        match(IToken.Kind.RSQUARE);
        return new Dimension(first, a, b);
    }

    public Statement statementAst() throws PLCException{
        IToken first = current;
        String name = "";
        PixelSelector selector = null;
        Expr expr, expr1 = null;
        Statement a = null;
        if(current.getKind() == IToken.Kind.IDENT){
            name = current.getText();
            consume();
            if(current.getKind() == IToken.Kind.LSQUARE){
                selector = pixelSelectorAst();
            }
            if(current.getKind() == IToken.Kind.ASSIGN){
                consume();
                expr = exprAst();
                a = new AssignmentStatement(first, name, selector, expr);
            }
            else if(current.getKind() == IToken.Kind.LARROW){
                consume();
                expr = exprAst();
                a = new ReadStatement(first, name, selector, expr);
            }
            else{
                throw new SyntaxException("Expected = or <- not: " + current.getKind().name());
            }
        }
        else if(current.getKind() == IToken.Kind.KW_WRITE){
            consume();
            expr = exprAst();
            match(IToken.Kind.RARROW);
            expr1 = exprAst();
            a = new WriteStatement(first, expr, expr1);
        }
        else{
            consume();
            expr = exprAst();
            a = new ReturnStatement(first, expr);
        }
        return a;
    }
}
