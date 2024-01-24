package me.erdi.bf2jc.trans;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// this all feels very illegal.
public class Transpiler implements Runnable {
    private static final List<Character> OPCODES = new ArrayList<>();

    static {
        OPCODES.add('<');
        OPCODES.add('>');
        OPCODES.add('-');
        OPCODES.add('+');
        OPCODES.add(',');
        OPCODES.add('.');
        OPCODES.add('[');
        OPCODES.add(']');
    }

    private final BufferedReader input;
    private final String tab;
    private final File output;
    private final int tapeLength;

    private final boolean minify;

    private boolean fixUnclosedBracket = false;

    private final String ptrField;
    private final String lenField;
    private final String dataField;

    public Transpiler(Reader input, File output) {
        this("\t", input, output);
    }

    public Transpiler(String tab, Reader input, File output) {
        this(tab, input, output, 30000);
    }

    public Transpiler(String tab, Reader input, File output, int tapeLength) {
        this(tab, input, output, tapeLength, false);
    }

    public Transpiler(String tab, Reader input, File output, int tapeLength, boolean minify) {
        this.tab = tab;
        this.input = new BufferedReader(input);
        this.output = output;
        this.tapeLength = tapeLength;
        this.minify = minify;

        if(minify) {
            ptrField = "p";
            lenField = "l";
            dataField = "t";
        } else {
            ptrField = "ptr";
            lenField = "len";
            dataField = "tape";
        }
    }

    private String getTabs(int depth) {
        if(minify)
            return "";

        return String.join("", Collections.nCopies(depth, tab));
    }

    private String handle(char opcode, int length, int depth) {
        String expr = tokenToJava(opcode, length);
        if(expr == null)
            return "";

        // remove spaces if necessary
        // NOTE: nothing returned from #tokenToJava(int,int) is whitespace-sensitive
        if(minify)
            expr = expr.replace(" ", "");

        return getTabs(depth + 2) + expr +
                (minify ? "" : System.lineSeparator());
    }

    public void setFixUnclosedBracket(boolean fixUnclosedBracket) {
        this.fixUnclosedBracket = fixUnclosedBracket;
    }

    @Override
    public void run() {
        char opcode = '\0';
        int length = 0;
        int depth = 0;

        int n;

        String newLine = minify ? "" : "\n";
        String space = minify ? "" : " ";

        String len = Integer.toString(tapeLength, 10);

        if(minify) {
            // use hex number whenever it's shorter than the decimal equivalent

            String hexLen = "0x" + Integer.toString(tapeLength, 16);
            if(hexLen.length() < len.length())
                len = hexLen;
        }

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
            if(!minify)
                writer.write("import java.io.IOException;\n\n");
            writer.write("public class ");
            writer.write(output.getName().replace(".java", ""));


            writer.write(("_{\n" +
                    getTabs(1) + "public static void main(String[]_" + (minify ? "$" : "args") + ")_throws " + (minify ? "java.io." : "") + "IOException_{\n" +
                    getTabs(2) + "int " + lenField + "_=_" + len + ";\n" +
                    getTabs(2) + "byte[]_" + dataField + "_=_new byte[" + lenField + "];\n" +
                    getTabs(2) + "int " + ptrField + "_=_0;\n" +
                    "\n").replace("\n", newLine).replace("_", space));

            while((n = input.read()) != -1) {
                if(!OPCODES.contains((char) n))
                    continue;

                if(lengthable(opcode)) {
                    if(opcode == n) {
                        length++;
                        continue;
                    }

                    writer.write(handle(opcode, length, depth));
                }


                opcode = (char) n;
                length = 1;

                if(lengthable(opcode))
                    continue;

                if(opcode == ']') {
                    depth--;

                    if(depth < 0)
                        throw new UnsupportedOperationException("Unbalanced brackets! (Unexpected ']')");
                }

                writer.write(handle(opcode, length, depth));

                if(opcode == '[')
                    depth++;
            }

            // handle EOF
            if(lengthable(opcode))
                writer.write(handle(opcode, length, depth));

            if(depth != 0) {
                if(!fixUnclosedBracket)
                    throw new UnsupportedOperationException("Unbalanced brackets! (Expected ']' to close '[', depth: " + depth + ")");

                System.err.println("WARNING: Unclosed brackets, auto-closing (depth: " + depth + ")");
                while(depth > 0) {
                    depth--;
                    writer.write(handle(']', 1, depth));
                }
            }

            writer.write(getTabs(1) + "}" + newLine +
                    "}");
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean lengthable(char opcode) {
        switch(opcode) {
        case '<':
        case '>':
        case '-':
        case '+':
            return true;
        }

        return false;
    }

    private String tokenToJava(char opcode, int length) {
        switch(opcode) {
        case '<':
            return ptrField + " = Math.floorMod(" + ptrField + " - " + length + ", " + lenField + ");";
        case '>':
            return ptrField + " = (" + ptrField + " + " + length + ") % " + lenField + ";";
        case '-':
            return dataField + "[" + ptrField + "] -= " + length + ";";
        case '+':
            return dataField + "[" + ptrField + "] += " + length + ";";
        case '.':
            return "System.out.print((char)" + dataField + "[" + ptrField + "]);";
        case ',':
            return dataField + "[" + ptrField + "] = (byte) System.in.read();";
        case '[':
            return "while(" + dataField + "[" + ptrField + "] != 0) {";
        case ']':
            return "}";
        }

        return null;
    }
}
