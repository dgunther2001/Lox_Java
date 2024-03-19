package lox;

class Interpreter implements Expr.Visitor<Object /* allows the illusion of dynamically typed variables */> { 

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
                return -(double)right; // negative inversion
        }

        //unreachable
        return null;
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

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression); // recursive evaluation of the subexpression in a grouping of parenthesis
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this); // sends the grouped object back into the interpretors visitor implementation
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) { // switch case to evaluate possible binary expressions
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER: // greater than operator
                return (double)left > (double)right;
            case GREATER_EQUAL: // greater than or equal to operator
                return (double)left >= (double)right;
            case LESS: // less than operator
                return (double)left < (double)right;
            case LESS_EQUAL: // less than or equal to operator
                return (double)left <= (double)right;
            case MINUS: // subtraction
                return (double)left - (double)right;
            case PLUS: // both numerical addition and string concatenation
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
                break;
            case SLASH: // division
                return (double)left / (double)right;
            case STAR: // multiplication
                return (double)left * (double)right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
        }

        return null; // will be unreachable once we implement all possible binary expressions

    }

}