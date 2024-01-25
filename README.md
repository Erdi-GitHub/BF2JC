# BF2JC
A simple BF to Java transpiler written in a day. Have fun!

## Compile
BF2JC's only dependency is docopt, which is automatically shaded using Maven.  
To build BF2JC yourself, all you need to do is run `mvn install` on the project's root directory, i.e. where pom.xml is located.  
  
`target/BF2JC-[VERSION].jar` (`target/BF2JC-1.3.0-SNAPSHOT.jar`) is where the compiled binary is located.

## Usage
Use the -h option to get the help menu. Everything else should be pretty self-explanatory.
```
Options:
   -h, --help       Guess. No really, guess. Or use it yourself.
   -i FILE          Input BrainFuck file [default: ./in.bf]
   -o FILE          Output Java file [default: ./out.java]
   -r, --replace    Replace the output file if it already exists [default: false]
   -p, --pipeable   Use input-stream as input (intended for piping) [default: false]
   -m, --minify     Minify the resulting Java code [default: false]
   -w W, --width W  Cell width, either 8 or 16. This has no impact on memory usage [default: 8]
   --length LENGTH  The length of the memory tape in bytes [default: 30000]
   --fix-open       Automatically close unclosed brackets instead of refusing to compile [default: false]
   --spaces SPACES  Use spaces instead of tabs (min: 0, max: 6) [default: -1]
```
