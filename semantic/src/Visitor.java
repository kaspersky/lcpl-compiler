import java.util.LinkedList;
import ro.pub.cs.lcpl.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

/*
 * Clasa folosita pentru operatorul '.'
 * Este o clasa intermediara, ea nu apare in arborele
 * sintactic final.
 */
class DotOperation extends Expression {
    private String symbol;
    private Expression e1;
    public String getSymbol() {
        return symbol;
    }
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    public Expression getE1() {
        return e1;
    }
    public void setE1(Expression e1) {
        this.e1 = e1;
    }
    public DotOperation(int lineNumber, String symbol,
            Expression e1) {
        super(lineNumber);
        this.symbol = symbol;
        this.e1 = e1;
    }
}

/*
 * Construieste arborele sintactic. Semnaturile functiilor
 * sunt autogenerate in exBaseVisitor.java.
 */
public class Visitor extends exBaseVisitor<Object> {

    /*
     * Construieste o clasa
     */
    public Object visitClasa(exParser.ClasaContext ctx) {
        String parentName = null;

        if (ctx.symbol().size() > 1)
            parentName = ctx.symbol().get(1).getText();

        LinkedList<Feature> features = new LinkedList<Feature>();

        for (int i = 0; i < ctx.feature().size(); i++)
        {
            if (ctx.feature().get(i).metoda() != null)
            {
                Object tn = visit(ctx.feature().get(i));
                features.add((Feature) tn);
            }
            else if (ctx.feature().get(i).classattr() != null)
            {
                for (int j = 0; j < ctx.feature().get(i).classattr().atribut().size(); j++)
                {
                    int line = ctx.feature().get(i).classattr().atribut().get(j).getStart().getLine();
                    String name = ctx.feature().get(i).classattr().atribut().get(j).symbol().getText();
                    String type = ctx.feature().get(i).classattr().atribut().get(j).type().getText();
                    Expression init = null;
                    if (ctx.feature().get(i).classattr().atribut().get(j).expr() != null)
                        init = (Expression) visit(ctx.feature().get(i).classattr().atribut().get(j).expr());

                    Attribute at = null;
                    if (ctx.feature().get(i).classattr().atribut().get(j).DouBrOp() != null)
                        at = new Attribute(line, name, "Vector", init);
                    else
                        at = new Attribute(line, name, type, init);
                    features.add(at);
                }
            }
        }

        LCPLClass cl = new LCPLClass(ctx.getStart().getLine(), ctx.symbol().get(0).getText(), parentName, features);

        return cl;
    }

    /*
     * Construieste o metoda
     */
    public Object visitMetoda(exParser.MetodaContext ctx) {
        /* Cauta parametri formali */
        LinkedList<FormalParam> formalparams = new LinkedList<FormalParam>();
        for (int i = 0; i < ctx.formalparam().size(); i++)
        {
            Object tn = visit(ctx.formalparam().get(i));
            formalparams.add((FormalParam) tn);
        }

        /* Cauta tip returnat */
        String returnType = "void";
        if (ctx.type() != null)
            returnType = ctx.type().getText();

        /* Cauta body */
        Block body = (Block) visit(ctx.block());

        return new Method(ctx.getStart().getLine(), ctx.symbol().getText(), formalparams, returnType, body);
    }

    /*
     * Construieste un program
     */
    public Object visitProgram(exParser.ProgramContext ctx) {
        LinkedList<LCPLClass> classes = new LinkedList<LCPLClass>();

        /* Cauta clase */
        for (int i = 0; i < ctx.clasa().size(); i++)
        {
            Object tn = visit(ctx.clasa().get(i));
            classes.add((LCPLClass) tn);
        }

        return new Program(ctx.getStart().getLine(), classes);
    }

    /*
     * Construieste un parametru formal
     */
    public Object visitFormalparam(exParser.FormalparamContext ctx) {
        return new FormalParam(ctx.symbol().getText(), ctx.type().getText());
    }

    /*
     * Construieste un atribut local
     */
    public Object visitLocalattr(exParser.LocalattrContext ctx) {

        /* Va parcurge blocul "local ... end;" si va crea o lista de definitii
         * locale. Fiecare definitie va avea ca scope urmatoare definitie de
         * atribut din lista. Ultima definitie va avea scope null, urmand ca
         * blocul urmator sa-i fie atribuit mai tarziu, dupa caz */
        LinkedList<LocalDefinition> localDefs = new LinkedList<LocalDefinition>();

        for (int i = 0; i < ctx.atribut().size(); i++)
        {
            Expression init = null;
            if (ctx.atribut().get(i).expr() != null)
                init = (Expression) visitExpr(ctx.atribut().get(i).expr());
            LocalDefinition lDef = new LocalDefinition(ctx.atribut().get(i).getStart().getLine(), ctx.atribut().get(i).symbol().getText(), ctx.atribut().get(i).type().getText(), init, null);
            //localDefs.add(lDef);

            Object obj = visit(ctx.atribut().get(i));
            localDefs.add((LocalDefinition) obj);
        }

        if (localDefs.size() == 0)
            return null;

        /* Legarea atributelor intre ele dupa scope */
        for (int i = 0; i < localDefs.size() - 1; i++)
            localDefs.get(i).setScope(localDefs.get(i + 1));

        return localDefs.get(0);
    }

    /*
     * Crearea unei instructiuni
     */
    public Object visitInstruction(exParser.InstructionContext ctx) {
        /* block */
        if (ctx.block() != null)
        {
            Object ret = visit(ctx.localattr());
            Object ret2 = visit(ctx.block());

            if (ret == null)
                return ret2;

            LocalDefinition it = (LocalDefinition) ret;
            while (it.getScope() != null)
                it = (LocalDefinition) it.getScope();

            /* Setarea scope-ului ultimei definitii de variabila locala
             * cu blocul urmator */
            it.setScope((Expression) ret2);

            return ret;
        }

        /* Expresie */
        if (ctx.expr() != null)
            return visit(ctx.expr());

        /* While statement */
        if (ctx.whileins() != null)
            return visit(ctx.whileins());

        return null;
    }

    /*
     * Crearea unui atribut de tip definitie locala.
     * Teoretic, ar fi trebuit sa fie folosit si pentru
     * parametri formali, dar na...
     */
    public Object visitAtribut(exParser.AtributContext ctx) {
        Expression init = null;

        if (ctx.expr() != null)
            init = (Expression) visitExpr(ctx.expr());

        if (ctx.DouBrOp() != null)
        {
            return new LocalDefinition(ctx.getStart().getLine(), ctx.symbol().getText(), "Vector", init, null);
        }

        LocalDefinition lDef = new LocalDefinition(ctx.getStart().getLine(), ctx.symbol().getText(), ctx.type().getText(), init, null);
        return lDef;
    }

    /*
     * Crearea unei expresii
     */
    public Object visitExpr(exParser.ExprContext ctx) {
        /* Detectam expresia folosita conform operatorului */

        //System.out.println(ctx.getText());

        if (ctx.DotOp() != null)
        {
            Expression left = (Expression) visit(ctx.expr().get(0));
            return new DotOperation(ctx.getStart().getLine(), ctx.getText(), left);
        }

        if (ctx.NewOp() != null)
        {
            Expression newObj = new NewObject(ctx.getStart().getLine(), ctx.type().getText());
            if (ctx.ClBrOp() != null)
            {
                LinkedList<Expression> args = new LinkedList<Expression>();
                args.add((Expression) visit(ctx.expr().get(0)));
                newObj.setType("Vector");
                return new Dispatch(ctx.getStart().getLine(), newObj, "init", args);
            }
            return newObj;
        }

        if (ctx.Vectaccess != null)
        {
            Expression e1 = (Expression) visit(ctx.expr().get(0));
            Expression e2 = (Expression) visit(ctx.expr().get(1));
            LinkedList<Expression> args = new LinkedList<Expression>();
            args.add(e2);
            return new Dispatch(ctx.getStart().getLine(), e1, "get", args);
        }

        if (ctx.LogNotOp() != null)
        {
            Expression left = (Expression) visit(ctx.expr().get(0));
            return new LogicalNegation(ctx.getStart().getLine(), left);
        }

        if (ctx.OpBrOp() != null)
        {
            Expression e1 = (Expression) visit(ctx.expr().get(0));
            Expression e2 = (Expression) visit(ctx.expr().get(1));
            Expression e3 = (Expression) visit(ctx.expr().get(2));

            return new SubString(ctx.getStart().getLine(), e1, e2, e3);
        }

        if (ctx.OpParOp() != null)
        {
            return visit(ctx.expr().get(0));
        }

        if (ctx.AddOp() != null)
        {
            Expression left = (Expression) visit(ctx.expr().get(0));
            Expression right = (Expression) visit(ctx.expr().get(1));
            return new Addition(ctx.getStart().getLine(), left, right);
        }

        if (ctx.LessOp() != null)
        {
            Expression left = (Expression) visit(ctx.expr().get(0));
            Expression right = (Expression) visit(ctx.expr().get(1));
            return new LessThan(ctx.getStart().getLine(), left, right);
        }

        if (ctx.LessEqOp() != null)
        {
            Expression left = (Expression) visit(ctx.expr().get(0));
            Expression right = (Expression) visit(ctx.expr().get(1));
            return new LessThanEqual(ctx.getStart().getLine(), left, right);
        }

        if (ctx.EqOp() != null)
        {
            Expression left = (Expression) visit(ctx.expr().get(0));
            Expression right = (Expression) visit(ctx.expr().get(1));
            return new EqualComparison(ctx.getStart().getLine(), left, right);
        }

        if (ctx.MinusOp() != null)
        {
            if (ctx.expr().size() == 2)
            {
                Expression left = (Expression) visit(ctx.expr().get(0));
                Expression right = (Expression) visit(ctx.expr().get(1));
                return new Subtraction(ctx.getStart().getLine(), left, right);
            }
            else if (ctx.expr().size() == 1)
            {
                Expression left = (Expression) visit(ctx.expr().get(0));
                return new UnaryMinus(ctx.getStart().getLine(), left);
            }
            return null;
        }

        if (ctx.MultOp() != null)
        {
            Expression left = (Expression) visit(ctx.expr().get(0));
            Expression right = (Expression) visit(ctx.expr().get(1));
            return new Multiplication(ctx.expr().get(1).getStart().getLine(), left, right);
        }

        if (ctx.DivOp() != null)
        {
            Expression left = (Expression) visit(ctx.expr().get(0));
            Expression right = (Expression) visit(ctx.expr().get(1));
            return new Division(ctx.getStart().getLine(), left, right);
        }

        if (ctx.ifexpr() != null)
        {
            return visit(ctx.ifexpr());
        }

        if (ctx.cast() != null)
        {
            return visit(ctx.cast());
        }

        if (ctx.AttrOp() != null)
        {
            Expression left = (Expression) visit(ctx.expr().get(0));
            Expression right = (Expression) visit(ctx.expr().get(1));
            try
            {
                /* Tratam special operatorul '.', pentru ca clasa corespunzatoare
                 * nu trebuie sa apara in arbore. */
                return new Assignment(ctx.getStart().getLine(), ((DotOperation) left).getSymbol(), right);
            }
            catch (Exception e)
            {
                try {
                    Dispatch dleft = (Dispatch) left;
                    LinkedList<Expression> args = (LinkedList<Expression>) dleft.getArguments();
                    args.add(right);
                    return new Dispatch(ctx.getStart().getLine(), dleft.getObject(), "set", args);
                    //return new Assignment(ctx.getStart().getLine(), ((Dispatch) left).getName(), right);
                }
                catch (Exception u)
                {
                }
            }
            return new Assignment(ctx.getStart().getLine(), ((Symbol) left).getName(), right);
        }

        if (ctx.atom() != null)
        {
            return visit(ctx.atom());
        }

        if (ctx.apel() != null)
        {
            return visit(ctx.apel());
        }

        if (ctx.stdisp() != null)
        {
            return visit(ctx.stdisp());
        }


        return null;
    }

    /*
     * Construim un while statement
     */
    public Object visitWhileins(exParser.WhileinsContext ctx) {
        Expression cond = (Expression) visit(ctx.expr());

        Block body = (Block) visit(ctx.block());

        return new WhileStatement(ctx.getStart().getLine(), cond, body);
    }

    /*
     * Construim un bloc
     */
    public Object visitBlock(exParser.BlockContext ctx) {
        LinkedList<Expression> expressions = new LinkedList<Expression>();

        for (int i = 0; i < ctx.instruction().size(); i++)
        {
            Object ret = visit(ctx.instruction().get(i));
            expressions.add((Expression) ret);
        }

        /* Daca blocul e vid, il punem la linia 0 */
        int line = ctx.getStart().getLine();
        if (expressions.isEmpty())
            line = 0;
        return new Block(line, expressions);
    }

    /*
     * Expresie if
     */
    public Object visitIfexpr(exParser.IfexprContext ctx) {
        Expression cond = (Expression) visit(ctx.expr());
        Block ifBody = (Block) visit(ctx.block().get(0));
        Block elseBody = null;
        if (ctx.block().size() > 1)
            elseBody = (Block) visit(ctx.block().get(1));

        return new IfStatement(ctx.getStart().getLine(), cond, ifBody, elseBody);
    }

    /*
     * Cast
     */
    public Object visitCast(exParser.CastContext ctx) {
        Expression left = (Expression) visit(ctx.expr());
        return new Cast(ctx.getStart().getLine(), ctx.type().getText(), left);
    }

    /*
     * Construim o expresie de tip atom
     */
    public Object visitAtom(exParser.AtomContext ctx) {
        if (ctx.symbol() != null)
            return new Symbol(ctx.symbol().getStart().getLine(), ctx.symbol().getText());
        if (ctx.voidd() != null)
            return new VoidConstant(ctx.voidd().getStart().getLine());
        if (ctx.integer() != null)
            return new IntConstant(ctx.integer().getStart().getLine(), Integer.parseInt(ctx.integer().getText()));
        if (ctx.string() != null)
        {
            String ret = ctx.string().getText();

            /* Let the string magic begin */
            ret = ret.replace("\\r", "\r");
            ret = ret.replace("\\n", "\n");
            ret = ret.replace("\\t", "\t");
            ret = ret.replace("\\\"", "\"");
            ret = ret.replaceAll("\\\\(?![nrt_\"])", "");
            return new StringConstant(ctx.getStart().getLine(), ret);
        }
        return null;
    }

    /*
     * Construim un dispatch
     */
    public Object visitApel(exParser.ApelContext ctx) {
        Expression obj = null;
        int startInd = 0;
        if (ctx.obj != null)
        {
            obj = (Expression) visit(ctx.obj);
            startInd = 1;
        }

        LinkedList<Expression> args = new LinkedList<Expression>();
        for (int i = startInd; i < ctx.expr().size(); i++)
        {
            Object arg = visit(ctx.expr().get(i));
            args.add((Expression) arg);
        }

        return new Dispatch(ctx.getStart().getLine(), obj, ctx.symbol().getText(), args);
    }

    /*
     * Construim un static dispatch
     */
    public Object visitStdisp(exParser.StdispContext ctx) {
        Expression expr = (Expression) visit(ctx.expr().get(0));

        LinkedList<Expression> args = new LinkedList<Expression>();
        for (int i = 1; i < ctx.expr().size(); i++)
        {
            Object arg = visit(ctx.expr().get(i));
            args.add((Expression) arg);
        }

        return new StaticDispatch(ctx.getStart().getLine(), expr, ctx.type().getText(), ctx.symbol().getText(), args);
    }
}
