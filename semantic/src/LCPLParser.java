import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.yaml.snakeyaml.*;
import ro.pub.cs.lcpl.*;
import java.io.*;

public class LCPLParser {

    public static void main(String[] args) throws Exception {

        exLexer lexer = new exLexer(new ANTLRFileStream(args[0]));
        exParser parser = new exParser(new CommonTokenStream(lexer));

        /* Parsarea fisierului de intrare */
        ParseTree tree = parser.program();

        /* Construim arborele sintactic */
        Visitor eval = new Visitor();
        Object tn = eval.visit(tree);

        /* Serializarea AST-ului */
        Yaml yaml = new Yaml();
        PrintStream fos = new PrintStream(new FileOutputStream(args[1]));
        fos.println(yaml.dump((TreeNode) tn));
        fos.close();
    }
}
