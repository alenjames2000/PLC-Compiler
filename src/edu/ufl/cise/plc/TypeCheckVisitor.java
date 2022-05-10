package edu.ufl.cise.plc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.ASTVisitor;
import edu.ufl.cise.plc.ast.AssignmentStatement;
import edu.ufl.cise.plc.ast.BinaryExpr;
import edu.ufl.cise.plc.ast.BooleanLitExpr;
import edu.ufl.cise.plc.ast.ColorConstExpr;
import edu.ufl.cise.plc.ast.ColorExpr;
import edu.ufl.cise.plc.ast.ConditionalExpr;
import edu.ufl.cise.plc.ast.ConsoleExpr;
import edu.ufl.cise.plc.ast.Declaration;
import edu.ufl.cise.plc.ast.Dimension;
import edu.ufl.cise.plc.ast.Expr;
import edu.ufl.cise.plc.ast.FloatLitExpr;
import edu.ufl.cise.plc.ast.IdentExpr;
import edu.ufl.cise.plc.ast.IntLitExpr;
import edu.ufl.cise.plc.ast.NameDef;
import edu.ufl.cise.plc.ast.NameDefWithDim;
import edu.ufl.cise.plc.ast.PixelSelector;
import edu.ufl.cise.plc.ast.Program;
import edu.ufl.cise.plc.ast.ReadStatement;
import edu.ufl.cise.plc.ast.ReturnStatement;
import edu.ufl.cise.plc.ast.StringLitExpr;
import edu.ufl.cise.plc.ast.Types.Type;
import edu.ufl.cise.plc.ast.UnaryExpr;
import edu.ufl.cise.plc.ast.UnaryExprPostfix;
import edu.ufl.cise.plc.ast.VarDeclaration;
import edu.ufl.cise.plc.ast.WriteStatement;

import static edu.ufl.cise.plc.ast.Types.Type.*;

public class TypeCheckVisitor implements ASTVisitor {

	SymbolTable symbolTable = new SymbolTable();  
	Program root;
	
	record Pair<T0,T1>(T0 t0, T1 t1){};  //may be useful for constructing lookup tables.
	
	private void check(boolean condition, ASTNode node, String message) throws TypeCheckException {
		if (!condition) {
			throw new TypeCheckException(message, node.getSourceLoc());
		}
	}
	
	//The type of a BooleanLitExpr is always BOOLEAN.  
	//Set the type in AST Node for later passes (code generation)
	//Return the type for convenience in this visitor.  
	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception {
		booleanLitExpr.setType(Type.BOOLEAN);
		return Type.BOOLEAN;
	}

	@Override
	public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception {
		stringLitExpr.setType(STRING);
		return STRING;
	}

	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
		intLitExpr.setType(INT);
		return INT;
	}

	@Override
	public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
		floatLitExpr.setType(Type.FLOAT);
		return Type.FLOAT;
	}

	@Override
	public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
		colorConstExpr.setType(COLOR);
		return COLOR;
	}

	@Override
	public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
		consoleExpr.setType(Type.CONSOLE);
		return Type.CONSOLE;
	}
	
	//Visits the child expressions to get their type (and ensure they are correctly typed)
	//then checks the given conditions.
	@Override
	public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
		Type redType = (Type) colorExpr.getRed().visit(this, arg);
		Type greenType = (Type) colorExpr.getGreen().visit(this, arg);
		Type blueType = (Type) colorExpr.getBlue().visit(this, arg);
		check(redType == greenType && redType == blueType, colorExpr, "color components must have same type");
		check(redType == Type.INT || redType == Type.FLOAT, colorExpr, "color component type must be int or float");
		Type exprType = (redType == Type.INT) ? Type.COLOR : Type.COLORFLOAT;
		colorExpr.setType(exprType);
		return exprType;
	}	

	
	
	//Maps forms a lookup table that maps an operator expression pair into result type.  
	//This more convenient than a long chain of if-else statements. 
	//Given combinations are legal; if the operator expression pair is not in the map, it is an error. 
	Map<Pair<Kind,Type>, Type> unaryExprs = Map.of(
			new Pair<Kind,Type>(Kind.BANG,BOOLEAN), BOOLEAN,
			new Pair<Kind,Type>(Kind.MINUS, FLOAT), FLOAT,
			new Pair<Kind,Type>(Kind.MINUS, INT),INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,INT), INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,COLOR), INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,IMAGE), IMAGE,
			new Pair<Kind,Type>(Kind.IMAGE_OP,IMAGE), INT
			);
	
	//Visits the child expression to get the type, then uses the above table to determine the result type
	//and check that this node represents a legal combination of operator and expression type. 
	@Override
	public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws Exception {
		// !, -, getRed, getGreen, getBlue
		Kind op = unaryExpr.getOp().getKind();
		Type exprType = (Type) unaryExpr.getExpr().visit(this, arg);
		//Use the lookup table above to both check for a legal combination of operator and expression, and to get result type.
		Type resultType = unaryExprs.get(new Pair<Kind,Type>(op,exprType));
		check(resultType != null, unaryExpr, "incompatible types for unaryExpr");
		//Save the type of the unary expression in the AST node for use in code generation later. 
		unaryExpr.setType(resultType);
		//return the type for convenience in this visitor.
		return resultType;
	}

	Map<Pair<Type,Type>, Type> binExprsPlusMinus = Map.of(
			new Pair<Type,Type>(INT, INT), INT,
			new Pair<Type,Type>(FLOAT, FLOAT), FLOAT,
			new Pair<Type,Type>(INT, FLOAT), FLOAT,
			new Pair<Type,Type>(FLOAT, INT), FLOAT,
			new Pair<Type,Type>(COLOR,COLOR), COLOR,
			new Pair<Type,Type>(COLORFLOAT, COLORFLOAT), COLORFLOAT,
			new Pair<Type,Type>(COLORFLOAT, COLOR), COLORFLOAT,
			new Pair<Type,Type>(COLOR, COLORFLOAT), COLORFLOAT,
			new Pair<Type,Type>(IMAGE, IMAGE), IMAGE
	);

	Map<Pair<Type,Type>, Type> binExprsTimesDivMod = Map.of(
			new Pair<Type,Type>(IMAGE, INT), IMAGE,
			new Pair<Type,Type>(IMAGE, FLOAT), IMAGE,
			new Pair<Type,Type>(INT, COLOR), COLOR,
			new Pair<Type,Type>(COLOR, INT), COLOR,
			new Pair<Type,Type>(FLOAT ,COLOR), COLORFLOAT,
			new Pair<Type,Type>(COLOR, FLOAT), COLORFLOAT
			);

	Map<Pair<Type,Type>, Type> binExprsComp = Map.of(
			new Pair<Type,Type>(INT, INT), BOOLEAN,
			new Pair<Type,Type>(FLOAT, FLOAT), BOOLEAN,
			new Pair<Type,Type>(INT, FLOAT), BOOLEAN,
			new Pair<Type,Type>(FLOAT, INT), BOOLEAN
	);

	//This method has several cases. Work incrementally and test as you go. 
	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
		Kind op = binaryExpr.getOp().getKind();
		Type left = (Type) binaryExpr.getLeft().visit(this, arg);
		Type right = (Type) binaryExpr.getRight().visit(this, arg);

		Type resulType = switch (op){
			case AND, OR -> {
				check(left == right && left == BOOLEAN, binaryExpr, "Incompatible type for Binary " + op.name() + " Expression");
				yield BOOLEAN;
			}

			case EQUALS, NOT_EQUALS -> {
				check(left == right, binaryExpr, "Incompatible type for Binary " + op.name() + " Expression");
				yield BOOLEAN;
			}

			case PLUS, MINUS -> {
				Type ret = binExprsPlusMinus.get(new Pair<Type, Type>(left, right));

				check(ret != null, binaryExpr, "Incompatible type for Binary " + op.name() + " Expression");
				if(left == INT && right == FLOAT){
					binaryExpr.getLeft().setCoerceTo(FLOAT);
				}
				else if(left == FLOAT && right == INT){
					binaryExpr.getRight().setCoerceTo(FLOAT);
				}
				else if(left == COLORFLOAT && right == COLOR){
					binaryExpr.getRight().setCoerceTo(COLORFLOAT);
				}
				else if(left == COLOR && right == COLORFLOAT){
					binaryExpr.getLeft().setCoerceTo(COLORFLOAT);
				}
				yield ret;
			}

			case TIMES, DIV, MOD -> {
				Type ret = binExprsTimesDivMod.get(new Pair<Type, Type>(left, right));
				if(ret == null){
					ret = binExprsPlusMinus.get(new Pair<Type, Type>(left, right));
				}
				check(ret != null, binaryExpr, "Incompatible type for Binary " + op.name() + " Expression");
				if(left == INT && right == COLOR){
					binaryExpr.getLeft().setCoerceTo(COLOR);
				}
				else if(left == COLOR && right == INT){
					binaryExpr.getRight().setCoerceTo(COLOR);
				}
				else if(left == FLOAT && right == COLOR){
					binaryExpr.getRight().setCoerceTo(COLORFLOAT);
					binaryExpr.getLeft().setCoerceTo(COLORFLOAT);
				}
				else if(left == COLOR && right == FLOAT){
					binaryExpr.getLeft().setCoerceTo(COLORFLOAT);
					binaryExpr.getRight().setCoerceTo(COLORFLOAT);
				}
				else if(left == INT && right == FLOAT){
					binaryExpr.getLeft().setCoerceTo(FLOAT);
				}
				else if(left == FLOAT && right == INT){
					binaryExpr.getRight().setCoerceTo(FLOAT);
				}
				else if(left == COLORFLOAT && right == COLOR){
					binaryExpr.getRight().setCoerceTo(COLORFLOAT);
				}
				else if(left == COLOR && right == COLORFLOAT){
					binaryExpr.getLeft().setCoerceTo(COLORFLOAT);
				}
				yield ret;
			}

			case LT, LE, GT, GE -> {
				Type ret = binExprsComp.get(new Pair<Type, Type>(left, right));
				check(ret != null, binaryExpr, "Incompatible type for Binary " + op.name() + " Expression");
				if(left == INT && right == FLOAT){
					binaryExpr.getLeft().setCoerceTo(FLOAT);
				}
				else if(left == FLOAT && right == INT){
					binaryExpr.getRight().setCoerceTo(FLOAT);
				}
				yield ret;
			}

			default -> throw new SyntaxException("Unexpected Operator");
		};

		binaryExpr.setType(resulType);
		return resulType;
	}

	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
		Declaration dec = symbolTable.lookup(identExpr.getText());
		check(dec != null, identExpr, "Identifier is not declared");
		check(dec.isInitialized(), identExpr, "Identifier is not initialized");
		identExpr.setType(dec.getType());
		identExpr.setDec(dec);
		return dec.getType();
	}

	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
		Type exp = (Type) conditionalExpr.getCondition().visit(this ,arg);
		Type trueCase = (Type) conditionalExpr.getTrueCase().visit(this, arg);
		Type falseCase = (Type) conditionalExpr.getFalseCase().visit(this, arg);
		check(exp == BOOLEAN, conditionalExpr, "Condition is not of type boolean");
		check(falseCase == trueCase, conditionalExpr, "False case and True case need same types");
		conditionalExpr.setType(trueCase);
		return trueCase;
	}

	@Override
	public Object visitDimension(Dimension dimension, Object arg) throws Exception {
		Type xType = (Type) dimension.getWidth().visit(this, arg);
		check(xType == Type.INT, dimension.getWidth(), "only ints as dimension components");
		Type yType = (Type) dimension.getHeight().visit(this, arg);
		check(yType == Type.INT, dimension.getHeight(), "only ints as dimension components");
		return null;
	}

	@Override
	//This method can only be used to check PixelSelector objects on the right hand side of an assignment. 
	//Either modify to pass in context info and add code to handle both cases, or when on left side
	//of assignment, check fields from parent assignment statement.
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		Type xType = (Type) pixelSelector.getX().visit(this, arg);
		check(xType == Type.INT, pixelSelector.getX(), "only ints as pixel selector components");
		Type yType = (Type) pixelSelector.getY().visit(this, arg);
		check(yType == Type.INT, pixelSelector.getY(), "only ints as pixel selector components");
		return null;
	}

	Map<Pair<Type,Type>, Type> assignStateNotImage = Map.of(
			new Pair<Type,Type>(INT, FLOAT), INT,
			new Pair<Type,Type>(FLOAT, INT), FLOAT,
			new Pair<Type,Type>(INT, COLOR), INT,
			new Pair<Type,Type>(COLOR, INT), COLOR
	);

	Map<Pair<Type,Type>, Type> assignStateImageNoPix = Map.of(
			new Pair<Type,Type>(IMAGE, INT), COLOR,
			new Pair<Type,Type>(IMAGE, FLOAT), COLORFLOAT,
			new Pair<Type,Type>(IMAGE, COLOR), COLOR,
			new Pair<Type,Type>(IMAGE, COLORFLOAT), COLORFLOAT
	);

	private Declaration createDec(IToken first){
		NameDef x = new NameDef(first, "int", first.getText());
		return new VarDeclaration(first, x, null, null);
	}

	@Override
	//This method several cases--you don't have to implement them all at once.
	//Work incrementally and systematically, testing as you go.  
	public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
		Declaration dec = symbolTable.lookup(assignmentStatement.getName());
		check(dec != null, assignmentStatement, "Identifier is not declared before being initialized");
		PixelSelector pix = assignmentStatement.getSelector();
		Type target = dec.getType();
		dec.setInitialized(true);

		if(target != IMAGE){
			check(pix == null, assignmentStatement, "Assignment to non-image cannot have pixel selector");
			Type expr = (Type) assignmentStatement.getExpr().visit(this, arg);
			if(target == expr){
				assignmentStatement.setTargetDec(dec);
			}
			else{
				Type coerce = assignStateNotImage.get(new Pair<Type, Type>(target, expr));
				check(coerce != null, assignmentStatement, "Incompatible types for assignment statement with no image");
				assignmentStatement.setTargetDec(dec);
				assignmentStatement.getExpr().setCoerceTo(coerce);
			}
		}
		else if(pix == null){
			Type expr = (Type) assignmentStatement.getExpr().visit(this, arg);
			if(target == expr){
				assignmentStatement.setTargetDec(dec);
			}
			else{
				Type coerce = assignStateImageNoPix.get(new Pair<Type, Type>(target, expr));
				check(coerce != null, assignmentStatement, "Incompatible types for assignment statement with image and no pixel selector");
				assignmentStatement.setTargetDec(dec);
				assignmentStatement.getExpr().setCoerceTo(coerce);
			}
		}
		else{
			check(pix.getX() instanceof IdentExpr && pix.getY() instanceof IdentExpr, assignmentStatement, "Pixel selector expression should be identifiers");
			IToken left = pix.getX().getFirstToken();
			IToken right = pix.getY().getFirstToken();
			Declaration x = createDec(left);
			x.setInitialized(true);
			check(!symbolTable.insertEntry(left.getText(), x), assignmentStatement, "First Identifier for pixel selector previously declared");
			Declaration y = createDec(left);
			y.setInitialized(true);
			check(!symbolTable.insertEntry(right.getText(), y), assignmentStatement, "Second Identifier for pixel selector previously declared");
			assignmentStatement.getSelector().visit(this, arg);
			Type expr = (Type) assignmentStatement.getExpr().visit(this, arg);
			check(expr == COLOR || expr == COLORFLOAT || expr == FLOAT || expr == INT, assignmentStatement, "Incorrect type for left expression");
			assignmentStatement.getExpr().setCoerceTo(COLOR);
			assignmentStatement.setTargetDec(dec);
			symbolTable.remove(left.getText());
			symbolTable.remove(right.getText());
		}
		return null;
	}


	@Override
	public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
		Type sourceType = (Type) writeStatement.getSource().visit(this, arg);
		Type destType = (Type) writeStatement.getDest().visit(this, arg);
		check(destType == Type.STRING || destType == Type.CONSOLE, writeStatement,
				"illegal destination type for write");
		check(sourceType != Type.CONSOLE, writeStatement, "illegal source type for write");
		return null;
	}

	@Override
	public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
		Declaration dec = symbolTable.lookup(readStatement.getName());
		Type target = dec.getType();
		PixelSelector pix = readStatement.getSelector();
		Type expr = (Type) readStatement.getSource().visit(this, arg);

		check(dec != null, readStatement, "Identifier is not declared");
		check(pix == null, readStatement, "Read statement cannot have pixel selector");
		check(expr == CONSOLE || expr == STRING, readStatement, "Expression must be CONSOLE or STRING");
		if(readStatement.getSource() instanceof ConsoleExpr){
			if(dec.getType() == IMAGE){
				readStatement.getSource().setCoerceTo(STRING);
			}
			else{
				readStatement.getSource().setCoerceTo(target);
			}
		}
		dec.setInitialized(true);
		readStatement.setTargetDec(dec);
		return null;
	}

	@Override
	public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
		Type target = declaration.getNameDef().getType();
		Expr exp = declaration.getExpr();
		IToken op = declaration.getOp();

		if(target == IMAGE){
			check(exp != null || declaration.getNameDef().getDim() != null, declaration, "Image identifiers require dimension or initializer");
			if(declaration.getNameDef().getDim() != null){
				declaration.setInitialized(false);
			}
			if(exp != null){
				Type expr = (Type) declaration.getExpr().visit(this, arg);
				if(op.getKind() == Kind.ASSIGN){
					if(declaration.getNameDef().getDim() != null){
						check(target == expr || expr == COLOR || expr == COLORFLOAT || expr == INT || expr == FLOAT, declaration, "Assignment to image must be of type image");
						if(target != expr){
							exp.setCoerceTo(assignStateImageNoPix.get(new Pair<Type, Type>(target, expr)));
						}
					}
					else {
						check(target == expr, declaration, "Assignment to image must be of type image");
					}
				}
				else{
					check(expr == CONSOLE || expr == STRING, declaration, "Expression must be CONSOLE or STRING");
					exp.setCoerceTo(STRING);
				}
				declaration.setInitialized(true);
			}
			declaration.getNameDef().visit(this, declaration);
		}
		else if(exp != null){
			Type expr = (Type) declaration.getExpr().visit(this, arg);
			if(op.getKind() == Kind.ASSIGN){
				if(target == expr){
					declaration.setInitialized(true);
					declaration.getNameDef().visit(this, declaration);
				}
				else{
					Type coerce = assignStateNotImage.get(new Pair<Type, Type>(target, expr));
					check(coerce != null, declaration, "Incompatible types for assignment statement with no image");
					declaration.setInitialized(true);
					declaration.getNameDef().visit(this, declaration);
					declaration.getExpr().setCoerceTo(coerce);
				}
			}
			else{
				check(expr == CONSOLE || expr == STRING, declaration, "Expression must be CONSOLE or STRING");
				if(expr == CONSOLE){
					exp.setCoerceTo(target);
				}
				declaration.setInitialized(true);
				declaration.getNameDef().visit(this, declaration);
			}
		}
		else{
			declaration.setInitialized(false);
			declaration.getNameDef().visit(this, declaration);
		}
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {		
		//TODO:  this method is incomplete, finish it.  
		
		//Save root of AST so return type can be accessed in return statements
		root = program;

		symbolTable.insertEntry(program.getName(), null);

		//params
		List<NameDef> params = program.getParams();
		for(NameDef name : program.getParams()){
			Declaration x = new VarDeclaration(name.getFirstToken(), name, null, null);
			x.setInitialized(true);
			name.visit(this, x);
		}

		//Check declarations and statements
		List<ASTNode> decsAndStatements = program.getDecsAndStatements();
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}
		return program;
	}

	@Override
	public Object visitNameDef(NameDef nameDef, Object arg) throws Exception {
		check(!symbolTable.insertEntry(nameDef.getName(), (Declaration) arg), nameDef, "Identifier already declared before");
		return null;
	}

	@Override
	public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
		nameDefWithDim.getDim().visit(this, arg);
		check(!symbolTable.insertEntry(nameDefWithDim.getName(), (Declaration) arg), nameDefWithDim, "Identifier already declared before");
		return null;
	}
 
	@Override
	public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception {
		Type returnType = root.getReturnType();  //This is why we save program in visitProgram.
		Type expressionType = (Type) returnStatement.getExpr().visit(this, arg);
		check(returnType == expressionType, returnStatement, "return statement with invalid type");
		return null;
	}

	@Override
	public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
		Type expType = (Type) unaryExprPostfix.getExpr().visit(this, arg);
		check(expType == Type.IMAGE, unaryExprPostfix, "pixel selector can only be applied to image");
		unaryExprPostfix.getSelector().visit(this, arg);
		unaryExprPostfix.setType(Type.INT);
		unaryExprPostfix.setCoerceTo(COLOR);
		return Type.COLOR;
	}

}
