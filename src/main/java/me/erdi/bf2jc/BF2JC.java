package me.erdi.bf2jc;

import me.erdi.bf2jc.trans.Transpiler;
import org.docopt.Docopt;

import javax.lang.model.SourceVersion;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;

public class BF2JC {
    private static final String DOC =
            "BF2JC\n" +
                    "Brainfuck to Java Transpiler\n" +
                    "\n" +
                    "Usage: bf2jc [options]\n" +
                    "\n" +
                    "Options:\n" +
                    "   -h, --help       Shows this screen.. duh, how else did you get here?\n" +
                    "   -i FILE          Input BrainFuck file [default: ."  + File.separator + "in.bf]\n" +
                    "   -o FILE          Output Java file [default: ." + File.separator + "out.java]\n" +
                    "   -r, --replace    Replace the output file if it already exists [default: false]\n" +
                    "   -p, --pipeable   Use input-stream as input (intended for piping) [default: false]\n" +
                    "   -m, --minify     Minify the resulting Java code [default: false]\n" +
                    "   --length LENGTH  The length of the memory tape in bytes [default: 30000]\n" +
                    "   --fix-open       Automatically close unclosed brackets instead of refusing to compile [default: false]\n" +
                    "   --spaces SPACES  Use spaces instead of tabs (min: 0, max: 6) [default: -1]";

    public static void main(String[] args) throws IOException {
        Map<String, Object> opts = new Docopt(DOC).parse(args);

        String tab = "\t";

        int spaces = Integer.parseInt((String)opts.get("--spaces"));
        if(spaces > -1)
            tab = String.join("", Collections.nCopies(Math.min(spaces, 6), " "));

        File output = new File((String) opts.get("-o"));
        String className = output.getName().replace(".java", "");
        if(!SourceVersion.isIdentifier(className) || SourceVersion.isKeyword(className)) {
            System.err.println("The output file's name must be a valid Java identifier!");
            System.exit(1);
        }

        boolean replace = (boolean)opts.get("--replace");
        if(!replace && output.exists()) {
            System.err.println("Output file already exists. To replace it, use --replace");
            System.exit(1);
        }

        boolean pipeable = (boolean)opts.get("--pipeable");
        boolean minify = (boolean)opts.get("--minify");

        int length = Integer.parseInt((String)opts.get("--length"));
        Transpiler transpiler = new Transpiler(tab, pipeable ? new InputStreamReader(System.in) : new FileReader((String) opts.get("-i")), output, length, minify);
        transpiler.setFixUnclosedBracket((boolean)opts.get("--fix-open"));

        long start = System.nanoTime();
        transpiler.run();
        long end = System.nanoTime();

        long delta = end - start;
        System.out.println("Transpiled in " + (((float)delta) / 1000000f) + "ms!");
    }
}
