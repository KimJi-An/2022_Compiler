import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

public class TestMiniGo {
	public static void main(String[] args) throws Exception {
		MiniGoLexer lexer = new MiniGoLexer(new ANTLRFileStream("test.go"));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MiniGoParser parser = new MiniGoParser(tokens);
		ParseTree tree = parser.program();
	}
}