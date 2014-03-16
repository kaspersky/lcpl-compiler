import ro.pub.cs.lcpl.*;
import java.util.List;
import java.util.TreeSet;
import java.util.TreeMap;
import java.util.LinkedList;

// Exceptions
class LCPLException extends Exception {
    public LCPLException(String e) {
        super(e);
    }
}

class LCPLDupClassNameException extends LCPLException {
    public LCPLDupClassNameException(int line, String name) {
        super("Error in line " + line + " : A class with the same name already exists : " + name);
    }
}

class LCPLNoMainClassException extends LCPLException {
    public LCPLNoMainClassException(int line, String message) {
        super("Error in line " + line + " : " + message);
    }
}

class LCPLParentClassException extends LCPLException {
    public LCPLParentClassException(int line, String message) {
        super("Error in line " + line + " : " + message);
    }
}

class LCPLDupAttributeException extends LCPLException {
    public LCPLDupAttributeException(int line, String message) {
        super("Error in line " + line + " : " + message);
    }
}

class LCPLDupMethodException extends LCPLException {
    public LCPLDupMethodException(int line, String message) {
        super("Error in line " + line + " : " + message);
    }
}

class LCPLTypeNotFoundException extends LCPLException {
    public LCPLTypeNotFoundException(int line, String message) {
        super("Error in line " + line + " : " + message);
    }
}

class LCPLSymbolNotFoundException extends LCPLException {
    public LCPLSymbolNotFoundException(int line, String message) {
        super("Error in line " + line + " : " + message);
    }
}

class LCPLMethodNotFoundException extends LCPLException {
    public LCPLMethodNotFoundException(int line, String message) {
        super("Error in line " + line + " : " + message);
    }
}

class LCPLEmptyBodyException extends LCPLException {
    public LCPLEmptyBodyException(int line, String message) {
        super("Error in line " + line + " : " + message);
    }
}

class LCPLCastException extends LCPLException {
    public LCPLCastException(int line, String message) {
        super("Error in line " + line + " : " + message);
    }
}

class LCPLFormalParamNumException extends LCPLException {
    public LCPLFormalParamNumException(int line, String message) {
        super("Error in line " + line + " : " + message);
    }
}

class LCPLBadOverMethodException extends LCPLException {
    public LCPLBadOverMethodException(int line, String message) {
        super("Error in line " + line + " : " + message);
    }
}

class LCPLConditionException extends LCPLException {
    public LCPLConditionException(int line, String message) {
        super("Error in line " + line + " : " + message);
    }
}

public class ASemantic {

    private TreeMap<String, LCPLClass> mapStringClass = new TreeMap<String, LCPLClass>();
    private LCPLClass LCPLObject = null;
    private LCPLClass LCPLIO = null;
    private LCPLClass LCPLString = null;
    private IntType intType = null;
    private NoType noType = null;
    private NullType nullType = null;
    private boolean FOLDING = true;

    public ASemantic(boolean folding) {
        FOLDING = folding;
    }

    // Create Object, IO and String classes
    private void initBaseTypes(Program p) {
        LCPLObject = new LCPLClass(0, "Object", null, null);
        LCPLString = new LCPLClass(0, "String", "Object", null);
        LCPLIO = new LCPLClass(0, "IO", "Object", null);
        intType = new IntType();
        noType = new NoType();
        nullType = new NullType();

        FormalParam selfFP = null;

        // Object
        LinkedList<Feature> features = new LinkedList<Feature>();
        Method met = new Method(0, "abort", new LinkedList<FormalParam>(), "void", null);
        met.setParent(LCPLObject);
        selfFP = new FormalParam("self", LCPLObject.getName());
        selfFP.setVariableType(LCPLObject);
        met.setSelf(selfFP);
        met.setReturnTypeData(noType);
        features.add(met);
        met = new Method(0, "typeName", new LinkedList<FormalParam>(), "String", null);
        met.setParent(LCPLObject);
        selfFP = new FormalParam("self", LCPLObject.getName());
        selfFP.setVariableType(LCPLObject);
        met.setSelf(selfFP);
        met.setReturnTypeData(LCPLString);
        features.add(met);
        met = new Method(0, "copy", new LinkedList<FormalParam>(), "Object", null);
        met.setParent(LCPLObject);
        selfFP = new FormalParam("self", LCPLObject.getName());
        selfFP.setVariableType(LCPLObject);
        met.setSelf(selfFP);
        met.setReturnTypeData(LCPLObject);
        features.add(met);
        LCPLObject.setFeatures(features);
        LCPLObject.setParentData(null);

        // IO
        features = new LinkedList<Feature>();
        LinkedList<FormalParam> formals = new LinkedList<FormalParam>();
        FormalParam fp = new FormalParam("msg", "String");
        fp.setVariableType(LCPLString);
        formals.add(fp);
        met = new Method(0, "out", formals, "IO", null);
        met.setParent(LCPLIO);
        selfFP = new FormalParam("self", LCPLIO.getName());
        selfFP.setVariableType(LCPLIO);
        met.setSelf(selfFP);
        met.setReturnTypeData(LCPLIO);
        features.add(met);
        met = new Method(0, "in", new LinkedList<FormalParam>(), "String", null);
        met.setParent(LCPLIO);
        selfFP = new FormalParam("self", LCPLIO.getName());
        selfFP.setVariableType(LCPLIO);
        met.setSelf(selfFP);
        met.setReturnTypeData(LCPLString);
        features.add(met);
        LCPLIO.setFeatures(features);
        LCPLIO.setParentData(LCPLObject);

        // String
        features = new LinkedList<Feature>();
        met = new Method(0, "length", new LinkedList<FormalParam>(), "Int", null);
        met.setParent(LCPLString);
        selfFP = new FormalParam("self", LCPLString.getName());
        selfFP.setVariableType(LCPLString);
        met.setSelf(selfFP);
        met.setReturnTypeData(intType);
        features.add(met);
        met = new Method(0, "toInt", new LinkedList<FormalParam>(), "Int", null);
        met.setParent(LCPLString);
        selfFP = new FormalParam("self", LCPLString.getName());
        selfFP.setVariableType(LCPLString);
        met.setSelf(selfFP);
        met.setReturnTypeData(intType);
        features.add(met);
        LCPLString.setFeatures(features);
        LCPLString.setParentData(LCPLObject);

        mapStringClass.put("Object", LCPLObject);
        mapStringClass.put("IO", LCPLIO);
        mapStringClass.put("String", LCPLString);

        p.setIntType(intType);
        p.setNoType(noType);
        p.setNullType(nullType);
        p.setObjectType(LCPLObject);
        p.setStringType(LCPLString);
        p.setIoType(LCPLIO);
    }

    // Test class names for unique names and create the NAME -> CLASS map
    private void testUniqueClassNames(Program p) throws LCPLException {
        for (LCPLClass cl : p.getClasses()) {
            LCPLClass ret = mapStringClass.put(cl.getName(), cl);
            if (ret != null)
                throw new LCPLDupClassNameException(cl.getLineNumber(), cl.getName());
        }
    }

    // Check for Main class and main method
    private void testNoMainClass(Program p) throws LCPLException {
        List<LCPLClass> classes = p.getClasses();
        LCPLClass cl = mapStringClass.get("Main");
        if (cl == null)
            throw new LCPLNoMainClassException(1, "Class Main not found.");

        // Look for main method in parent classes
        while (cl.getParent() != null) {
            for (Feature ft : cl.getFeatures())
                if (ft instanceof Method)
                    if (((Method) ft).getName().equals("main"))
                        return;
            cl = mapStringClass.get(cl.getParent());
        }

        throw new LCPLNoMainClassException(1, "Method main not found in class Main");
    }

    // Check if class has an existent parent class
    private void testParentClass(Program p) throws LCPLException {
        for (LCPLClass cl : p.getClasses())
            if (cl.getParent() == null && !cl.getName().equals("Object"))
                cl.setParent("Object");
        for (LCPLClass cl : p.getClasses())
        {
            if (cl.getParent() == null || cl.getParent().equals("Object") || cl.getParent().equals("IO"))
                continue;
            if (cl.getParent().equals("Int"))
                throw new LCPLParentClassException(cl.getLineNumber(), "Class Int not found.");
            else if (cl.getParent().equals("String"))
                throw new LCPLParentClassException(cl.getLineNumber(), "A class cannot inherit a String");
            else if (cl.getParent().equals(cl.getName()))
                throw new LCPLParentClassException(cl.getLineNumber(), "Class " + cl.getName() + " recursively inherits itself.");
            else {
                // Make sure class hierarchy is an acyclic graph
                boolean found = false;
                for (LCPLClass cl2 : p.getClasses())
                    if (cl.getParent().equals(cl2.getName())) {
                        TreeSet<String> ts = new TreeSet<String>();
                        ts.add(cl2.getName());
                        boolean res;
                        do {
                            String parent = cl2.getParent();
                            res = ts.add(parent);
                            if (!res)
                                throw new LCPLParentClassException(cl.getLineNumber(), "Class " + cl.getName() + " recursively inherits itself.");
                            res = false;
                            for (LCPLClass cl3 : p.getClasses())
                                if (cl3.getName().equals(parent)) {
                                    cl2 = cl3;
                                    res = true;
                                    break;
                                }
                        } while (res && !cl2.getParent().equals("null") && !cl2.getParent().equals("Object"));
                        found = true;
                        break;
                    }
                if (!found)
                    throw new LCPLParentClassException(cl.getLineNumber(), "Class " + cl.getParent() + " not found.");
            }
        }
    }

    // Check for duplicate method names in same class
    private void testDupMethods(Program p) throws LCPLException {
        for (LCPLClass cl : p.getClasses()) {
            TreeSet<String> methods = new TreeSet<String>();
            for (Feature ft : cl.getFeatures())
                if (ft instanceof Method)
                    if (!methods.add(((Method) ft).getName()))
                        throw new LCPLDupMethodException(((Method) ft).getLineNumber(), "A method with the same name already exists in class " + cl.getName() + " : " + ((Method) ft).getName());
        }
    }

    // Used in testType
    private void testLocalDefinitionType(Expression e) throws LCPLException {
        if (e instanceof Block)
            for (Expression expr : ((Block) e).getExpressions())
                testLocalDefinitionType(expr);
        else if (e instanceof LocalDefinition) {
            String type = ((LocalDefinition) e).getType();
            if (!type.equals("Int") && !type.equals("Object") && !type.equals("String") && !type.equals("IO") && !type.equals("void") && mapStringClass.get(type) == null)
                throw new LCPLTypeNotFoundException(((TreeNode) e).getLineNumber(), "Class " + type + " not found.");
            testLocalDefinitionType(((LocalDefinition) e).getScope());
        }
    }

    // Check return types
    private void testType(Program p) throws LCPLException {
        for (LCPLClass cl : p.getClasses()) {
            for (Feature ft : cl.getFeatures())
                if (ft instanceof Attribute) {
                    Attribute attr = (Attribute) ft;
                    String type = attr.getType();
                    if (!type.equals("Int") && !type.equals("Object") && !type.equals("String") && !type.equals("IO") && !type.equals("void") && mapStringClass.get(type) == null)
                        throw new LCPLTypeNotFoundException(attr.getLineNumber(), "Class " + type + " not found.");
                }
                else if (ft instanceof Method) {
                    Method met = (Method) ft;
                    String type;
                    for (FormalParam fp : met.getParameters()) {
                        type = fp.getType();
                        if (!type.equals("Int") && !type.equals("Object") && !type.equals("String") && !type.equals("IO") && !type.equals("void") && mapStringClass.get(type) == null)
                            throw new LCPLTypeNotFoundException(fp.getLineNumber(), "Class " + type + " not found.");
                    }
                    type = met.getReturnType();
                    if (!type.equals("Int") && !type.equals("Object") && !type.equals("String") && !type.equals("IO") && !type.equals("void") && mapStringClass.get(type) == null)
                        throw new LCPLTypeNotFoundException(met.getLineNumber(), "Class " + type + " not found.");
                    testLocalDefinitionType(met.getBody());
                    if (!type.equals("void") && (((Block) met.getBody()).getExpressions().isEmpty()))
                        throw new LCPLEmptyBodyException(0, "Cannot convert a value of type (none) into " + type);
                }
        }
    }

    // Check if cast from "from" to "to" is possible
    private boolean castIsPossible(String to, String from) {
        if (to.equals(from))
            return true;
        if (to.equals("String") && from.equals("Int"))
            return true;
        if (to.equals("Object"))
            return true;
        if (from.equals("void"))
            return true;
        LCPLClass cl = mapStringClass.get(from);
        if (cl == null)
            return false;
        while (!cl.getName().equals("Object") && !cl.getName().equals("IO")) {
            if (cl.getParent().equals(to))
                return true;
            cl = mapStringClass.get(cl.getParent());
        }
        return false;
    }

    // Actual semantic analysis
    // Create variables (resolve symbols) and calculate expression types
    private Expression solveTypeAndVariable(LCPLClass cl, FormalParam selfie, Method method, TreeMap<String, Variable> attributes, TreeMap<String, Variable> formals, TreeMap<String, Variable> locals, Expression e) throws LCPLException {
        if (e == null)
            return e;
        // Base building blocks
        if (e instanceof IntConstant)
            e.setTypeData(intType);
        else if (e instanceof StringConstant)
            e.setTypeData(LCPLString);
        else if (e instanceof VoidConstant)
            e.setTypeData(nullType);
        else if (e instanceof Addition) {
            Expression e1 = ((BinaryOp) e).getE1();
            Expression e2 = ((BinaryOp) e).getE2();
            e1 = solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, e1);
            e2 = solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, e2);
            String e1Type = e1.getTypeData().getName();
            String e2Type = e2.getTypeData().getName();
            if (e1Type.equals("Int") && e2Type.equals("Int")) {
                if (FOLDING && e1 instanceof IntConstant && e2 instanceof IntConstant) {
                    int value = ((IntConstant) e1).getValue() + ((IntConstant) e2).getValue();
                    Expression ret = new IntConstant(e.getLineNumber(), value);
                    ret.setTypeData(intType);
                    return ret;
                }
                e.setTypeData(intType);
            }
            else if (e1Type.equals("Int") && e2Type.equals("String") || e1Type.equals("String") && e2Type.equals("Int")) {
                if (e1Type.equals("Int")) {
                    if (FOLDING && e1 instanceof IntConstant && e2 instanceof StringConstant) {
                        String value = "" + ((IntConstant) e1).getValue() + ((StringConstant) e2).getValue();
                        Expression ret = new StringConstant(e.getLineNumber(), value);
                        ret.setTypeData(LCPLString);
                        return ret;
                    }
                    Cast cast = new Cast(e.getLineNumber(), "String", e1);
                    cast.setTypeData(LCPLString);
                    ((BinaryOp) e).setE1(cast);
                } else {
                    if (FOLDING && e2 instanceof IntConstant && e1 instanceof StringConstant) {
                        String value = ((StringConstant) e1).getValue() + ((IntConstant) e2).getValue();
                        Expression ret = new StringConstant(e.getLineNumber(), value);
                        ret.setTypeData(LCPLString);
                        return ret;
                    }
                    Cast cast = new Cast(e.getLineNumber(), "String", e2);
                    cast.setTypeData(LCPLString);
                    ((BinaryOp) e).setE2(cast);
                }
                e.setTypeData(LCPLString);
            }
            else if (e1Type.equals("String") && e2Type.equals("String")) {
                if (FOLDING && e1 instanceof StringConstant && e2 instanceof StringConstant) {
                    String value = ((StringConstant) e1).getValue() + ((StringConstant) e2).getValue();
                    Expression ret = new StringConstant(e.getLineNumber(), value);
                    ret.setTypeData(LCPLString);
                    return ret;
                }
                e.setTypeData(LCPLString);
            }
            else if (e1Type.equals("Int"))
                throw new LCPLCastException(((TreeNode) e).getLineNumber(), "Cannot convert '+' expression to Int or String");
            else if (e2Type.equals("Int"))
                throw new LCPLCastException(((TreeNode) e).getLineNumber(), "Cannot convert '+' expression to Int or String");
            else if (e1Type.equals("String")) {
                if (e2Type.equals("void")) {
                    if (FOLDING) {
                        String value = ((StringConstant) e1).getValue();
                        Expression ret = new StringConstant(e.getLineNumber(), value);
                        ret.setTypeData(LCPLString);
                        return ret;
                    }
                    e.setTypeData(LCPLString);
                }
                else
                    throw new LCPLCastException(((TreeNode) e).getLineNumber(), "Cannot convert a value of type " + e2Type + " into String");
            } else if (e2Type.equals("String")) {
                if (e1Type.equals("void")) {
                    if (FOLDING) {
                        String value = ((StringConstant) e2).getValue();
                        Expression ret = new StringConstant(e.getLineNumber(), value);
                        ret.setTypeData(LCPLString);
                        return ret;
                    }
                    e.setTypeData(LCPLString);
                }
                else
                    throw new LCPLCastException(((TreeNode) e).getLineNumber(), "Cannot convert a value of type " + e1Type + " into String");
            }
            else if (e1.getType().equals("void") && e2.getType().equals("void")) {
                throw new LCPLCastException(((TreeNode) e).getLineNumber(), "Cannot convert '+' expression to Int or String");
                //Use this when void + void will be allowed
                //if (FOLDING) {
                //    Expression ret = new VoidConstant(e.getLineNumber());
                //    ret.setTypeData(nullType);
                //    return ret;
                //}
                //e.setTypeData(nullType);
            }
        }
        // Assignment
        else if (e instanceof Assignment) {
            Assignment as = (Assignment) e;
            as.setE1(solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, as.getE1()));
            Variable ret;

            String [] ss = as.getSymbol().split("self.");
            if (ss[0].isEmpty()) {
                as.setSymbol(ss[1]);
                ret = attributes.get(as.getSymbol());
                if (ret == null)
                    throw new LCPLSymbolNotFoundException(((TreeNode) e).getLineNumber(), "Attribute " + as.getSymbol() + " not found in class " + cl.getName());
            }
            else {
                ret = locals.get(as.getSymbol());
                if (ret == null) {
                    ret = formals.get(as.getSymbol());
                    if (ret == null) {
                        ret = attributes.get(as.getSymbol());
                        if (ret == null)
                            throw new LCPLSymbolNotFoundException(((TreeNode) e).getLineNumber(), "Attribute " + as.getSymbol() + " not found in class " + cl.getName());
                    }
                }
            }
            as.setSymbolData(ret);
            if (ret instanceof Attribute) {
                if (!((Attribute) ret).getType().equals(as.getE1().getType())) {
                    if (!castIsPossible(((Attribute) ret).getType(), as.getE1().getType()))
                        throw new LCPLCastException(((TreeNode) e).getLineNumber(), "Cannot convert a value of type " + as.getE1().getType() + " into " + ((Attribute) ret).getType());
                    else if (!as.getE1().getType().equals("void")) {
                        Cast cast = new Cast(e.getLineNumber(), ((Attribute) ret).getType(), as.getE1());
                        cast.setTypeData(((Attribute) ret).getTypeData());
                        as.setE1(cast);
                    }
                }
                e.setTypeData(((Attribute) ret).getTypeData());
            } else if (ret instanceof FormalParam) {
                if (!((FormalParam) ret).getType().equals(as.getE1().getType())) {
                    if (!castIsPossible(((FormalParam) ret).getType(), as.getE1().getType()))
                        throw new LCPLCastException(((TreeNode) e).getLineNumber(), "Cannot convert a value of type " + as.getE1().getType() + " into " + ((FormalParam) ret).getType());
                    else if (!as.getE1().getType().equals("void")) {
                        Cast cast = new Cast(e.getLineNumber(), ((FormalParam) ret).getType(), as.getE1());
                        cast.setTypeData(((FormalParam) ret).getVariableType());
                        as.setE1(cast);
                    }
                }
                e.setTypeData(((FormalParam) ret).getVariableType());
            } else if (ret instanceof LocalDefinition) {
                if (!((LocalDefinition) ret).getType().equals(as.getE1().getType())) {
                    if (!castIsPossible(((LocalDefinition) ret).getType(), as.getE1().getType())) {
                        String type = as.getE1().getType();
                        throw new LCPLCastException(((TreeNode) e).getLineNumber(), "Cannot convert a value of type " + type + " into " + ((LocalDefinition) ret).getType());
                    }
                    else if (!as.getE1().getType().equals("void")) {
                        Cast cast = new Cast(e.getLineNumber(), ((LocalDefinition) ret).getType(), as.getE1());
                        cast.setTypeData(((LocalDefinition) ret).getVariableType());
                        as.setE1(cast);
                    }
                }
                e.setTypeData(((LocalDefinition) ret).getVariableType());
            }
        }
        else if (e instanceof Block) {
            // Block has its last expression's type
            Type dataType = noType;
            List<Expression> expressions = ((Block) e).getExpressions();
            for (int i = 0; i < expressions.size(); i++) {
                Expression expr = expressions.get(i);
                expr = solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, expr);
                expressions.set(i, expr);
                if (expr instanceof LocalDefinition)
                    dataType = ((LocalDefinition) expr).getScope().getTypeData();
                else {
                    dataType = expr.getTypeData();
                }
            }
            e.setTypeData(dataType);
        }
        else if (e instanceof Cast) {
            // It is specified that type checking in explicit casting is done at runtime
            // although I could do it here, statically
            Cast cast = (Cast) e;
            cast.setE1(solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, cast.getE1()));
            if (mapStringClass.get(cast.getType()) == null && !cast.getType().equals("Int"))
                throw new LCPLCastException(e.getLineNumber(), "Class " + cast.getType() + " not found.");
            e.setTypeData(mapStringClass.get(cast.getType()));
        }
        else if (e instanceof Dispatch) {
            Dispatch d = (Dispatch) e;
            Expression obj = ((BaseDispatch) d).getObject();

            solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, obj);

            // Find the method
            LCPLClass it = cl;
            Method met = null;
            if (obj != null) {
                Type t = ((Expression) obj).getTypeData();
                if (t instanceof LCPLClass)
                    it = (LCPLClass) t;
                else
                    return e;
            }

            // Find the method eventually in parent classes
            LCPLClass initIt = it;
            while (true) {
                boolean found = false;
                for (Feature ft : it.getFeatures()) {
                    if (ft instanceof Method && ((Method) ft).getName().equals(d.getName())) {
                        met = (Method) ft;
                        found = true;
                        break;
                    }
                }
                if (found || it.getParent() == null)
                    break;
                it = mapStringClass.get(it.getParent());
            }

            if (met != null) {
                e.setTypeData(met.getReturnTypeData());
                ((BaseDispatch) e).setMethod(met);

                if (obj == null) {
                    Symbol self;
                    if (method != null)
                        self = new Symbol(method.getLineNumber(), "self");
                    else
                        self = new Symbol(e.getLineNumber(), "self");
                    ((Expression) self).setTypeData(selfie.getVariableType());
                    self.setVariable(selfie);
                    ((BaseDispatch) d).setObject(self);
                }
            }
            else
                throw new LCPLMethodNotFoundException(e.getLineNumber(), "Method " + d.getName() + " not found in class " + initIt.getName());

            // Check that formal and actual parameters match
            List<Expression> arguments = ((BaseDispatch) d).getArguments();
            for (int i = 0; i < arguments.size(); i++) {
                Expression expr = arguments.get(i);
                arguments.set(i, solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, expr));
                expr = arguments.get(i);
                if (i == met.getParameters().size())
                    throw new LCPLFormalParamNumException(e.getLineNumber(), "Too many arguments in method call " + met.getName());
                FormalParam fp = met.getParameters().get(i);
                if (expr.getTypeData() != fp.getVariableType()) {
                    String type = expr.getTypeData().getName();
                    if (!castIsPossible(fp.getVariableType().getName(), type)) {
                        throw new LCPLCastException(expr.getLineNumber(), "Cannot convert a value of type " + type + " into " + fp.getType());
                    }
                    else if (!type.equals("void")) {
                        Cast cast = new Cast(expr.getLineNumber(), fp.getVariableType().getName(), expr);
                        cast.setTypeData(fp.getVariableType());
                        arguments.set(i, cast);
                    }
                }
            }
            if (arguments.size() < met.getParameters().size())
                throw new LCPLFormalParamNumException(((TreeNode) e).getLineNumber(), "Not enough arguments in method call " + met.getName());

        }
        else if (e instanceof Division) {
            Expression e1 = ((BinaryOp) e).getE1();
            Expression e2 = ((BinaryOp) e).getE2();
            e1 = solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, e1);
            e2 = solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, e2);
            if (!e1.getType().equals("Int"))
                throw new LCPLCastException(((TreeNode) e).getLineNumber(), "Cannot convert a value of type " + e1.getType() + " into Int");
            if (!e2.getType().equals("Int"))
                throw new LCPLCastException(((TreeNode) e).getLineNumber(), "Cannot convert a value of type " + e2.getType() + " into Int");
            if (FOLDING && e1 instanceof IntConstant && e2 instanceof IntConstant) {
                int value = ((IntConstant) e1).getValue() / ((IntConstant) e2).getValue();
                Expression ret = new IntConstant(e.getLineNumber(), value);
                ret.setTypeData(intType);
                return ret;
            }
            e.setTypeData(intType);
        }
        else if (e instanceof EqualComparison) {
            Expression e1 = ((BinaryOp) e).getE1();
            Expression e2 = ((BinaryOp) e).getE2();
            e1 = solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, e1);
            e2 = solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, e2);
            if (e1.getType().equals("Int") && e2.getType().equals("Int")) {
                if (FOLDING && e1 instanceof IntConstant && e2 instanceof IntConstant) {
                    int value = ((IntConstant) e1).getValue() == ((IntConstant) e2).getValue() ? 1 : 0;
                    Expression ret = new IntConstant(e.getLineNumber(), value);
                    ret.setTypeData(intType);
                    return ret;
                }
                e.setTypeData(intType);
            }
            else if (e1.getType().equals("Int") && e2.getType().equals("String") || e1.getType().equals("String") && e2.getType().equals("Int")) {
                if (e1.getType().equals("Int")) {
                    if (FOLDING && e1 instanceof IntConstant && e2 instanceof StringConstant) {
                        int value = ("" + ((IntConstant) e1).getValue()).equals(((StringConstant) e2).getValue()) ? 1 : 0;
                        Expression ret = new IntConstant(e.getLineNumber(), value);
                        ret.setTypeData(intType);
                        return ret;
                    }
                    Expression cast = new Cast(e.getLineNumber(), "String", e1);
                    cast.setTypeData(LCPLString);
                    ((BinaryOp) e).setE1(cast);
                } else {
                    if (FOLDING && e2 instanceof IntConstant && e1 instanceof StringConstant) {
                        int value = ((StringConstant) e1).getValue().equals("" + ((IntConstant) e2).getValue()) ? 1 : 0;
                        Expression ret = new IntConstant(e.getLineNumber(), value);
                        ret.setTypeData(intType);
                        return ret;
                    }
                    Expression cast = new Cast(e.getLineNumber(), "String", e2);
                    cast.setTypeData(LCPLString);
                    ((BinaryOp) e).setE2(cast);
                }
                e.setTypeData(intType);
            }
            else if (e1.getType().equals("String") && e2.getType().equals("String")) {
                if (FOLDING && e1 instanceof StringConstant && e2 instanceof StringConstant) {
                    int value = ((StringConstant) e1).getValue().equals(((StringConstant) e2).getValue()) ? 1 : 0;
                    Expression ret = new IntConstant(e.getLineNumber(), value);
                    ret.setTypeData(intType);
                    return ret;
                }
                e.setTypeData(intType);
            }
            else if (e1.getType().equals("Int") || e2.getType().equals("Int"))
                throw new LCPLCastException(((TreeNode) e).getLineNumber(), "Invalid type of parameters for == expression");
            else if (e1.getType().equals("(none)") || e2.getType().equals("(none)")) {
                throw new LCPLCastException(((TreeNode) e).getLineNumber(), "Invalid type of parameters for == expression");
            }
            else {
                if (FOLDING) {
                    if (e1 instanceof StringConstant && e2.getType().equals("void") || e2 instanceof StringConstant && e1.getType().equals("void")) {
                        Expression ret = new IntConstant(e.getLineNumber(), 0);
                        ret.setTypeData(intType);
                        return ret;
                    }
                    if (e1.getType().equals("void") && e2.getType().equals("void")) {
                        Expression ret = new IntConstant(e.getLineNumber(), 1);
                        ret.setTypeData(intType);
                        return ret;
                    }
                }
                if (!e1.getType().equals("Object") && !(e1.getType().equals("void"))) {
                    Cast cast1 = new Cast(e1.getLineNumber(), "Object", e1);
                    cast1.setTypeData(LCPLObject);
                    ((BinaryOp) e).setE1(cast1);
                }
                if (!e2.getType().equals("Object") && !(e2.getType().equals("void"))) {
                    Cast cast2 = new Cast(e2.getLineNumber(), "Object", e2);
                    cast2.setTypeData(LCPLObject);
                    ((BinaryOp) e).setE2(cast2);
                }
                e.setTypeData(intType);
            }
        }
        else if (e instanceof IfStatement) {
            IfStatement is = (IfStatement) e;

            is.setCondition(solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, is.getCondition()));
            if (!is.getCondition().getType().equals("Int"))
                throw new LCPLConditionException(((TreeNode) e).getLineNumber(), "If condition must be Int");
            is.setIfExpr(solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, is.getIfExpr()));
            is.setThenExpr(solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, is.getThenExpr()));

            if (FOLDING && is.getCondition() instanceof IntConstant) {
                if (((IntConstant) is.getCondition()).getValue() == 0) {
                    return is.getThenExpr();
                }
                return is.getIfExpr();
            }

            // Set expression type
            if (is.getThenExpr() == null)
                e.setTypeData(noType);
            else if (is.getIfExpr().getType().equals(is.getThenExpr().getType()))
                e.setTypeData(is.getIfExpr().getTypeData());
            else if (is.getIfExpr().getType().equals("void"))
                e.setTypeData(is.getThenExpr().getTypeData());
            else if (is.getThenExpr().getType().equals("void"))
                e.setTypeData(is.getIfExpr().getTypeData());
            else if (castIsPossible(is.getIfExpr().getType(), is.getThenExpr().getType())) {
                if (!is.getThenExpr().getType().equals("Int"))
                    e.setTypeData(is.getIfExpr().getTypeData());
                else
                    e.setTypeData(noType);
            }
            else if (castIsPossible(is.getThenExpr().getType(), is.getIfExpr().getType())) {
                if (!is.getIfExpr().getType().equals("Int"))
                    e.setTypeData(is.getThenExpr().getTypeData());
                else
                    e.setTypeData(noType);
            }
            else
                e.setTypeData(noType);
        }
        else if (e instanceof LessThan) {
            Expression e1 = ((BinaryOp) e).getE1();
            Expression e2 = ((BinaryOp) e).getE2();
            e1 = solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, e1);
            e2 = solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, e2);
            if (!e1.getType().equals("Int"))
                throw new LCPLCastException(((TreeNode) e).getLineNumber(), "Cannot convert a value of type " + e1.getType() + " into Int");
            if (!e2.getType().equals("Int"))
                throw new LCPLCastException(((TreeNode) e).getLineNumber(), "Cannot convert a value of type " + e2.getType() + " into Int");
            if (FOLDING && e1 instanceof IntConstant && e2 instanceof IntConstant) {
                int value = ((IntConstant) e1).getValue() < ((IntConstant) e2).getValue() ? 1 : 0;
                Expression ret = new IntConstant(e.getLineNumber(), value);
                ret.setTypeData(intType);
                return ret;
            }
            e.setTypeData(intType);
        }
        else if (e instanceof LessThanEqual) {
            Expression e1 = ((BinaryOp) e).getE1();
            Expression e2 = ((BinaryOp) e).getE2();
            e1 = solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, e1);
            e2 = solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, e2);
            if (!e1.getType().equals("Int"))
                throw new LCPLCastException(((TreeNode) e).getLineNumber(), "Cannot convert a value of type " + e1.getType() + " into Int");
            if (!e2.getType().equals("Int"))
                throw new LCPLCastException(((TreeNode) e).getLineNumber(), "Cannot convert a value of type " + e2.getType() + " into Int");
            if (FOLDING && e1 instanceof IntConstant && e2 instanceof IntConstant) {
                int value = ((IntConstant) e1).getValue() <= ((IntConstant) e2).getValue() ? 1 : 0;
                Expression ret = new IntConstant(e.getLineNumber(), value);
                ret.setTypeData(intType);
                return ret;
            }
            e.setTypeData(intType);
        }
        // Local definition
        else if (e instanceof LocalDefinition) {
            LocalDefinition ld = (LocalDefinition) e;
            if (ld.getType().equals("String")) {
                ld.setVariableType(LCPLString);
            } else if (ld.getType().equals("Int")) {
                ld.setVariableType(intType);
            } else if (ld.getType().equals("Object")) {
                ld.setVariableType(LCPLObject);
            } else if (ld.getType().equals("IO")) {
                ld.setVariableType(LCPLIO);
            } else {
                Type type = mapStringClass.get(ld.getType());
                if (type == null)
                    throw new LCPLTypeNotFoundException(((TreeNode) e).getLineNumber(), "Class " + ld.getType() + " not found.");
                ld.setVariableType(type);
            }
            locals = new TreeMap<String, Variable>(locals);

            solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, ld.getInit());
            locals.put(ld.getName(), ld);

            if (ld.getInit() != null && !ld.getType().equals(ld.getInit().getType())) {
                if (!castIsPossible(ld.getType(), ld.getInit().getType())) {
                    String type = ld.getInit().getType();
                    throw new LCPLCastException(((TreeNode) e).getLineNumber(), "Cannot convert a value of type " + type + " into " + ld.getType());
                } else if (!ld.getInit().getType().equals("void") && ld.getTypeData() != ld.getInit().getTypeData()) {
                    Cast cast = new Cast(ld.getLineNumber(), ld.getType(), ld.getInit());
                    cast.setTypeData(ld.getVariableType());
                    ld.setInit(cast);
                }
            }

            solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, ld.getScope());
            ld.setTypeData(ld.getScope().getTypeData());
        }
        else if (e instanceof LogicalNegation) {
            LogicalNegation ln = (LogicalNegation) e;
            Expression e1 = solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, ((UnaryOp) ln).getE1());
            if (!e1.getType().equals("Int")) {
                String type = e1.getType();
                throw new LCPLCastException(((TreeNode) e).getLineNumber(), "Cannot convert a value of type " + type + " into Int");
            }
            if (FOLDING && e1 instanceof IntConstant) {
                int value = ((IntConstant) e1).getValue() == 0 ? 1 : 0;
                Expression ret = new IntConstant(e.getLineNumber(), value);
                ret.setTypeData(intType);
                return ret;
            }
            e.setTypeData(intType);
        }
        else if (e instanceof Multiplication) {
            Expression e1 = ((BinaryOp) e).getE1();
            Expression e2 = ((BinaryOp) e).getE2();
            e1 = solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, e1);
            e2 = solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, e2);
            if (!e1.getType().equals("Int"))
                throw new LCPLCastException(((TreeNode) e).getLineNumber(), "Cannot convert a value of type " + e1.getType() + " into Int");
            if (!e2.getType().equals("Int"))
                throw new LCPLCastException(((TreeNode) e).getLineNumber(), "Cannot convert a value of type " + e2.getType() + " into Int");
            if (FOLDING && e1 instanceof IntConstant && e2 instanceof IntConstant) {
                int value = ((IntConstant) e1).getValue() * ((IntConstant) e2).getValue();
                Expression ret = new IntConstant(e.getLineNumber(), value);
                ret.setTypeData(intType);
                return ret;
            }
            e.setTypeData(intType);
        }
        else if (e instanceof NewObject) {
            NewObject obj = (NewObject) e;
            LCPLClass type = mapStringClass.get(obj.getType());
            if (type == null) {
                if (obj.getType().equals("Int"))
                    throw new LCPLTypeNotFoundException(((TreeNode) e).getLineNumber(), "Illegal instruction : new Int");
                else
                    throw new LCPLTypeNotFoundException(((TreeNode) e).getLineNumber(), "Class " + obj.getType() + " not found.");
            }
            e.setTypeData(type);
        }
        else if (e instanceof StaticDispatch) {
            StaticDispatch sd = (StaticDispatch) e;

            Expression obj = ((BaseDispatch) sd).getObject();
            solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, obj);

            LCPLClass it = mapStringClass.get(sd.getType());
            if (it == null)
                throw new LCPLTypeNotFoundException(((TreeNode) e).getLineNumber(), "Class " + sd.getType() + " not found.");

            if (!castIsPossible(sd.getType(), obj.getType()))
                throw new LCPLCastException(((TreeNode) e).getLineNumber(), "Cannot convert from " + obj.getType() + " to " + sd.getType() + " in StaticDispatch");


            sd.setSelfType(it);

            // Find the method
            Method met = null;
            while (true) {
                boolean found = false;
                for (Feature ft : it.getFeatures()) {
                    if (ft instanceof Method && ((Method) ft).getName().equals(sd.getName())) {
                        met = (Method) ft;
                        found = true;
                        break;
                    }
                }
                if (found || it.getParent() == null)
                    break;
                it = mapStringClass.get(it.getParent());
            }

            if (met != null) {
                e.setTypeData(met.getReturnTypeData());
                ((BaseDispatch) e).setMethod(met);
            }
            else
                throw new LCPLMethodNotFoundException(((TreeNode) e).getLineNumber(), "Method " + sd.getName() + " not found in class " + cl.getName());

            // Check that formal and actual parameters match
            List<Expression> arguments = ((BaseDispatch) sd).getArguments();
            for (int i = 0; i < arguments.size(); i++) {
                Expression expr = arguments.get(i);
                solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, expr);
                if (i == met.getParameters().size())
                    throw new LCPLFormalParamNumException(((TreeNode) e).getLineNumber(), "Too many arguments in method call " + met.getName());
                FormalParam fp = met.getParameters().get(i);
                if (!expr.getType().equals(fp.getType())) {
                    if (!castIsPossible(fp.getType(), expr.getType())) {
                        String type = expr.getType();
                        throw new LCPLCastException(((TreeNode) expr).getLineNumber(), "Cannot convert a value of type " + type + " into " + fp.getType());
                    } if (!expr.getType().equals("void")) {
                        Cast cast = new Cast(expr.getLineNumber(), fp.getType(), expr);
                        cast.setTypeData(fp.getVariableType());
                        arguments.set(i, cast);
                    }
                }
            }
            if (arguments.size() < met.getParameters().size())
                throw new LCPLFormalParamNumException(((TreeNode) e).getLineNumber(), "Not enough arguments in method call " + met.getName());
        }
        else if (e instanceof SubString) {
            Expression stringExpr = ((SubString) e).getStringExpr();
            Expression startPosition = ((SubString) e).getStartPosition();
            Expression endPosition = ((SubString) e).getEndPosition();
            ((SubString) e).setStringExpr(solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, stringExpr));
            ((SubString) e).setStartPosition(solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, startPosition));
            ((SubString) e).setEndPosition(solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, endPosition));
            if (FOLDING && ((SubString) e).getStringExpr() instanceof StringConstant && ((SubString) e).getStartPosition() instanceof IntConstant && ((SubString) e).getEndPosition() instanceof IntConstant) {
                int ind1 = ((IntConstant) ((SubString) e).getStartPosition()).getValue();
                int ind2 = ((IntConstant) ((SubString) e).getEndPosition()).getValue();
                String s = ((StringConstant) (((SubString) e).getStringExpr())).getValue();
                if (ind1 >= 0 && ind2 < s.length() && ind1 <= ind2) {
                    String value = s.substring(ind1, ind2);
                    Expression ret = new StringConstant(e.getLineNumber(), value);
                    ret.setTypeData(LCPLString);
                    return ret;
                }
            }
            e.setTypeData(LCPLString);
        }
        else if (e instanceof Subtraction) {
            Expression e1 = ((BinaryOp) e).getE1();
            Expression e2 = ((BinaryOp) e).getE2();
            e1 = solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, e1);
            e2 = solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, e2);
            if (!e1.getType().equals("Int"))
                throw new LCPLCastException(((TreeNode) e).getLineNumber(), "Cannot convert a value of type " + e1.getType() + " into Int");
            if (!e2.getType().equals("Int"))
                throw new LCPLCastException(((TreeNode) e).getLineNumber(), "Cannot convert a value of type " + e2.getType() + " into Int");
            if (FOLDING && e1 instanceof IntConstant && e2 instanceof IntConstant) {
                int value = ((IntConstant) e1).getValue() - ((IntConstant) e2).getValue();
                Expression ret = new IntConstant(e.getLineNumber(), value);
                ret.setTypeData(intType);
                return ret;
            }
            e.setTypeData(intType);
        }
        else if (e instanceof Symbol) {
            Symbol s = (Symbol) e;
            Variable ret;
            String [] ss = s.getName().split("self.");
            if (ss[0].isEmpty()) {
                s.setName(ss[1]);
                ret = attributes.get(s.getName());
                if (ret == null)
                    throw new LCPLSymbolNotFoundException(((TreeNode) s).getLineNumber(), "Attribute " + s.getName() + " not found in class " + cl.getName());
            }
            else {
                ret = locals.get(s.getName());
                if (ret == null) {
                    ret = formals.get(s.getName());
                    if (ret == null && selfie != null && selfie.getName().equals(s.getName()))
                        ret = selfie;
                    if (ret == null) {
                        ret = attributes.get(s.getName());
                        if (ret == null)
                            throw new LCPLSymbolNotFoundException(((TreeNode) s).getLineNumber(), "Attribute " + s.getName() + " not found in class " + cl.getName());
                    }
                }
            }
            s.setVariable(ret);
            if (ret instanceof LocalDefinition)
                e.setTypeData(((LocalDefinition) ret).getVariableType());
            else if (ret instanceof FormalParam)
                e.setTypeData(((FormalParam) ret).getVariableType());
            else if (ret instanceof Attribute) {
                e.setTypeData(((Attribute) ret).getTypeData());
            }
        }
        else if (e instanceof UnaryMinus) {
            UnaryMinus um = (UnaryMinus) e;
            Expression e1 = solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, ((UnaryOp) um).getE1());
            if (!(e1.getType().equals("Int"))) {
                String type = ((UnaryOp) um).getE1().getType();
                throw new LCPLCastException(((TreeNode) e).getLineNumber(), "Cannot convert a value of type " + type + " into Int");
            }
            if (FOLDING && e1 instanceof IntConstant) {
                int value = -((IntConstant) e1).getValue();
                Expression ret = new IntConstant(e.getLineNumber(), value);
                ret.setTypeData(intType);
                return ret;
            }
            e.setTypeData(intType);
        }
        else if (e instanceof WhileStatement) {
            WhileStatement ws = (WhileStatement) e;
            ws.setCondition(solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, ws.getCondition()));
            ws.setLoopBody(solveTypeAndVariable(cl, selfie, method, attributes, formals, locals, ws.getLoopBody()));
            if (!ws.getCondition().getType().equals("Int"))
                throw new LCPLConditionException(((TreeNode) e).getLineNumber(), "If condition must be Int");
            if (FOLDING && ws.getCondition() instanceof IntConstant) {
                if (((IntConstant) ws.getCondition()).getValue() == 0) {
                    ws.setLoopBody(null);
                }
            }
            e.setTypeData(noType);
        }
        return e;
    }

    private void solveTypesAndVariables(Program p) throws LCPLException {
        TreeMap<String, Variable> formals = new TreeMap<String, Variable>();

        // Resolve methods part I
        // Self, parent and return type
        for (LCPLClass cl : p.getClasses()) {
            for (Feature ft : cl.getFeatures())
                if (ft instanceof Method) {
                    Method met = (Method) ft;
                    // Self
                    FormalParam selfFP = new FormalParam("self", cl.getName());
                    selfFP.setVariableType(cl);
                    met.setSelf(selfFP);

                    // Parent
                    met.setParent(cl);

                    // Return type
                    if (met.getReturnType().equals("String"))
                        met.setReturnTypeData(LCPLString);
                    else if (met.getReturnType().equals("Int"))
                        met.setReturnTypeData(intType);
                    else if (met.getReturnType().equals("Object"))
                        met.setReturnTypeData(LCPLObject);
                    else if (met.getReturnType().equals("IO"))
                        met.setReturnTypeData(LCPLIO);
                    else if (met.getReturnType().equals("void"))
                        met.setReturnTypeData(noType);
                    else
                        met.setReturnTypeData(mapStringClass.get(met.getReturnType()));

                    for (FormalParam fp : met.getParameters()) {
                        if (fp.getType().equals("String"))
                            fp.setVariableType(LCPLString);
                        else if (fp.getType().equals("Int"))
                            fp.setVariableType(intType);
                        else if (fp.getType().equals("Object"))
                            fp.setVariableType(LCPLObject);
                        else if (fp.getType().equals("IO"))
                            fp.setVariableType(LCPLIO);
                        else {
                            Type type = mapStringClass.get(fp.getType());
                            if (type == null)
                                throw new LCPLTypeNotFoundException(((TreeNode) fp).getLineNumber(), "Class " + fp.getType() + " not found.");
                            fp.setVariableType(type);
                        }
                        formals.put(fp.getName(), fp);
                    }
                }
        }

        for (LCPLClass cl : p.getClasses()) {
            TreeMap<String, Variable> attributes = new TreeMap<String, Variable>();

            // Add parent data
            cl.setParentData(mapStringClass.get(cl.getParent()));

            // Add parameter self
            Attribute self = new Attribute(0, "self", cl.getName(), null);
            self.setTypeData(cl);
            //attributes.put("self", self);

            FormalParam selfFP = new FormalParam("self", cl.getName());
            selfFP.setVariableType(cl);

            // Get all attributes from this class and parents
            LCPLClass it = cl;
            while (true) {
                for (Feature ft : it.getFeatures()) {
                    if (ft instanceof Attribute) {
                        Attribute attr = (Attribute) ft;
                        if (attr.getType().equals("String"))
                            attr.setTypeData(LCPLString);
                        else if (attr.getType().equals("Int"))
                            attr.setTypeData(intType);
                        else if (attr.getType().equals("Object"))
                            attr.setTypeData(LCPLObject);
                        else if (attr.getType().equals("IO"))
                            attr.setTypeData(LCPLIO);
                        else {
                            Type type = mapStringClass.get(attr.getType());
                            if (type == null)
                                throw new LCPLTypeNotFoundException(((TreeNode) attr).getLineNumber(), "Class " + attr.getType() + " not found.");
                            attr.setTypeData(type);
                        }
                        Variable res = attributes.put(attr.getName(), attr);
                        if (res != null) {
                            if (it == cl)
                                throw new LCPLDupAttributeException(attr.getLineNumber(), "An attribute with the same name already exists in class " + it.getName() + " : " + attr.getName());
                            else
                                throw new LCPLDupAttributeException(1, "Attribute " + attr.getName() + " is redefined.");
                        }
                    }
                }
                if (it.getParent().equals("Object") || it.getParent().equals("IO"))
                    break;
                it = mapStringClass.get(it.getParent());
            }

            // Resolve init expressions for attributes
            for (Feature ft : cl.getFeatures()) {
                if (ft instanceof Attribute) {
                    Attribute a = (Attribute) ft;
                        FormalParam fp = new FormalParam("self", a.getType());
                    // AttrInitSelf
                    if (a.getInit() != null) {
                        fp.setVariableType(cl);
                        a.setAttrInitSelf(fp);
                    }

                    a.setInit(solveTypeAndVariable(cl, fp, null, attributes, new TreeMap<String, Variable>(), new TreeMap<String, Variable>(), a.getInit()));
                    if (a.getInit() != null && !a.getType().equals(a.getInit().getType())) {
                        if (!castIsPossible(a.getType(), a.getInit().getType())) {
                            String type = a.getInit().getType();
                            throw new LCPLCastException(((TreeNode) a).getLineNumber(), "Cannot convert a value of type " + type + " into " + a.getType());
                        } else if (!a.getInit().getType().equals("void")) {
                            Cast cast = new Cast(((TreeNode) a).getLineNumber(), a.getType(), a.getInit());
                            cast.setTypeData(a.getTypeData());
                            a.setInit(cast);
                        }
                    }
                }
            }

            // Resolve methods part II
            // Overload, body
            for (Feature ft : cl.getFeatures()) {
                if (ft instanceof Method) {
                    Method met = (Method) ft;

                    // Check for overloaded methods
                    it = mapStringClass.get(cl.getParent());
                    boolean found = false;
                    while (it != null && !found) {
                        for (Feature ft2 : it.getFeatures())
                            if (ft2 instanceof Method) {
                                Method met2 = (Method) ft2;
                                if (met2.getName().equals(met.getName())) {
                                    if (!met.getReturnType().equals(met2.getReturnType()))
                                        throw new LCPLBadOverMethodException(((TreeNode) ft).getLineNumber(), "Return type changed in overloaded method.");
                                    if (met.getParameters().size() != met2.getParameters().size())
                                        throw new LCPLBadOverMethodException(((TreeNode) ft).getLineNumber(), "Overloaded method has a different number of parameters");
                                    for (int i = 0; i < met.getParameters().size(); i++)
                                        if (!met.getParameters().get(i).getType().equals(met2.getParameters().get(i).getType()))
                                            throw new LCPLBadOverMethodException(((TreeNode) ft).getLineNumber(), "Parameter " + met.getParameters().get(i).getName() + " has a different type in overloaded method.");
                                    found = true;
                                    break;
                                }
                            }
                        if (it.getParent() != null)
                            it = mapStringClass.get(it.getParent());
                        else
                            it = null;
                    }

                    // Body
                    formals.clear();

                    for (FormalParam fp : met.getParameters()) {
                        if (fp.getType().equals("String"))
                            fp.setVariableType(LCPLString);
                        else if (fp.getType().equals("Int"))
                            fp.setVariableType(intType);
                        else if (fp.getType().equals("Object"))
                            fp.setVariableType(LCPLObject);
                        else if (fp.getType().equals("IO"))
                            fp.setVariableType(LCPLIO);
                        else {
                            Type type = mapStringClass.get(fp.getType());
                            if (type == null)
                                throw new LCPLTypeNotFoundException(((TreeNode) fp).getLineNumber(), "Class " + fp.getType() + " not found.");
                            fp.setVariableType(type);
                        }
                        formals.put(fp.getName(), fp);
                    }

                    solveTypeAndVariable(cl, met.getSelf(), met, attributes, formals, new TreeMap<String, Variable>(), met.getBody());
                    if (!met.getReturnType().equals("void")) {
                        List<Expression> list = ((Block) met.getBody()).getExpressions();
                        if (!castIsPossible(met.getReturnType(), met.getBody().getType())) {
                            String type = met.getBody().getType();
                            if (list.size() == 0)
                                throw new LCPLCastException(0, "Cannot convert a value of type " + type + " into " + met.getReturnType());
                            else
                                throw new LCPLCastException(((TreeNode) list.get(list.size() - 1)).getLineNumber(), "Cannot convert a value of type " + type + " into " + met.getReturnType());
                        }

                        if (!met.getBody().getType().equals("void") && met.getReturnTypeData() != met.getBody().getTypeData()) {
                            Cast cast = new Cast(list.get(list.size() - 1).getLineNumber(), met.getReturnType(), met.getBody());
                            cast.setTypeData(met.getReturnTypeData());
                            met.setBody(cast);
                        }
                    }
                }
            }
        }
    }

    public void parse(Program p) throws LCPLException {
        initBaseTypes(p);
        testUniqueClassNames(p);
        testParentClass(p);
        testDupMethods(p);
        solveTypesAndVariables(p);
        testNoMainClass(p);

        // Add special classes
        List<LCPLClass> classes = p.getClasses();
        classes.add(LCPLObject);
        classes.add(LCPLIO);
        classes.add(LCPLString);
    }
}
