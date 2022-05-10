package edu.ufl.cise.plc;

import java.util.ArrayList;
import java.util.List;

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
import edu.ufl.cise.plc.runtime.ColorTuple;
import edu.ufl.cise.plc.runtime.ImageOpsAdd;


import static edu.ufl.cise.plc.ast.Types.Type.*;

public class CodeGenVisitor implements ASTVisitor {

    StringBuilder programString = new StringBuilder();
    String Name;
    List<String> imports = new ArrayList<>();
    int tabs = 1;

    public CodeGenVisitor(String packageName){
        Name = packageName;
    }

    public String toStringFromType(Type s) {
        return switch(s) {
            case BOOLEAN -> "boolean";
            case COLOR -> "ColorTuple";
            case CONSOLE -> "console";
            case FLOAT -> "float";
            case IMAGE -> "BufferedImage";
            case INT -> "int";
            case STRING -> "String";
            case VOID -> "void";
            default -> throw new IllegalArgumentException("Unexpected type value: " + s);
        };
    }

    public String toStringFromColorOp(String s){
        return switch (s){
            case "getRed" -> "ImageOps.extractRed(";
            case "getGreen" -> "ImageOps.extractGreen(";
            case "getBlue" -> "ImageOps.extractBlue(";
            default -> throw new IllegalArgumentException("Unexpected Color Op: " + s);
        };
    }

    public String toStringFromColorOpC(String s){
        return switch (s){
            case "getRed" -> "ColorTuple.getRed(";
            case "getGreen" -> "ColorTuple.getGreen(";
            case "getBlue" -> "ColorTuple.getBlue(";
            default -> throw new IllegalArgumentException("Unexpected Color Op: " + s);
        };
    }

    public String toImageOpFromKind(IToken.Kind s){
        return switch(s){
            case EQUALS -> "ImageOps.BoolOP.EQUALS,";
            case NOT_EQUALS -> "ImageOps.BoolOP.NOT_EQUALS,";
            case PLUS -> "ImageOps.OP.PLUS,";
            case MINUS -> "ImageOps.OP.MINUS,";
            case TIMES -> "ImageOps.OP.TIMES,";
            case DIV -> "ImageOps.OP.DIV,";
            case MOD -> "ImageOps.OP.MOD,";
            default -> throw new IllegalArgumentException("Unexpected Binary Operator for Colors" + s);
        };
    }

    public String toParseFromType(Type s){
        return switch(s){
            case BOOLEAN -> "(Boolean)FileURLIO.readValueFromFile(";
            case FLOAT -> "(Float)FileURLIO.readValueFromFile(";
            case INT -> "(Integer)FileURLIO.readValueFromFile(";
            case STRING -> "(String)FileURLIO.readValueFromFile(";
            case COLOR -> "(ColorTuple)FileURLIO.readValueFromFile(";
            default -> throw new IllegalArgumentException("Unexpected Binary Operator for Colors" + s);
        };
    }

    public String toBoxedFromType(Type s) {
        return switch(s) {
            case BOOLEAN -> "Boolean";
            case FLOAT -> "Float";
            case INT -> "Integer";
            case STRING -> "String";
            case COLOR -> "ColorTuple";
            default -> throw new IllegalArgumentException("Unexpected type value: " + s);
        };
    }

    public String toPromptFromType(Type s) {
        return switch(s) {
            case BOOLEAN -> "\"Enter Boolean: \"";
            case FLOAT -> "\"Enter Float: \"";
            case INT -> "\"Enter Integer: \"";
            case STRING -> "\"Enter String: \"";
            case COLOR -> "\"Enter Color: \"";
            default -> throw new IllegalArgumentException("Unexpected type value: " + s);
        };
    }

    public CodeGenVisitor addSemi(){
        programString.append(";");
        return this;
    }

    public CodeGenVisitor addComma(){
        programString.append(",");
        return this;
    }

    public CodeGenVisitor addSpace(){
        programString.append(" ");
        return this;
    }

    public CodeGenVisitor addTab(boolean increment){
        for(int i=0; i < tabs; i++) {
            programString.append("\t");
        }
        if(increment) {
            tabs++;
        }
        return this;
    }

    public CodeGenVisitor addNewLine(){
        programString.append("\n");
        return this;
    }

    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception{
        if(booleanLitExpr.getValue()){
            programString.append("true");
        }
        else{
            programString.append("false");
        }

        return programString;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception{
        programString.append("\"\"\"\n");
        //addTab(false);
        programString.append(stringLitExpr.getValue()).append("\"\"\"");

        return programString;
    }

    @Override
    public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception{
        if(intLitExpr.getCoerceTo() != null && intLitExpr.getCoerceTo()!= INT){
            if(intLitExpr.getCoerceTo() == COLOR){
                programString.append("new ColorTuple(").append(intLitExpr.getText()).append(")");
                if(!(imports.contains("import edu.ufl.cise.plc.runtime.ColorTuple;\n"))){
                    imports.add("import edu.ufl.cise.plc.runtime.ColorTuple;\n");
                }
            }
            else {
                programString.append("(").append(toStringFromType(intLitExpr.getCoerceTo())).append(")").append(intLitExpr.getText());
            }
        }
        else {
            programString.append(intLitExpr.getText());
        }

        return programString;
    }

    @Override
    public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception{
        if(floatLitExpr.getCoerceTo() != null && floatLitExpr.getCoerceTo()!= FLOAT){
            if(floatLitExpr.getCoerceTo() == COLORFLOAT){
                programString.append("new ColorTupleFloat(").append(floatLitExpr.getText()).append("f").append(")");
                if(!(imports.contains("import edu.ufl.cise.plc.runtime.ColorTupleFloat;\n"))){
                    imports.add("import edu.ufl.cise.plc.runtime.ColorTupleFloat;\n");
                }
            }
            else{
                programString.append("(").append(toStringFromType(floatLitExpr.getCoerceTo())).append(")").append(floatLitExpr.getText()).append("f");
            }
        }
        else {
            programString.append(floatLitExpr.getText()+"f");
        }

        return programString;
    }

    @Override
    public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception{
        if(!(imports.contains("import edu.ufl.cise.plc.runtime.ColorTuple;\n"))){
            imports.add("import edu.ufl.cise.plc.runtime.ColorTuple;\n");
        }
        if(!(imports.contains("import java.awt.Color;\n"))){
            imports.add("import java.awt.Color;\n");
        }
        if(colorConstExpr.getCoerceTo() == COLORFLOAT){
            programString.append("new ColorTupleFloat(");
            if(!(imports.contains("import edu.ufl.cise.plc.runtime.ColorTupleFloat;\n"))){
                imports.add("import edu.ufl.cise.plc.runtime.ColorTupleFloat;\n");
            }
        }
        programString.append("(ColorTuple.unpack(Color.");
        programString.append(colorConstExpr.getFirstToken().getText());
        programString.append(".getRGB())");
        if(colorConstExpr.getCoerceTo() == INT){
            programString.append(".pack()");
        }
        if(colorConstExpr.getCoerceTo() == COLORFLOAT){
            programString.append(")");
        }
        programString.append(")");
        return programString;
    }

    @Override
    public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception{
        programString.append("(");
        programString.append(toBoxedFromType(consoleExpr.getCoerceTo())).append(")ConsoleIO.readValueFromConsole(\"").append(consoleExpr.getCoerceTo().name()).append("\", ").append(toPromptFromType(consoleExpr.getCoerceTo())).append(")");
        if(!(imports.contains("import edu.ufl.cise.plc.runtime.ConsoleIO;\n"))){
            imports.add("import edu.ufl.cise.plc.runtime.ConsoleIO;\n");
        }

        return programString;
    }

    @Override
    public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception{
        if(!(imports.contains("import edu.ufl.cise.plc.runtime.ColorTuple;\n"))){
            imports.add("import edu.ufl.cise.plc.runtime.ColorTuple;\n");
        }
        if(colorExpr.getCoerceTo() == COLORFLOAT || colorExpr.getType() == COLORFLOAT){
            programString.append("new ColorTupleFloat(");
            if(!(imports.contains("import edu.ufl.cise.plc.runtime.ColorTupleFloat;\n"))){
                imports.add("import edu.ufl.cise.plc.runtime.ColorTupleFloat;\n");
            }
        }
        if(colorExpr.getType() == COLOR){
            programString.append("new ColorTuple(");
        }
        colorExpr.getRed().visit(this, arg);
        programString.append(",");
        colorExpr.getGreen().visit(this, arg);
        programString.append(",");
        colorExpr.getBlue().visit(this, arg);
        programString.append(")");
        if((colorExpr.getCoerceTo() == COLORFLOAT || colorExpr.getType() == COLORFLOAT) && colorExpr.getType() == COLOR){
            programString.append(")");
        }
        if(colorExpr.getCoerceTo() == INT){
            programString.append(".pack()");
        }
        return programString;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpression, Object arg) throws Exception{
        boolean colorCoerce = unaryExpression.getCoerceTo() == COLOR;
        boolean colorCoerceF = unaryExpression.getCoerceTo() == COLORFLOAT;
        if(colorCoerce){
            programString.append("new ColorTuple(");
        }
        else if(colorCoerceF){
            programString.append("new ColorTupleFloat(");
        }
        else if(unaryExpression.getCoerceTo() != null){
            programString.append("(").append(toStringFromType(unaryExpression.getCoerceTo())).append(")");
        }

        if(unaryExpression.getOp().getKind() == Kind.MINUS || unaryExpression.getOp().getKind() == Kind.BANG){
            programString.append("(").append(unaryExpression.getOp().getText()).append(" ");
            unaryExpression.getExpr().visit(this, null);
            programString.append(")");
        }
        else{
            if(unaryExpression.getOp().getKind() == Kind.COLOR_OP){
                if(unaryExpression.getExpr().getType() == IMAGE) {
                    programString.append("(").append(toStringFromColorOp(unaryExpression.getOp().getText()));
                }
                else{
                    programString.append("(").append(toStringFromColorOpC(unaryExpression.getOp().getText()));
                }
                unaryExpression.getExpr().visit(this, arg);
                programString.append("))");
            }
            else{
                programString.append("(");
                unaryExpression.getExpr().visit(this, arg);
                programString.append(".").append(unaryExpression.getOp().getText()).append("())");
            }
        }
        if(colorCoerce || colorCoerceF){
            programString.append(")");
        }

        return  programString;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception{
        if(binaryExpr.getCoerceTo() != null && binaryExpr.getCoerceTo()!= binaryExpr.getType()){
            if(binaryExpr.getCoerceTo() == COLORFLOAT){
                programString.append("new ColorTupleFloat(");
                if(!(imports.contains("import edu.ufl.cise.plc.runtime.ColorTupleFloat;\n"))){
                    imports.add("import edu.ufl.cise.plc.runtime.ColorTupleFloat;\n");
                }
            }
            else if(binaryExpr.getCoerceTo() == COLOR){
                programString.append("new ColorTuple(");
                if(!(imports.contains("import edu.ufl.cise.plc.runtime.ColorTuple;\n"))){
                    imports.add("import edu.ufl.cise.plc.runtime.ColorTuple;\n");
                }
            }
            else {
                programString.append("(").append(toStringFromType(binaryExpr.getCoerceTo())).append(")");
            }
        }
        Type left = (Type) binaryExpr.getLeft().getType();
        Type right = (Type) binaryExpr.getRight().getType();
        Type leftC = (Type) binaryExpr.getLeft().getCoerceTo();
        Type rightC = (Type) binaryExpr.getRight().getCoerceTo();
        IToken op = binaryExpr.getOp();
        boolean b = (left == right && left == STRING) || (leftC == rightC && leftC == STRING);
        boolean stringOpEq = (b && op.getKind() == Kind.EQUALS);
        boolean stringOpNoEq = (b && op.getKind() == Kind.NOT_EQUALS);
        boolean colorOp = ((left == COLOR || right == COLOR) || (left == COLORFLOAT || right == COLORFLOAT) || (leftC == COLOR || rightC == COLOR) || (leftC == COLORFLOAT || rightC == COLORFLOAT));
        boolean imageOp = ((left == right && left == IMAGE) || (leftC == rightC && leftC == IMAGE));
        boolean imageIntOp = ((left == IMAGE && (right == INT || right == FLOAT)) || (leftC == IMAGE && (rightC == INT || rightC == FLOAT)));
        programString.append("(");
        if(colorOp){
            programString.append("ImageOps.binaryTupleOp(");
            programString.append(toImageOpFromKind(binaryExpr.getOp().getKind()));
            binaryExpr.getLeft().visit(this, arg);
            addComma();
            binaryExpr.getRight().visit(this, arg);
            programString.append(")");

            if(!(imports.contains("import edu.ufl.cise.plc.runtime.ImageOps;\n"))){
                imports.add("import edu.ufl.cise.plc.runtime.ImageOps;\n");
            }
        }
        else if(imageOp){
            if(binaryExpr.getOp().getKind() == Kind.EQUALS){
                programString.append("ImageOpsAdd.equals(");
                binaryExpr.getLeft().visit(this, arg);
                addComma();
                binaryExpr.getRight().visit(this, arg);
                programString.append(")");
                if(!(imports.contains("import edu.ufl.cise.plc.runtime.ImageOpsAdd;\n"))){
                    imports.add("import edu.ufl.cise.plc.runtime.ImageOpsAdd;\n");
                }
            }
            else if(binaryExpr.getOp().getKind() == Kind.NOT_EQUALS){
                programString.append("!(ImageOpsAdd.equals(");
                binaryExpr.getLeft().visit(this, arg);
                addComma();
                binaryExpr.getRight().visit(this, arg);
                programString.append("))");
                if(!(imports.contains("import edu.ufl.cise.plc.runtime.ImageOpsAdd;\n"))){
                    imports.add("import edu.ufl.cise.plc.runtime.ImageOpsAdd;\n");
                }
            }
            else{
                programString.append("ImageOps.binaryImageImageOp(");
                programString.append(toImageOpFromKind(binaryExpr.getOp().getKind()));
                binaryExpr.getLeft().visit(this, arg);
                addComma();
                binaryExpr.getRight().visit(this, arg);
                programString.append(")");
            }

            if(!(imports.contains("import edu.ufl.cise.plc.runtime.ImageOps;\n"))){
                imports.add("import edu.ufl.cise.plc.runtime.ImageOps;\n");
            }

        }
        else if(imageIntOp){
            programString.append("ImageOps.binaryImageScalarOp(");
            programString.append(toImageOpFromKind(binaryExpr.getOp().getKind()));
            binaryExpr.getLeft().visit(this, arg);
            addComma();
            binaryExpr.getRight().visit(this, arg);
            programString.append(")");

            if(!(imports.contains("import edu.ufl.cise.plc.runtime.ImageOps;\n"))){
                imports.add("import edu.ufl.cise.plc.runtime.ImageOps;\n");
            }
        }
        else{
            if(stringOpNoEq){
                programString.append("!");
            }
            binaryExpr.getLeft().visit(this, null);
            if(stringOpEq || stringOpNoEq){
                programString.append(".equals(");
            }
            else{
                addSpace();
                programString.append(binaryExpr.getOp().getText());
                addSpace();
            }
            binaryExpr.getRight().visit(this, null);
            if(stringOpEq || stringOpNoEq){
                programString.append(")");
            }
        }
        if((binaryExpr.getCoerceTo() == COLORFLOAT || binaryExpr.getCoerceTo() == COLOR) && binaryExpr.getCoerceTo()!= binaryExpr.getType()){
            programString.append(")");
        }
        programString.append(")");

        return programString;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception{
        if(identExpr.getCoerceTo() != null && identExpr.getCoerceTo()!= identExpr.getType()){
            if(identExpr.getCoerceTo() == COLOR){
                programString.append("new ColorTuple(").append(identExpr.getText()).append(")");
                if(!(imports.contains("import edu.ufl.cise.plc.runtime.ColorTuple;\n"))){
                    imports.add("import edu.ufl.cise.plc.runtime.ColorTuple;\n");
                }
            }
            else if(identExpr.getCoerceTo() == COLORFLOAT){
                programString.append("new ColorTupleFloat(").append(identExpr.getText()).append(")");
                if(!(imports.contains("import edu.ufl.cise.plc.runtime.ColorTupleFloat;\n"))){
                    imports.add("import edu.ufl.cise.plc.runtime.ColorTupleFloat;\n");
                }
            }
            else {
                if(identExpr.getCoerceTo() == INT && (identExpr.getType() == COLOR || identExpr.getType() == COLORFLOAT)) {
                    programString.append(identExpr.getText()).append(".pack()");
                }
                else{
                    programString.append("(").append(toStringFromType(identExpr.getCoerceTo())).append(")").append(identExpr.getText());
                }
            }
        }
        else {
            programString.append(identExpr.getText());
        }

        return programString;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception{
        if(conditionalExpr.getCoerceTo() != null && conditionalExpr.getCoerceTo()!= conditionalExpr.getType()){
            if(conditionalExpr.getCoerceTo() == COLORFLOAT){
                programString.append("new ColorTupleFloat(");
                if(!(imports.contains("import edu.ufl.cise.plc.runtime.ColorTupleFloat;\n"))){
                    imports.add("import edu.ufl.cise.plc.runtime.ColorTupleFloat;\n");
                }
            }
            else if(conditionalExpr.getCoerceTo() == COLOR){
                programString.append("new ColorTuple(");
                if(!(imports.contains("import edu.ufl.cise.plc.runtime.ColorTuple;\n"))){
                    imports.add("import edu.ufl.cise.plc.runtime.ColorTuple;\n");
                }
            }
            else{
                programString.append("(").append(toStringFromType(conditionalExpr.getCoerceTo())).append(")");
            }
        }
        programString.append("((");
        conditionalExpr.getCondition().visit(this, null);
        programString.append(") ? ");
        conditionalExpr.getTrueCase().visit(this, null);
        programString.append(" : ");
        conditionalExpr.getFalseCase().visit(this ,null);
        programString.append(")");
        if((conditionalExpr.getCoerceTo() == COLORFLOAT || conditionalExpr.getCoerceTo() == COLOR) && conditionalExpr.getCoerceTo()!= conditionalExpr.getType()){
            programString.append(")");
        }
        return programString;
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws Exception{
        dimension.getWidth().visit(this, arg);
        programString.append(",");
        dimension.getHeight().visit(this ,arg);

        return programString;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception{
        pixelSelector.getX().visit(this, arg);
        programString.append(",");
        pixelSelector.getY().visit(this ,arg);

        return programString;
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception{
        if(assignmentStatement.getTargetDec().getType() == IMAGE){
            if(!(imports.contains("import edu.ufl.cise.plc.runtime.ImageOps;\n"))){
                imports.add("import edu.ufl.cise.plc.runtime.ImageOps;\n");
            }
            if(assignmentStatement.getExpr().getType() == IMAGE){
                if(assignmentStatement.getTargetDec().getDim() != null){
                    programString.append(assignmentStatement.getName()).append(" = ");
                    programString.append("ImageOps.resize(");
                    assignmentStatement.getExpr().visit(this ,arg);
                    addComma();
                    ((VarDeclaration)assignmentStatement.getTargetDec()).getNameDef().getDim().visit(this, arg);
                    programString.append(")");
                }
                else{
                    programString.append(assignmentStatement.getName()).append(" = ");
                    programString.append("ImageOps.clone(");
                    assignmentStatement.getExpr().visit(this ,arg);
                    programString.append(")");
                }
                addSemi();
            }
            else{
                if(assignmentStatement.getSelector() != null){
                    programString.append("for(int ");
                    assignmentStatement.getSelector().getX().visit(this, arg);
                    programString.append("=0; ");
                    assignmentStatement.getSelector().getX().visit(this, arg);
                    programString.append("<").append(assignmentStatement.getName());
                    programString.append(".getWidth(); ");
                    assignmentStatement.getSelector().getX().visit(this, arg);
                    programString.append("++){");
                    tabs++;
                    addNewLine().addTab(false);
                    programString.append("for(int ");
                    assignmentStatement.getSelector().getY().visit(this, arg);
                    programString.append("=0; ");
                    assignmentStatement.getSelector().getY().visit(this, arg);
                    programString.append("<").append(assignmentStatement.getName());
                    programString.append(".getHeight(); ");
                    assignmentStatement.getSelector().getY().visit(this, arg);
                    programString.append("++){");
                    tabs++;
                    addNewLine().addTab(false);
                    programString.append("ImageOpsAdd.setColor(");
                    programString.append(assignmentStatement.getName()).append(",");
                    assignmentStatement.getSelector().getX().visit(this, arg);
                    programString.append(",");
                    assignmentStatement.getSelector().getY().visit(this, arg);
                    programString.append(",");
                    assignmentStatement.getExpr().visit(this, arg);
                    programString.append(")");
                    addSemi();
                    tabs--;
                    addNewLine().addTab(false);
                    programString.append("}");
                    tabs--;
                    addNewLine().addTab(false);
                    programString.append("}");
                    if(!(imports.contains("import edu.ufl.cise.plc.runtime.ImageOpsAdd;\n"))){
                        imports.add("import edu.ufl.cise.plc.runtime.ImageOpsAdd;\n");
                    }
                }
                else{
                    programString.append("ImageOpsAdd.setAllPixels(");
                    programString.append(assignmentStatement.getName());
                    programString.append(",");
                    assignmentStatement.getExpr().visit(this, arg);
                    programString.append(");");
                    if(!(imports.contains("import edu.ufl.cise.plc.runtime.ImageOpsAdd;\n"))){
                        imports.add("import edu.ufl.cise.plc.runtime.ImageOpsAdd;\n");
                    }
                }
            }
        }
        else {
            programString.append(assignmentStatement.getName()).append(" = ");
            assignmentStatement.getExpr().visit(this, null);
            addSemi();
        }

        return  programString;
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception{
        if(writeStatement.getSource().getType() == IMAGE){
            if(writeStatement.getDest() instanceof ConsoleExpr){
                programString.append("ConsoleIO.displayImageOnScreen(");
                writeStatement.getSource().visit(this, arg);
            }
            else{
                if(!(imports.contains("import edu.ufl.cise.plc.runtime.FileURLIO;\n"))){
                    imports.add("import edu.ufl.cise.plc.runtime.FileURLIO;\n");
                }
                programString.append("FileURLIO.writeImage(");
                writeStatement.getSource().visit(this, arg);
                programString.append(",");
                writeStatement.getDest().visit(this, arg);
            }
        }
        else{
            if(writeStatement.getDest() instanceof ConsoleExpr){
                programString.append("ConsoleIO.console.println(");
                writeStatement.getSource().visit(this, arg);
            }
            else{
                if(!(imports.contains("import edu.ufl.cise.plc.runtime.FileURLIO;\n"))){
                    imports.add("import edu.ufl.cise.plc.runtime.FileURLIO;\n");
                }
                programString.append("FileURLIO.writeValue(");
                writeStatement.getSource().visit(this, arg);
                programString.append(",");
                writeStatement.getDest().visit(this, arg);
            }
        }
        programString.append(");");

        if(!(imports.contains("import edu.ufl.cise.plc.runtime.ConsoleIO;\n"))){
            imports.add("import edu.ufl.cise.plc.runtime.ConsoleIO;\n");
        }
        return programString;
    }

    @Override
    public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception{
        programString.append(readStatement.getName()).append(" = ");
        if(readStatement.getSource() instanceof ConsoleExpr){
            if(readStatement.getTargetDec().getType() == IMAGE){
                programString.append("FileURLIO.readImage(");
                readStatement.getSource().visit(this,arg);
                programString.append(",");
                ((VarDeclaration)readStatement.getTargetDec()).getNameDef().getDim().visit(this, arg);
                programString.append(")");
                if(!(imports.contains("import edu.ufl.cise.plc.runtime.FileURLIO;\n"))){
                    imports.add("import edu.ufl.cise.plc.runtime.FileURLIO;\n");
                }
            }
            else{
                readStatement.getSource().visit(this,  null);
            }

        }
        else{
            if(readStatement.getTargetDec().getType() == IMAGE){
                programString.append("FileURLIO.readImage(");
                readStatement.getSource().visit(this,arg);
                programString.append(",");
                ((VarDeclaration)readStatement.getTargetDec()).getNameDef().getDim().visit(this, arg);
                programString.append(")");
                if(!(imports.contains("import edu.ufl.cise.plc.runtime.FileURLIO;\n"))){
                    imports.add("import edu.ufl.cise.plc.runtime.FileURLIO;\n");
                }
            }
            else{
                if(!(imports.contains("import edu.ufl.cise.plc.runtime.FileURLIO;\n"))){
                    imports.add("import edu.ufl.cise.plc.runtime.FileURLIO;\n");
                }
                programString.append(toParseFromType(readStatement.getTargetDec().getType()));
                readStatement.getSource().visit(this, arg);
                programString.append(")");
            }

        }
        addSemi();
        return programString;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception{
        programString.append("public class ").append(program.getName()).append(" {");
        addNewLine().addTab(true);
        programString.append("public static ").append(toStringFromType(program.getReturnType())).append(" apply(");

        List<NameDef> params = program.getParams();
        for(int i=0; i < params.size()-1; i++){
            params.get(i).visit(this, null);
            addComma().addSpace();
        }
        if(params.size() != 0){
            params.get(params.size()-1).visit(this, null);
        }

        programString.append("){");
        addNewLine().addTab(false);

        List<ASTNode> decsAndStatements = program.getDecsAndStatements();
        for(int i=0; i < decsAndStatements.size()-1; i++){
            decsAndStatements.get(i).visit(this, null);
            addNewLine().addTab(false);
        }
        if(decsAndStatements.size() != 0){
            decsAndStatements.get(decsAndStatements.size()-1).visit(this, null);
            tabs--;
            addNewLine().addTab(false);
        }

        programString.append("}");
        tabs--;
        addNewLine().addTab(false);
        programString.append("}");

        String ret = "";
        if(Name != null && Name.length() != 0){
            ret =  "package " + Name +";\n";
        }

        for(int i=0; i<imports.size(); i++){
            ret += imports.get(i);
        }
        return ret + programString.toString();
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws Exception{
        programString.append(toStringFromType(nameDef.getType())).append(" ").append(nameDef.getName());
        if(nameDef.getType() == COLOR){
            if(!(imports.contains("import edu.ufl.cise.plc.runtime.ColorTuple;;\n"))){
                imports.add("import edu.ufl.cise.plc.runtime.ColorTuple;\n");
            }
        }
        if(nameDef.getType() == IMAGE){
            if(!(imports.contains("import java.awt.image.BufferedImage;\n"))){
                imports.add("import java.awt.image.BufferedImage;\n");
            }
        }

        return programString;
    }

    @Override
    public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception{
        if(nameDefWithDim.getType() == IMAGE){
            programString.append(toStringFromType(nameDefWithDim.getType())).append(" ").append(nameDefWithDim.getName());
            if(!(imports.contains("import java.awt.image.BufferedImage;\n"))){
                imports.add("import java.awt.image.BufferedImage;\n");
            }
        }

        return programString;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception{
        programString.append("return ");
        returnStatement.getExpr().visit(this, null);
        addSemi();
        return programString;
    }

    @Override
    public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception{
        Type target = declaration.getNameDef().getType();
        Expr exp = declaration.getExpr();
        IToken op = declaration.getOp();

        if(target == IMAGE){
            if(declaration.getNameDef().getDim() != null && exp != null){
                declaration.getNameDef().visit(this, arg);
                if(op.getKind() == Kind.ASSIGN){
                    if(declaration.getExpr().getType() == IMAGE){
                        programString.append("=ImageOps.resize(");
                        declaration.getExpr().visit(this ,arg);
                        addComma();
                        declaration.getNameDef().getDim().visit(this, arg);
                        if(!(imports.contains("import edu.ufl.cise.plc.runtime.ImageOps;\n"))){
                            imports.add("import edu.ufl.cise.plc.runtime.ImageOps;\n");
                        }
                    }
                    else {
                        programString.append("=ImageOpsAdd.makeConstantImage(");
                        declaration.getNameDef().getDim().visit(this, arg);
                        addComma();
                        declaration.getExpr().visit(this, arg);
                        if (!(imports.contains("import edu.ufl.cise.plc.runtime.ImageOpsAdd;\n"))) {
                            imports.add("import edu.ufl.cise.plc.runtime.ImageOpsAdd;\n");
                        }
                    }
                    programString.append(")");
                    addSemi();
                }
                else{
                    programString.append("=FileURLIO.readImage(");
                    exp.visit(this,arg);
                    programString.append(",");
                    declaration.getNameDef().getDim().visit(this, arg);
                    programString.append(")");
                    addSemi();
                    if(!(imports.contains("import edu.ufl.cise.plc.runtime.FileURLIO;\n"))){
                        imports.add("import edu.ufl.cise.plc.runtime.FileURLIO;\n");
                    }
                }
            }
            else if(exp != null){
                declaration.getNameDef().visit(this, arg);
                if(op.getKind() == Kind.ASSIGN){
                    programString.append("=ImageOps.clone(");
                    declaration.getExpr().visit(this ,arg);
                    programString.append(")");
                    addSemi();
                    if(!(imports.contains("import edu.ufl.cise.plc.runtime.ImageOps;\n"))){
                        imports.add("import edu.ufl.cise.plc.runtime.ImageOps;\n");
                    }
                }
                else{
                    programString.append("=FileURLIO.readImage(");
                    exp.visit(this,arg);
                    programString.append(")");
                    addSemi();
                    if(!(imports.contains("import edu.ufl.cise.plc.runtime.FileURLIO;\n"))){
                        imports.add("import edu.ufl.cise.plc.runtime.FileURLIO;\n");
                    }
                }
            }
            else{
                declaration.getNameDef().visit(this, arg);
                programString.append("=new BufferedImage(");
                declaration.getNameDef().getDim().visit(this, arg);
                programString.append(",BufferedImage.TYPE_INT_RGB)");
                addSemi();
            }
            if(!(imports.contains("import java.awt.image.BufferedImage;\n"))){
                imports.add("import java.awt.image.BufferedImage;\n");
            }
        }
        else if(exp != null){
            Type expr = declaration.getExpr().getType();
            if(op.getKind() == Kind.ASSIGN){
                declaration.getNameDef().visit(this, null);
                addSpace();
                programString.append("=");
                addSpace();
                exp.visit(this, null);
                addSemi();
            }
            else{
                declaration.getNameDef().visit(this, null);
                addSpace();
                programString.append("=");
                addSpace();
                if(declaration.getExpr() instanceof ConsoleExpr){
                    exp.visit(this, null);

                }
                else{
                    if(!(imports.contains("import edu.ufl.cise.plc.runtime.FileURLIO;\n"))){
                        imports.add("import edu.ufl.cise.plc.runtime.FileURLIO;\n");
                    }
                    programString.append(toParseFromType(declaration.getType()));
                    exp.visit(this, arg);
                    programString.append(")");
                }
                addSemi();
            }
        }
        else{
            declaration.getNameDef().visit(this, null);
            addSemi();
        }

        return programString;
    }

    @Override
    public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception{
        if(unaryExprPostfix.getCoerceTo() != INT){
            programString.append("ColorTuple.unpack(");
        }
        unaryExprPostfix.getExpr().visit(this, arg);
        programString.append(".getRGB(");
        unaryExprPostfix.getSelector().visit(this, arg);
        programString.append(")");
        if(unaryExprPostfix.getCoerceTo() != INT){
            programString.append(")");
        }

        return programString;
    }

}
