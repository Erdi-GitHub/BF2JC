# BF2JC
A simple BF to Java transpiler written in a day. Have fun!

## Compile
BF2JC's only dependency is docopt, which is automatically shaded using Maven.  
To build BF2JC yourself, all you need to do is run `mvn install` on the project's root directory, i.e. where pom.xml is loacted.  
  
`target/BF2JC-[VERSION].jar` (`target/BF2JC-1.0.0-SNAPSHOT.jar`) is where the compiled binary is located.

## Usage
Use the -h option to get the help menu. Everything else should be pretty self-explanatory.
```
Options:
   -h, --help       Shows this screen.. duh, how else did you get here?
   -i FILE          Input BrainFuck file [default: ./in.bf]
   -o FILE          Output Java file [default: ./out.java]
   -r, --replace    Replace the output file if it already exists [default: false]
   -p, --pipeable   Use input-stream as input (intended for piping) [default: false]
   --fix-open       Automatically close unclosed brackets instead of refusing to compile [default: false]
```
