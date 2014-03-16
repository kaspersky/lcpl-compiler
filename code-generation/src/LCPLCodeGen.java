import java.io.*;

import ro.pub.cs.lcpl.*;

import org.yaml.snakeyaml.*;
import org.yaml.snakeyaml.constructor.Constructor;

public class LCPLCodeGen {

    public static void main(String[] args) {
        if (args.length != 2)
        {
            System.err.println("Usage: LCPLCodeGen <filein.run> <fileout.ir>\n");
            System.exit(1);
        }
        try {
            Yaml yaml = new Yaml(new Constructor(Program.class));
            FileInputStream fis = new FileInputStream(args[0]);
            Program p = (Program) yaml.load(fis);
            fis.close();

            LCPLtoIR lcpltoir = new LCPLtoIR(p);
            lcpltoir.emit();

            PrintStream fos = new PrintStream(new FileOutputStream(args[1]));
            fos.println(lcpltoir.antet);
            fos.close();
        } catch (IOException ex) {
            System.err.println("File error: " + ex.getMessage());
            System.err.println("===================================================");
        }

    }

}
