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

    private boolean fixUnclosedBracket = false;

    public Transpiler(String tab, Reader input, File output) {
        this.tab = tab;
        this.input = new BufferedReader(input);
        this.output = output;
    }

    private String getTabs(int depth) {
        return String.join("", Collections.nCopies(depth, tab));
    }

    private String handle(char opcode, int length, int depth) {
        return getTabs(depth + 2) + tokenToJava(opcode, length) + System.lineSeparator();
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


        try(BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
            writer.write("import java.util.List;\n" +
                    "import java.util.ArrayList;\n" +
                    "import java.io.IOException;\n" +
                    "\n" +
                    "public class ");
            writer.write(output.getName().replace(".java", ""));
            writer.write(" {\n" +
                    getTabs(1) + "public static void main(String[] args) throws IOException {\n" +
                    getTabs(2) + "List<Byte> data = new ArrayList<>();\n" +
                    getTabs(2) + "data.add((byte) 0);\n" +
                    getTabs(2) + "int pos = 0;\n" +
                    "\n");

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
            if(opcode == ']') {
                depth--;

                if(depth < 0)
                    throw new UnsupportedOperationException("Unbalanced brackets! (Unexpected ']')");
            }
            writer.write(handle(opcode, length, depth));
            // handle EOF

            if(depth != 0) {
                if(!fixUnclosedBracket)
                    throw new UnsupportedOperationException("Unbalanced brackets! (Expected ']' to close '[', depth: " + depth + ")");

                System.err.println("WARNING: Unclosed brackets, auto-closing (depth: " + depth + ")");
                while(depth > 0) {
                    depth--;
                    writer.write(handle(']', 1, depth));
                }
            }

            writer.write(getTabs(1) + "}\n" +
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
            return "pos -= " + length + ";";
        case '>':
            return "pos += " + length + "; while(pos >= data.size()) data.add((byte) 0);";
        case '-':
            return "data.set(pos, (byte) (data.get(pos) - " + length + "));";
        case '+':
            return "data.set(pos, (byte) (data.get(pos) + " + length + "));";
        case '.':
            return "System.out.print((char)(byte) data.get(pos));";
        case ',':
            return "data.set(pos, (byte) System.in.read());";
        case '[':
            return "while(data.get(pos) != 0) {";
        case ']':
            return "}";
        }

        return null;
    }
}
