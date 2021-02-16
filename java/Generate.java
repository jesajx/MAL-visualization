import java.io.File;

public class Generate {

    public static void main(String[] args) {
        try {
            String malFile = args[0];
            AST ast = Parser.parse(new File(malFile));
            Lang lang = LangConverter.convert(ast);
            // TODO add option to switch between visualizers
            Generator.generate(lang, null);
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
