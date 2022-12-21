package listener;
import generated.*;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
public class MiniGoPrintListener extends MiniGoBaseListener {
    ParseTreeProperty<String> newTexts = new ParseTreeProperty<>();
    @Override
    public void exitProgram(MiniGoParser.ProgramContext ctx) {
        String program = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            newTexts.put(ctx, ctx.decl(i).getText()); //ParseTree 인 newText 에 decl 을 넣음
            program += newTexts.get(ctx.getChild(i)); //ctx child 에 들어갔다가 나오면서 출력
        }
        System.out.println(program);
        File file = new File(String.format("[HW3]202002481.c"));
        // 본인 학번으로 변경해주세요.
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(program);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exitVar_decl(MiniGoParser.Var_declContext ctx) {

    }

    @Override
    public void exitType_spec(MiniGoParser.Type_specContext ctx) {
        newTexts.put(ctx, ctx.getChild(0).getText());
    }
}
