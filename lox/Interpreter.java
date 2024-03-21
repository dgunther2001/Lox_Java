package lox;

import java.util.ArrayList;
import java.util.List;

class Interpreter implements Expr.Visitor<Object /* allows the illusion of dynamically typed variables */>, Stmt.Visitor<Void>  { 

    private Environment environment = new Environment();

    void interpret(List<Stmt> statements) {  // essentially the API that allows us o actually interpret a piece of code (ALLOWS US TO USE THE INTERPRETER)
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value; // returning the literal as a generic object
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right); // evaluates thright side of the expression

        switch (expr.operator.type) { 
            case BANG:
                return !isTruthy(right); // boolean check
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right; // negative inversion
        }

        //unreachable
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) { // go straight to the environment whenever we want to access a variable
        return environment.get(expr.name);
    }

    private void checkNumberOperand(Token operator, Object operand) { // if a unary expression for the negative inversion isn't a number, it throws an error
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private boolean isTruthy(Object object) { // checks to see if something is a boolean
        if (object == null) return false; // returns false if its not a boolean
        if (object instanceof Boolean) return (boolean)object; // returns the inversion of the boolean if it is
        return true; // otherwise it just returns true
    }

    private boolean isEqual(Object a, Object b) { // equality method for checking if two objects are equal at runtime
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    private String stringify(Object object) { // creates a stingification of our output of an expression
        if (object == null) return "nil"; // if the object is null, it stays that way

        if (object instanceof Double) { // if its a double, it takes the result and turns it into a string that we can output
            String text = object.toString();
            if (text.endsWith(".0")) { // hacks off the decimal place when outputting ints, as we treat everything in our java "interpreter" as a double
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString(); // takes other literal types and outputs them
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression); // recursive evaluation of the subexpression in a grouping of parenthesis
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this); // sends the grouped object back into the interpretors visitor implementation
    }

    private void execute(Stmt stmt) {
        stmt.accept(this); // actually calls and executes the statement
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment; // sets a previous environment
        try {
            this.environment = environment; // passes a new environment

            for (Stmt statement : statements) { // executes all of the statements in the block
                execute(statement);
            }
        } finally {
            this.environment = previous; // goes back to the previous environment
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression); // evaluates the expression statment 
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression); // evaluates the statement part of the expression
        System.out.println(stringify(value)); // actually prints the value out to the user
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if(stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value); // adds the key value pair to the environment i our HashMap
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) { // switch case to evaluate possible binary expressions
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER: // greater than operator
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL: // greater than or equal to operator
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS: // less than operator
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL: // less than or equal to operator
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case MINUS: // subtraction
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case PLUS: // both numerical addition and string concatenation
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case SLASH: // division
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;
            case STAR: // multiplication
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
        }

        return null; // will be unreachable once we implement all possible binary expressions

    }

    private void checkNumberOperands(Token operator, Object left, Object right) { // throws a runtime error if each side of a binary expression are not numbers
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

}