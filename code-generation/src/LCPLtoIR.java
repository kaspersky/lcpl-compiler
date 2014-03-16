import ro.pub.cs.lcpl.*;
import java.util.HashMap;
import java.util.LinkedList;

// Rezultat ale evaluarii unei expresii
// Intoarce codul emis si variabila in care este pastrat rezultatul
class ExprCode {
    public String code;
    public String retE;

    public ExprCode(String code, String retE) {
        this.code = code;
        this.retE = retE;
    }

    public ExprCode() {
        this("", "");
    }
}

public class LCPLtoIR {
    public Program p;
    public int varNum = 0;
    public int labelNum = 0;
    public int stringNum = 0;
    public int stringTNum = 0;
    public String antet = "";

    // Holds mappings for return expressions with their corresponding labels
    public HashMap<String, String> retELabel = new HashMap<String, String>();

    // Holds the current label
    public String currentLabel = "";

    // Holds the class type definitions ( %struct.TClassname )
    public String classTypes = "";

    // Holds the symbol pointers during method parse
    public String symbols = "";

    // Holds the string global constants
    public String globalStrings = "";

    public String pself;
    public HashMap<String, LCPLClass> stringClass = null;

    // Will hold the class instance while parsing it
    // for retrieving attributes
    public LCPLClass lcplclass;

    // Methods of a class, including the inherited methods, but not including the constructor
    public HashMap<LCPLClass, LinkedList<Method> > classMethod = new HashMap<LCPLClass, LinkedList<Method>>();

    // Attributes of a class, first inherited ones
    public HashMap<LCPLClass, LinkedList<Attribute>> classAttr = new HashMap<LCPLClass, LinkedList<Attribute>>();

    // Symbol table
    public HashMap<Variable, String> sTable = new HashMap<Variable, String>();

    public LCPLtoIR(Program p) {
        this.p = p;
    }

    public void getClassMethods(LCPLClass cl) {
        LinkedList<Method> methods = classMethod.get(cl);
        if (methods != null)
            return;

        LinkedList<Method> pmethods;

        if (cl.getParentData() != null) {
            getClassMethods(cl.getParentData());

            pmethods = classMethod.get(cl.getParentData());
        }
        else {
            pmethods = new LinkedList<Method>();
        }

        methods = new LinkedList<Method>(pmethods);
        LinkedList<Method> cmethods = new LinkedList<Method>();

        for (Feature ft : cl.getFeatures())
            if (ft instanceof Method) {
                int index = -1;
                for (int i = 0; i < methods.size(); i++)
                    if (methods.get(i).getName().equals(((Method) ft).getName())) {
                        index = i;
                        break;
                    }
                if (index == -1) {
                    //if (cl.getName().equals("IO"))
                    //    cmethods.add(0, (Method) ft);
                    //else
                        cmethods.add((Method) ft);
                }
                else
                    methods.set(index, (Method) ft);
            }

        for (Method m : cmethods)
            methods.add(m);
        classMethod.put(cl, methods);
    }

    public void genStringClass() {
        stringClass = new HashMap<String, LCPLClass>();
        for (LCPLClass cl : p.getClasses()) {
            stringClass.put(cl.getName(), cl);
            getClassMethods(cl);

            // Count attributes
            LinkedList<Attribute> attributes = new LinkedList<Attribute>();
            LCPLClass it = cl;
            while (it != null) {
                LinkedList<Attribute> tmpattr = new LinkedList<Attribute>();
                for (Feature ft : it.getFeatures())
                    if (ft instanceof Attribute)
                        tmpattr.add((Attribute) ft);

                it = it.getParentData();
                for (Attribute a : attributes)
                    tmpattr.add(a);
                attributes = tmpattr;
            }
            LinkedList<Method> cmethods = classMethod.get(cl);
            LinkedList<Attribute> cattributes = new LinkedList<Attribute>(attributes);
            classAttr.put(cl, cattributes);
        }
    }

    public void antett()
    {
        antet += "%struct.TObject = type { %struct.__lcpl_rtti* }\n";
        antet += "%struct.TString = type { %struct.__lcpl_rtti*, i32, i8* }\n";
        antet += "%struct.TIO = type { %struct.__lcpl_rtti* }\n";
        antet += "%struct.__lcpl_rtti = type { %struct.TString*, i32, %struct.__lcpl_rtti*, [0 x i8*] }\n";
        antet += "@RObject = external global %struct.__lcpl_rtti\n";
        antet += "@RString = external global %struct.__lcpl_rtti\n";
        antet += "@RIO = external global %struct.__lcpl_rtti\n";
        antet += "declare void @Object_init(%struct.TObject*)\n";
        antet += "declare void @M6_Object_abort(%struct.TObject*)\n";
        antet += "declare %struct.TString* @M6_Object_typeName(%struct.TObject*)\n";
        antet += "declare %struct.TObject* @M6_Object_copy(%struct.TObject*)\n";
        antet += "declare void @IO_init(%struct.TIO*)\n";
        antet += "declare %struct.TString* @M2_IO_in(%struct.TIO*)\n";
        antet += "declare %struct.TIO* @M2_IO_out(%struct.TIO*, %struct.TString*)\n";
        antet += "declare i8* @__lcpl_new(%struct.__lcpl_rtti*)\n";
        antet += "declare i8* @__lcpl_cast(i8*, %struct.__lcpl_rtti*)\n";
        antet += "declare void @__lcpl_checkNull(i8*)\n";
        antet += "declare %struct.TString* @__lcpl_intToString(i32)\n";
        antet += "declare %struct.TString* @M6_String_concat(%struct.TString* %self, %struct.TString* %other)\n";
        antet += "declare %struct.TString* @M6_String_substring(%struct.TString* %self, i32 %start, i32 %final)\n";
        antet += "declare i32 @M6_String_equal(%struct.TString* %self, %struct.TString* %other)\n";

        // Initialize global empty string
        antet += "@.gsempty = constant [1 x i8] c\"\\00\"\n";
        antet += "@SEmpty = global %struct.TString {%struct.__lcpl_rtti* @RString, i32 0, i8* getelementptr ([1 x i8]* @.gsempty, i32 0, i32 0)}\n";
    }

    public void emit() {
        parseProgram();
    }

    public void parseProgram() {
        genStringClass();
        antett();
        for (LCPLClass cl : p.getClasses()) {
            parseClass(cl);
        }
        // Create startup
        antet = classTypes + antet + globalStrings;
        antet += createStartup();
    }

    public void parseClass(LCPLClass cl) {
        if (!cl.getName().equals("Object") && !cl.getName().equals("String") && !cl.getName().equals("IO")) {
            // Update current class
            lcplclass = cl;

            LinkedList<Method> methods = classMethod.get(cl);
            int nm = methods.size() + /*Constructor*/ 1;

            // Create String containing class name
            String gs = "@.gs" + stringNum + " = constant [" + (cl.getName().length() + 1) + " x i8] c\"" + cl.getName() + "\\00\"\n";
            String nameS = "@N" + cl.getName() + " = global %struct.TString { %struct.__lcpl_rtti* @RString, i32 " + cl.getName().length() + ", i8* getelementptr ([" + (cl.getName().length() + 1) + " x i8]* " + "@.gs" + stringNum +", i32 0, i32 0) }\n";
            stringNum++;
            antet += gs;
            antet += nameS;

            // Create class type
            LinkedList<Attribute> cattributes = classAttr.get(cl);
            classTypes += "%struct.T" + cl.getName() + " = type { %struct.__lcpl_rtti*";
            for (Attribute attr : cattributes)
                classTypes += ", " + getIRType(attr.getType());
            classTypes += "}\n";

            // Create class rtti type
            String rttiType = getRTTIType(cl.getName());
            antet += rttiType + " = type { %struct.TString*, i32, " + getRTTIType(cl.getParentData().getName()) + "*, [" + nm + " x i8*] }\n";

            // Create class rtti
            antet += "@R" + cl.getName() + " = global " + rttiType + " { %struct.TString* @N" + cl.getName() + ", i32 " + 4 * (1 + classAttr.get(cl).size()) + ", " + getRTTIType(cl.getParentData().getName()) + "* " + "@R" + cl.getParentData().getName() + ", [" + nm + " x i8*] [";
            antet += "i8* bitcast (void (" + getIRType(cl.getName()) + ")* @" + cl.getName() + "_init to i8*), ";
            for (Method met : methods)
            {
                antet += "i8* bitcast (";
                antet += getIRType(met.getReturnType()) + " (";

                // Add self param
                antet += getIRType(met.getSelf().getType()) + ", ";

                // Add other params
                for (FormalParam fp : met.getParameters())
                    antet += getIRType(fp.getType()) + ", ";

                antet = antet.substring(0, antet.length() - 2) + ")* ";
                String metName = "@" + getIRMetType(met);
                if (metName.equals("@M_Object_copy"))
                    metName = "@M6_Object_copy";
                else if (metName.equals("@M_Object_typeName"))
                    metName = "@M6_Object_typeName";
                else if (metName.equals("@M_Object_abort"))
                    metName = "@M6_Object_abort";
                else if (metName.equals("@M_IO_in"))
                    metName = "@M2_IO_in";
                else if (metName.equals("@M_IO_out"))
                    metName = "@M2_IO_out";
                antet += metName + " to i8*), ";
            }
            antet = antet.substring(0, antet.length() - 2) + "]}\n";

            // Create class init
            String tmp = createClassInit(cl);
            antet += tmp;

            // Parse features
            for (Feature ft : cl.getFeatures()) {
                if (ft instanceof Method) {
                    tmp = parseMethod((Method) ft);
                    antet += tmp;
                }
            }
        }
    }

    public String parseMethod(Method met) {

        // Refresh symbol table
        sTable = new HashMap<Variable, String>();
        symbols = "";

        // Refresh return expressions - labels mappings
        retELabel = new HashMap<String, String>();

        String ret = "";

        ret += "define ";
        ret += getIRType(met.getReturnType()) + " @" + getIRMetType(met) + "(";

        // Add self param
        ret += getIRType(met.getSelf().getType()) + " ";
        ret += "%self, ";

        // Add other params
        for (FormalParam fp : met.getParameters())
        {
            ret += getIRType(fp.getType());
            ret += " %" + fp.getName() + ", ";
        }
        ret = ret.substring(0, ret.length() - 2) + ") {\n";

        // Add self param to symbol table
        Variable vself = met.getSelf();
        String pointer = "%e" + varNum++;
        symbols += pointer + " = alloca " + getIRType(met.getSelf().getType()) + "\n";
        symbols += "store " + getIRType(met.getSelf().getType()) + " %" + vself.getName() + ", " + getIRType(met.getSelf().getType()) + "* " + pointer + "\n";
        sTable.put(vself, pointer);
        pself = pointer;

        ExprCode ec = parseExpression(met.getBody());
        ret += symbols;
        ret += ec.code;

        ret += "ret " + getIRType(met.getReturnType());
        if (!getIRType(met.getReturnType()).equals("void"))
            ret += " " + ec.retE;

        ret += "\n}\n";

        return ret;
    }

    public ExprCode parseExpression(Expression expr) {
        if (expr instanceof Block) {
            Block b = (Block) expr;

            String code = "";
            String retE = "";

            for (Expression e : b.getExpressions()) {
                ExprCode ec = parseExpression(e);
                code += ec.code;
                retE = ec.retE;
            }
            retELabel.put(retE, currentLabel);
            return new ExprCode(code, retE);
        }
        else if (expr instanceof IntConstant) {
            String retE = "%e" + varNum++;
            String code = retE + " = add i32 0, " + ((IntConstant) expr).getValue() + "\n";
            retELabel.put(retE, currentLabel);
            return new ExprCode(code, retE);
        }
        else if (expr instanceof Multiplication) {
            Multiplication m = (Multiplication) expr;
            ExprCode e1 = parseExpression(m.getE1());
            ExprCode e2 = parseExpression(m.getE2());

            String retE = "";
            String code = "";
            retE += "%e" + varNum++;
            code += e1.code + e2.code + retE + " = mul i32" + e1.retE + ", " + e2.retE + "\n";

            retELabel.put(retE, currentLabel);
            return new ExprCode(code, retE);
        }
        else if (expr instanceof Subtraction) {
            Subtraction m = (Subtraction) expr;
            ExprCode e1 = parseExpression(m.getE1());
            ExprCode e2 = parseExpression(m.getE2());

            String retE = "";
            String code = "";
            retE += "%e" + varNum++;
            code += e1.code + e2.code + retE + " = sub i32" + e1.retE + ", " + e2.retE + "\n";

            retELabel.put(retE, currentLabel);
            return new ExprCode(code, retE);
        }
        else if (expr instanceof Division) {
            Division m = (Division) expr;
            ExprCode e1 = parseExpression(m.getE1());
            ExprCode e2 = parseExpression(m.getE2());

            String retE = "";
            String code = "";
            retE += "%e" + varNum++;
            code += e1.code + e2.code + retE + " = sdiv i32 " + e1.retE + ", " + e2.retE + "\n";

            retELabel.put(retE, currentLabel);
            return new ExprCode(code, retE);
        }
        else if (expr instanceof Addition) {
            Addition a = (Addition) expr;
            ExprCode e1 = parseExpression(a.getE1());
            ExprCode e2 = parseExpression(a.getE2());

            String retE = "";
            String code = "";
            if (!expr.getType().equals("String")) {
                retE += "%e" + varNum++;
                code += e1.code + e2.code + retE + " = add i32 " + e1.retE + ", " + e2.retE + "\n";
            }
            else {
                retE += "%e" + varNum++;
                code += e1.code + e2.code + retE + " = call %struct.TString* @M6_String_concat(%struct.TString* " + e1.retE + ", %struct.TString* " + e2.retE + ")\n";
            }

            retELabel.put(retE, currentLabel);
            return new ExprCode(code, retE);
        }
        else if (expr instanceof UnaryMinus) {
            UnaryMinus um = (UnaryMinus) expr;
            String code = "";
            String retE = "";

            ExprCode ec = parseExpression(um.getE1());
            code += ec.code;
            retE = "%e" + varNum++;
            code += retE + " = mul i32 -1, " + ec.retE + "\n";
            retELabel.put(retE, currentLabel);
            return new ExprCode(code, retE);
        }
        else if (expr instanceof LogicalNegation) {
            LogicalNegation ln = (LogicalNegation) expr;
            String code = "";
            String retE = "";

            ExprCode ec = parseExpression(ln.getE1());
            code += ec.code;

            code += "%e" + varNum++ + " = icmp eq i32 0, " + ec.retE + "\n";
            retE = "%e" + varNum++;
            code += retE + " = select i1 %e" + (varNum - 2) + ", i32 1, i32 0\n";
            retELabel.put(retE, currentLabel);
            return new ExprCode(code, retE);
        }
        else if (expr instanceof SubString) {
            String code = "";
            String retE = "";

            SubString ss = (SubString) expr;

            ExprCode ec = parseExpression(ss.getStringExpr());
            code += ec.code;
            ExprCode ec1 = parseExpression(ss.getStartPosition());
            code += ec1.code;
            ExprCode ec2 = parseExpression(ss.getEndPosition());
            code += ec2.code;

            retE = "%e" + varNum++;
            code += retE + " = call %struct.TString* @M6_String_substring(" + getIRType(ss.getStringExpr().getTypeData().getName()) + " " + ec.retE + ", " + getIRType(ss.getStartPosition().getTypeData().getName()) + " " + ec1.retE + ", " + getIRType(ss.getEndPosition().getTypeData().getName()) + " " + ec2.retE + ")\n";

            retELabel.put(retE, currentLabel);
            return new ExprCode(code, retE);
        }
        else if (expr instanceof StringConstant) {
            StringConstant sc = (StringConstant) expr;

            // Global string constant
            String gs = "@.gs" + stringNum + " = constant [" + (sc.getValue().length() + 1) + " x i8] c\"" + sc.getValue().replace("\\", "\\5C").replace("\"", "\\22").replace("\n", "\\0A").replace("\r", "\\0D") + "\\00\"\n";
            String gst = "@.gst" + stringTNum + " = global %struct.TString { %struct.__lcpl_rtti* @RString, i32 " + sc.getValue().length() + ", i8* getelementptr ([" + (sc.getValue().length() + 1) + " x i8]* " + "@.gs" + stringNum +", i32 0, i32 0) }\n";

            globalStrings += gs;
            globalStrings += gst;

            // Expression return
            String retE = "%e" + varNum++;
            String code = retE + " = load %struct.TString* @.gst" + stringTNum + "\n";
            stringNum++;
            stringTNum++;
            code = "";
            retE = "@.gst" + (stringTNum - 1);
            retELabel.put(retE, currentLabel);
            return new ExprCode(code, retE);
        }
        else if (expr instanceof Cast) {
            String code = "";
            String retE = "";

            Cast c = (Cast) expr;
            ExprCode ec = parseExpression(c.getE1());

            code += ec.code;

            if (c.getTypeData().getName().equals("String") && c.getE1().getTypeData().getName().equals("Int")) {
                retE = "%e" + varNum++;
                code += retE + " = call %struct.TString* @__lcpl_intToString(i32 " + ec.retE + ")\n";
            }
            else {
                // Check if cast is possible
                String sourcetoi8 = "%e" + varNum++;
                code += sourcetoi8 + " = bitcast " + getIRType(c.getE1().getTypeData().getName()) + " " + ec.retE + " to i8*\n";
                String checkcast = "%e" + varNum++;
                code += checkcast + " = call i8* @__lcpl_cast(i8* " + sourcetoi8 + ", %struct.__lcpl_rtti* bitcast (" + getRTTIType(c.getTypeData().getName()) + "* @R" + c.getTypeData().getName() + " to %struct.__lcpl_rtti*))\n";


                // Cast
                retE = "%e" + varNum++;
                code += retE + " = bitcast i8* " + checkcast + " to " + getIRType(c.getType()) + "\n";
            }

            retELabel.put(retE, currentLabel);
            return new ExprCode(code, retE);
        }
        else if (expr instanceof NewObject) {
            String code = "";
            String retE = "";

            NewObject no = (NewObject) expr;

            code += "%e" + varNum++ + " = call i8* @__lcpl_new(%struct.__lcpl_rtti* bitcast (" + getRTTIType(no.getTypeData().getName()) + "* @R" + no.getType() + " to %struct.__lcpl_rtti*))\n";
            retE = "%e" + varNum++;
            code += retE + " = bitcast i8* %e" + (varNum - 2) + " to " + getIRType(no.getType()) + "\n";

            retELabel.put(retE, currentLabel);
            return new ExprCode(code, retE);
        }
        else if (expr instanceof VoidConstant) {
            String code = "";
            String retE = "null";

            retELabel.put(retE, currentLabel);
            return new ExprCode(code, retE);
        }
        else if (expr instanceof LocalDefinition) {
            String code = "";
            String retE = "";

            LocalDefinition ld = (LocalDefinition) expr;

            symbols += registerSymbol(ld).code;

            // Assign the init expression result
            if (ld.getInit() != null) {
                ExprCode init = parseExpression(ld.getInit());
                code += init.code;
                String pointer = sTable.get(ld);
                String type = getVariableType(ld);
                code += "store " + getIRType(type) + " " + init.retE + ", " + getIRType(type) + "* " + pointer + "\n";
            }

            ExprCode ret = parseExpression(ld.getScope());
            code += ret.code;
            retE = ret.retE;

            retELabel.put(retE, currentLabel);
            return new ExprCode(code, retE);
        }
        else if (expr instanceof StaticDispatch) {
            String code = "";
            String retE = "";

            StaticDispatch sd = (StaticDispatch) expr;

            // Resolve object
            ExprCode obj = parseExpression(sd.getObject());
            code += obj.code;

            // Resolve arguments
            LinkedList<ExprCode> args = new LinkedList<ExprCode>();
            for (Expression e : sd.getArguments()) {
                ExprCode ec = parseExpression(e);
                args.add(ec);
                code += ec.code;
            }

            // Find method in vtable
            String dtype = sd.getSelfType().getName(); // parent class type
            String stype = sd.getObject().getTypeData().getName(); //self object type
            LCPLClass cl = stringClass.get(dtype);
            Method m = sd.getMethod();
            LinkedList<Method> mets = classMethod.get(cl);
            int offset = mets.size();
            for (offset = mets.size() - 1; offset >= 0; offset--) {
                if (mets.get(offset).getName().equals(m.getName()))
                    break;
            }
            offset++;

            // Load the method from rtti
            code += "%e" + varNum++ + " = getelementptr " + getRTTIType(dtype) + "* @R" + dtype + ", i32 0, i32 3\n";
            code += "%e" + varNum++ + " = bitcast [" + (mets.size() + 1) + " x i8*]* %e" + (varNum - 2) + " to i8**\n";
            code += "%e" + varNum++ + " = getelementptr i8** %e" + (varNum - 2) + ", i32 " + offset + "\n";
            code += "%e" + varNum++ + " = load i8** %e" + (varNum - 2) + "\n";

            // Create method signature
            String signature = "";
            signature += getIRType(m.getReturnType()) + " (";
            signature += getIRType(m.getSelf().getType());
            for (FormalParam fp : m.getParameters())
                signature += ", " + getIRType(fp.getType());
            signature += ")";

            String fn = "%e" + varNum++;
            code += fn + " = bitcast i8* %e" + (varNum - 2) + " to " + signature + "*\n";
            String self = "%e" + varNum++;
            code += self + " = bitcast " + getIRType(stype) + " " + obj.retE + " to " + getIRType(m.getSelf().getType()) + "\n";
            if (getIRType(m.getReturnType()).equals("void"))
                code += "call " + getIRType(m.getReturnType()) + " " + fn;
            else {
                retE = "%e" + varNum++;
                code += retE + " = call " + getIRType(m.getReturnType()) + " " + fn;
            }
            code += "(" + getIRType(m.getSelf().getType()) + " " + self;
            for (int i = 0; i < args.size(); i++)
                code += ", " + getIRType(m.getParameters().get(i).getType()) + " " + args.get(i).retE;
            code += ")\n";

            retELabel.put(retE, currentLabel);
            return new ExprCode(code, retE);
        }
        else if (expr instanceof Dispatch) {
            String code = "";
            String retE = "";

            Dispatch d = (Dispatch) expr;

            // Resolve object
            ExprCode obj = parseExpression(d.getObject());
            code += obj.code;

            // Check for null object
            code += "%e" + varNum++ + " = bitcast " + getIRType(d.getObject().getTypeData().getName()) + " " + obj.retE + " to i8*\n";
            code += "call void @__lcpl_checkNull(i8* %e" + (varNum - 1) + ")\n";

            // Resolve arguments
            LinkedList<ExprCode> args = new LinkedList<ExprCode>();
            for (Expression e : d.getArguments()) {
                ExprCode ec = parseExpression(e);
                args.add(ec);
                code += ec.code;
            }

            // Find method in vtable
            String stype = d.getObject().getTypeData().getName();
            LCPLClass cl = stringClass.get(stype);
            Method m = d.getMethod();
            LinkedList<Method> mets = classMethod.get(cl);
            int offset = mets.size();
            for (offset = mets.size() - 1; offset >= 0; offset--) {
                if (mets.get(offset).getName().equals(m.getName()))
                    break;
            }
            offset++;

            // Load the method from rtti
            code += "%e" + varNum++ + " = getelementptr " + getIRType(stype) + " " + obj.retE + ", i32 0, i32 0\n";
            code += "%e" + varNum++ + " = load %struct.__lcpl_rtti** %e" + (varNum - 2) + "\n";
            code += "%e" + varNum++ + " = getelementptr %struct.__lcpl_rtti* %e" + (varNum - 2) + ", i32 0, i32 3\n";
            code += "%e" + varNum++ + " = bitcast [0 x i8*]* %e" + (varNum - 2) + " to i8**\n";
            code += "%e" + varNum++ + " = getelementptr i8** %e" + (varNum - 2) + ", i32 " + offset + "\n";
            code += "%e" + varNum++ + " = load i8** %e" + (varNum - 2) + "\n";

            // Create method signature
            String signature = "";
            signature += getIRType(m.getReturnType()) + " (";
            signature += getIRType(m.getSelf().getType());
            for (FormalParam fp : m.getParameters())
                signature += ", " + getIRType(fp.getType());
            signature += ")";

            String fn = "%e" + varNum++;
            code += fn + " = bitcast i8* %e" + (varNum - 2) + " to " + signature + "*\n";
            String self = "%e" + varNum++;
            code += self + " = bitcast " + getIRType(stype) + " " + obj.retE + " to " + getIRType(m.getSelf().getType()) + "\n";
            if (getIRType(m.getReturnType()).equals("void")) {
                code += "call " + getIRType(m.getReturnType()) + " " + fn;
            }
            else {
                retE = "%e" + varNum++;
                code += retE + " = call " + getIRType(m.getReturnType()) + " " + fn;
            }
            code += "(" + getIRType(m.getSelf().getType()) + " " + self;
            for (int i = 0; i < args.size(); i++)
                code += ", " + getIRType(m.getParameters().get(i).getType()) + " " + args.get(i).retE;
            code += ")\n";

            retELabel.put(retE, currentLabel);
            return new ExprCode(code, retE);
        }
        else if (expr instanceof WhileStatement) {
            String code = "";
            String retE = "";

            WhileStatement ws = (WhileStatement) expr;

            // Make while labels
            String condLabel = "label" + labelNum++;
            String bodyLabel = "label" + labelNum++;
            String outLabel = "label" + labelNum++;

            // Make the condition
            code += "br label %" + condLabel + "\n";
            code += condLabel + ":\n";
            currentLabel = condLabel;
            ExprCode cond = parseExpression(ws.getCondition());
            code += cond.code;

            // Make the condition check
            code += "%e" + varNum++ + " = icmp eq i32 " + cond.retE + ", 0\n";
            code += "br i1 %e" + (varNum - 1) + ", label %" + outLabel + ", label %" + bodyLabel + "\n";

            // Create loop body
            code += bodyLabel + ":\n";
            currentLabel = bodyLabel;
            ExprCode body = parseExpression(ws.getLoopBody());
            code += body.code;

            // Jump to condition
            code += "br label %" + condLabel + "\n";

            // Outside label
            code += outLabel + ":\n";
            currentLabel = outLabel;

            return new ExprCode(code, retE);
        }
        else if (expr instanceof EqualComparison) {
            String code = "";
            String retE = "";

            EqualComparison eq = (EqualComparison) expr;

            ExprCode ec1 = parseExpression(eq.getE1());
            ExprCode ec2 = parseExpression(eq.getE2());

            code += ec1.code;
            code += ec2.code;

            if (eq.getE1().getTypeData().getName().equals("void") || eq.getE2().getTypeData().getName().equals("void"))
                code += "%e" + varNum++ + " = icmp eq %struct.TObject* " + ec1.retE + ", " + ec2.retE + "\n";
            else if (eq.getE1().getTypeData().getName().equals("String") || eq.getE2().getTypeData().getName().equals("String")) {
                String stringcomp = "%e" + varNum++;
                code += stringcomp + " = call i32 @M6_String_equal(%struct.TString* " + ec1.retE + ", %struct.TString* " + ec2.retE + ")\n";
                code += "%e" + varNum++ + " = icmp eq i32 1, " + stringcomp + "\n";
            }
            else
                code += "%e" + varNum++ + " = icmp eq " + getIRType(eq.getE1().getTypeData().getName()) + " " + ec1.retE + ", " + ec2.retE + "\n";

            retE = "%e" + varNum++;
            code += retE + " = zext i1 %e" + (varNum - 2) + " to i32\n";

            retELabel.put(retE, currentLabel);
            return new ExprCode(code, retE);
        }
        else if (expr instanceof LessThan) {
            String code = "";
            String retE = "";

            LessThan lt = (LessThan) expr;

            ExprCode ec1 = parseExpression(lt.getE1());
            ExprCode ec2 = parseExpression(lt.getE2());

            code += ec1.code;
            code += ec2.code;

            code += "%e" + varNum++ + " = icmp slt i32 " + ec1.retE + ", " + ec2.retE + "\n";
            retE = "%e" + varNum++;
            code += retE + " = zext i1 %e" + (varNum - 2) + " to i32\n";

            retELabel.put(retE, currentLabel);
            return new ExprCode(code, retE);
        }
        else if (expr instanceof IfStatement) {
            String code = "";
            String retE = "";

            IfStatement is = (IfStatement) expr;

            String thenLabel = "label" + labelNum++;
            String elseLabel = "label" + labelNum++;
            String outLabel = "label" + labelNum++;

            // Condition
            ExprCode cond = parseExpression(is.getCondition());
            code += cond.code;

            // Condition check
            String condE = "%e" + varNum++;
            code += condE + " = icmp eq i32 " + cond.retE + ", 0\n";
            code += "br i1 " + condE + ", label %" + elseLabel + ", label %" + thenLabel + "\n";

            // Then bloc
            code += thenLabel + ":\n";
            currentLabel = thenLabel;
            ExprCode thenBloc = parseExpression(is.getIfExpr());
            code += thenBloc.code;
            code += "br label %" + outLabel + "\n";

            // Else bloc
            ExprCode elseBloc = null;
            code += elseLabel + ":\n";
            if (is.getThenExpr() != null) {
                currentLabel = elseLabel;
                elseBloc = parseExpression(is.getThenExpr());
                code += elseBloc.code;
            }
            code += "br label %" + outLabel + "\n";

            // Out of ifstatement
            code += outLabel + ":\n";
            currentLabel = outLabel;
            retE = "%e" + varNum++;
            if (is.getThenExpr() != null && !expr.getTypeData().getName().equals("(none)")) {
                retELabel.put(retE, currentLabel);
                code += retE + " = phi " + getIRType(expr.getTypeData().getName()) + " [" + thenBloc.retE + ", %" + retELabel.get(thenBloc.retE) + "], [" + elseBloc.retE + ", %" + retELabel.get(elseBloc.retE) + "]\n";
            }

            return new ExprCode(code, retE);
        }
        else if (expr instanceof LessThanEqual) {
            String code = "";
            String retE = "";

            LessThanEqual lte = (LessThanEqual) expr;

            ExprCode ec1 = parseExpression(lte.getE1());
            ExprCode ec2 = parseExpression(lte.getE2());

            code += ec1.code;
            code += ec2.code;

            code += "%e" + varNum++ + " = icmp sle i32 " + ec1.retE + ", " + ec2.retE + "\n";
            retE = "%e" + varNum++;
            code += retE + " = zext i1 %e" + (varNum - 2) + " to i32\n";

            retELabel.put(retE, currentLabel);
            return new ExprCode(code, retE);
        }
        else if (expr instanceof Assignment) {
            String code = "";
            String retE = "";

            Assignment a = (Assignment) expr;

            ExprCode reg = registerSymbol(a.getSymbolData());
            symbols += reg.code;

            ExprCode ec = parseExpression(a.getE1());
            code += ec.code;

            code += "store " + getIRType(getVariableType(a.getSymbolData())) + " " + ec.retE + ", " + getIRType(getVariableType(a.getSymbolData())) + "* " + reg.retE + "\n";

            retE = "%e" + varNum++;
            code += retE + " = load " + getIRType(getVariableType(a.getSymbolData())) + "* " + reg.retE + "\n";

            retELabel.put(retE, currentLabel);
            return new ExprCode(code, retE);
        }
        else if (expr instanceof Symbol) {
            String code = "";
            String retE = "";

            Symbol s = (Symbol) expr;
            Variable v = s.getVariable();

            // Locate the variable in the symbol table
            symbols += registerSymbol(v).code;

            // Load the symbol
            String pointer = sTable.get(v);
            retE = "%e" + varNum++;
            code += retE + " = load " + getIRType(getSymbolType(s)) + "* " + pointer + "\n";

            retELabel.put(retE, currentLabel);
            return new ExprCode(code, retE);
        }

        return new ExprCode("", "");
    }

    public String createClassInit(LCPLClass cl) {
        // Refresh symbol table
        sTable = new HashMap<Variable, String>();
        symbols = "";

        // Refresh return expressions - labels mappings
        retELabel = new HashMap<String, String>();

        String ret = "";
        String type = getIRType(cl.getName());

        ret += "define void @" + cl.getName() + "_init(" + type + " %self) {\n";

        // Save self parameter
        pself = "%e" + varNum++;
        ret += pself + " = alloca " + type + "\n";
        ret += "store " + type + " %self, " + type + "* " + pself + "\n";

        // Call parent constructor
        String clexpr = "%e" + varNum++;
        ret += clexpr + " = load " + type + "* %e" + (varNum - 2) + "\n";
        ret += "%e" + varNum++ + " = bitcast " + type + " %e" + (varNum - 2) + " to " + getIRType(cl.getParentData().getName()) + "\n";
        ret += "call void @" + cl.getParent() + "_init(" + getIRType(cl.getParentData().getName()) + " %e" + (varNum - 1) + ")\n";

        // Init class attributes with default values
        LinkedList<Attribute> parentattr = classAttr.get(cl.getParentData());
        LinkedList<Attribute> attrs = classAttr.get(cl);
        for (int i = parentattr.size(); i < attrs.size(); i++) {
            ret += "%e" + varNum++ + " = getelementptr " + getIRType(cl.getName()) + " " + clexpr + ", i32 0, i32 " + (i + 1) + "\n";
            String attrType = attrs.get(i).getTypeData().getName();
            if (attrType.equals("Int"))
                ret += "store i32 0, i32* %e" + (varNum - 1) + "\n";
            else if (attrType.equals("String"))
                ret += "store %struct.TString* @SEmpty, %struct.TString** %e" + (varNum - 1) + "\n";
            else {
                ret += "store " + getIRType(attrType) + " null, " + getIRType(attrType) + "* %e" + (varNum - 1) + "\n";
            }
        }

        // Init class attributes with init expressions
        for (int i = parentattr.size(); i < attrs.size(); i++) {
            if (attrs.get(i).getInit() == null)
                continue;
            Expression init = attrs.get(i).getInit();
            symbols = "";
            ExprCode ec = parseExpression(init);
            ret += symbols;
            ret += ec.code;
            ret += "%e" + varNum++ + " = getelementptr " + getIRType(cl.getName()) + " " + clexpr + ", i32 0, i32 " + (i + 1) + "\n";
            ret += "store " + getIRType(attrs.get(i).getType()) + " " + ec.retE + ", " + getIRType(attrs.get(i).getType()) + "* %e" + (varNum - 1) + "\n";
        }

        ret += "ret void\n}\n";

        return ret;
    }

    public String createStartup() {
        String ret = "";

        ret += "define void @startup() {\n";
        ret += "%e" + varNum++ + " = call i8* @__lcpl_new(%struct.__lcpl_rtti* bitcast (%.RT" + "Main" + "* @R" + "Main" + " to %struct.__lcpl_rtti*))\n";
        ret += "%e" + varNum++ + " = bitcast i8* %e" + (varNum - 2) + " to " + "%struct.TMain" + "*\n";
        ret += "%e" + varNum++ + " = bitcast %struct.TMain* %e" + (varNum - 2) + " to i8*\n";
        ret += "call void @__lcpl_checkNull(i8* %e" + (varNum - 1) + ")\n";
        ret += "%e" + varNum++ + " = getelementptr %struct.TMain* %e" + (varNum - 3) + ", i32 0, i32 0\n";
        ret += "%e" + varNum++ + " = load %struct.__lcpl_rtti** %e" + (varNum - 2) + "\n";
        ret += "%e" + varNum++ + " = getelementptr %struct.__lcpl_rtti* %e" + (varNum - 2) + ", i32 0, i32 3\n";
        ret += "%e" + varNum++ + " = bitcast [0 x i8*]* %e" + (varNum - 2) + " to i8**\n";

        // Lookup for main method
        LinkedList<Method> methods = classMethod.get(stringClass.get("Main"));
        int offset = 1;
        for (Method met : methods) {
            if (met.getName().equals("main"))
                break;
            offset += 1;
        }

        ret += "%e" + varNum++ + " = getelementptr i8** %e" + (varNum - 2) + ", i32 " + offset + "\n";

        ret += "%e" + varNum++ + " = load i8** %e" + (varNum - 2) + "\n";
        ret += "%e" + varNum++ + " = bitcast i8* %e" + (varNum - 2) + " to void (%struct.TMain*)*\n";
        ret += "call void %e" + (varNum - 1) + "(%struct.TMain* %e" + (varNum - 9) + ")\n";
        ret += "ret void\n}\n";

        return ret;
    }

    public String getIRType(String type) {
        if (type.equals("void"))
            return "void";
        if (type.equals("Int"))
            return "i32";
        return "%struct.T" + type + "*";
    }

    public String getRTTIType(String type) {
        if (type.equals("IO"))
            return "%struct.__lcpl_rtti";
        if (type.equals("Object"))
            return "%struct.__lcpl_rtti";
        if (type.equals("String"))
            return "%struct.__lcpl_rtti";
        return "%.RT" + type;
    }

    public String getSymbolType(Symbol s) {
        return ((Type) s.getTypeData()).getName();
    }

    public String getVariableType(Variable v) {
        if (v instanceof FormalParam)
            return ((FormalParam) v).getVariableType().getName();
        if (v instanceof LocalDefinition)
            return ((LocalDefinition) v).getType();
        if (v instanceof Attribute)
            return ((Attribute) v).getTypeData().getName();
        return "";
    }

    public String getIRMetType(Method met) {
        return "M" + met.getParent().getName().length() + "_" + met.getParent().getName() + "_" + met.getName();
    }

    public ExprCode registerSymbol(Variable v) {
        String code = "";

        // Add variable to symbol table
        if (sTable.get(v) == null) {
            if (v instanceof FormalParam) {
                String pointer = "%e" + varNum++;
                code += pointer + " = alloca " + getIRType(getVariableType(v)) + "\n";
                code += "store " + getIRType(getVariableType(v)) + " %" + v.getName() + ", " + getIRType(getVariableType(v)) + "* " + pointer + "\n";
                sTable.put(v, pointer);
            }
            else if (v instanceof LocalDefinition) {
                String type = getVariableType(v);
                String pointer = "%e" + varNum++;
                code += pointer + " = alloca " + getIRType(type) + "\n";
                // Init local variable
                if (type.equals("Int"))
                    code += "store i32 0, i32* " + pointer + "\n";
                else if (type.equals("String"))
                    code += "store %struct.TString* @SEmpty, %struct.TString** " + pointer + "\n";
                else
                    code += "store " + getIRType(type) + " null, " + getIRType(type) + "* " + pointer + "\n";

                sTable.put(v, pointer);
            }
            else if (v instanceof Attribute) {
                // Search for invoked attribute
                LinkedList<Attribute> attrs = classAttr.get(lcplclass);
                int offset = 1;
                for (int i = 0; i < attrs.size(); i++) {
                    if (attrs.get(i) == v)
                        break;
                    offset++;
                }

                // Calculate attribute address
                String self = "%e" + varNum++;
                code += self + " = load " + getIRType(lcplclass.getName()) + "* " + pself + "\n";
                String pointer = "%e" + varNum++;
                code += pointer + " = getelementptr " + getIRType(lcplclass.getName()) + " " + self + ", i32 0, i32 " + offset + "\n";
                sTable.put(v, pointer);
            }
        }

        return new ExprCode(code, sTable.get(v));
    }
}
