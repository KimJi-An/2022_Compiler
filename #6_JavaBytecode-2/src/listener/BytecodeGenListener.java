package listener;

import java.util.Hashtable;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

import generated.MiniGoBaseListener;
import generated.MiniGoParser.*;

import static listener.BytecodeGenListenerHelper.*;
import static listener.SymbolTable.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class BytecodeGenListener extends MiniGoBaseListener implements ParseTreeListener {
    ParseTreeProperty<String> newTexts = new ParseTreeProperty<String>();
    SymbolTable symbolTable = new SymbolTable();

    int tab = 0;
    int label = 0;

    public String addTab(int num) {
        String result = "";

        for(int i = 0; i < num; i++)
            result += "\t";

        return result;
    }

    /* program : decl+ */
    @Override
    public void exitProgram(ProgramContext ctx) {
        String classProlog = getFunProlog();

        String fun_decl = "", var_decl = "";

        for(int i = 0; i < ctx.getChildCount(); i++) {
            if(isFunDecl(ctx, i))
                fun_decl += newTexts.get(ctx.decl(i));
            else
                var_decl += newTexts.get(ctx.decl(i));
        }

        newTexts.put(ctx, classProlog + var_decl + fun_decl);

//        System.out.println(newTexts.get(ctx));

        // make file
        File file = new File(String.format("test.j"));

        try {
            FileWriter fw = new FileWriter(file);
            fw.write(newTexts.get(ctx));
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* decl	: var_decl | fun_decl */
    @Override
    public void exitDecl(DeclContext ctx) {
        String decl = "";

        if(ctx.getChildCount() == 1) {
            // var_decl
            if (ctx.var_decl() != null)
                decl += newTexts.get(ctx.var_decl());
            // fun_decl
            else
                decl += newTexts.get(ctx.fun_decl());
        }
        newTexts.put(ctx, decl);
    }

    /* local_decl : var_decl */
    @Override
    public void enterLocal_decl(Local_declContext ctx) {
        String varName = getLocalVarName(ctx);
        Type varType;

        // var_decl : dec_spec IDENT '[' LITERAL ']' type_spec
        if(isArrayDecl(ctx.var_decl())) {
            varType = Type.INTARRAY;

            symbolTable.putLocalVar(varName, varType);
        }
        // var_decl : dec_spec IDENT type_spec '=' LITERAL
        else if(isDeclWithInit(ctx.var_decl())) {
            varType = Type.INT;
            int initVal = Integer.valueOf(ctx.var_decl().LITERAL().getText());

            symbolTable.putLocalVarWithInitVal(varName, varType, initVal);
        }
        // var_decl : dec_spec IDENT type_spec
        else {
            varType = Type.INT;

            symbolTable.putLocalVar(varName, varType);
        }
    }

    @Override
    public void exitLocal_decl(Local_declContext ctx) {
        String varDecl = "";

        if(isDeclWithInit(ctx.var_decl())) {
            String varId = symbolTable.getVarId(ctx);
            String var = ctx.var_decl().LITERAL().getText();

            varDecl += addTab(tab) + "ldc " + var + "\n"
                        + addTab(tab) + "istore " + varId + "\n";
        }
        newTexts.put(ctx, varDecl);
    }

    /* var_decl	: dec_spec IDENT type_spec
     *		    | dec_spec IDENT type_spec '=' LITERAL
     *		    | dec_spec IDENT '[' LITERAL ']' type_spec
     */
    @Override
    public void enterVar_decl(Var_declContext ctx) {
        String varName = ctx.IDENT().getText();

        // dec_spec IDENT '[' LITERAL ']' type_spec
        if (isArrayDecl(ctx)) {
            symbolTable.putGlobalVar(varName, Type.INTARRAY);
        }
        // dec_spec IDENT type_spec '=' LITERAL
        else if (isDeclWithInit(ctx)) {
            int initVal = Integer.valueOf(ctx.LITERAL().getText());
            symbolTable.putGlobalVarWithInitVal(varName, Type.INT, initVal);
        }
        // dec_spec IDENT type_spec
        else {
            symbolTable.putGlobalVar(varName, Type.INT);
        }
    }

    @Override
    public void exitVar_decl(Var_declContext ctx) {
        String varName = ctx.IDENT().getText();
        String varDecl = "";

        if (isDeclWithInit(ctx)) {
            varDecl += "putfield " + varName + "\n";
            // v. initialization => Later! skip now..:
        }
        newTexts.put(ctx, varDecl);
    }

    /* fun_decl : FUNC IDENT '(' params ')' type_spec compound_stmt */
    @Override
    public void enterFun_decl(Fun_declContext ctx) {
        tab++;
        symbolTable.initFunDecl();

        String fname = getFunName(ctx);
        ParamsContext params;

        if (fname.equals("main")) {
            symbolTable.putLocalVar("args", Type.INTARRAY);
        }
        else {
            symbolTable.putFunSpecStr(ctx);
            params = (ParamsContext) ctx.getChild(3);
            symbolTable.putParams(params);
        }
    }

    @Override
    public void exitFun_decl(Fun_declContext ctx) {
        String fname = getFunName(ctx);             // function name
        String fheader = funcHeader(ctx, fname);    // header

        // header + compound_stmt
        String stmt = fheader + newTexts.get(ctx.compound_stmt());

        // function type : void
        if(getTypeText(ctx.type_spec()).equals("V")) {
            stmt += addTab(tab) + "return" + "\n";
        }
        tab--;
        stmt += ".end method" + "\n";

        newTexts.put(ctx, stmt);
    }

    private String funcHeader(Fun_declContext ctx, String fname) {
        return "\n.method public static " + symbolTable.getFunSpecStr(fname) + "\n"
                + "\t.limit stack " + getStackSize(ctx) + "\n"
                + "\t.limit locals " + getLocalVarSize(ctx) + "\n";
    }

    /* stmt	: expr_stmt | compound_stmt | if_stmt | for_stmt | return_stmt */
    @Override
    public void exitStmt(StmtContext ctx) {
        String stmt = "";

        if(ctx.getChildCount() > 0) {
            // expr_stmt
            if(ctx.expr_stmt() != null)
                stmt += newTexts.get(ctx.expr_stmt());
            // compound_stmt
            else if(ctx.compound_stmt() != null)
                stmt += newTexts.get(ctx.compound_stmt());
            // if_stmt
            else if(ctx.if_stmt() != null)
                stmt += newTexts.get(ctx.if_stmt());
            // for_stmt
            else if(ctx.for_stmt() != null)
                stmt += newTexts.get(ctx.for_stmt());
            // return_stmt
            else if(ctx.return_stmt() != null)
                stmt += newTexts.get(ctx.return_stmt());
        }
        newTexts.put(ctx, stmt);
    }

    /* expr_stmt : expr  */
    @Override
    public void exitExpr_stmt(Expr_stmtContext ctx) {
        String stmt = newTexts.get(ctx.expr());  // expr

        newTexts.put(ctx, stmt);
    }

    /* for_stmt : FOR expr stmt */
    @Override
    public void exitFor_stmt(For_stmtContext ctx) {
        String lstart = symbolTable.newLabel();
        String lend = symbolTable.newLabel();

        String stmt = "\n" + lstart + ":\n"
                    + newTexts.get(ctx.expr())
                    + addTab(tab) + "ifeq " + lend + "\n"
                    + newTexts.get(ctx.stmt())
                    + addTab(tab) + "goto " + lstart + "\n"
                    + "\n" + lend + ": \n";

        newTexts.put(ctx, stmt);
    }

    /* compound_stmt : '{' local_decl* stmt* '}' */
    @Override
    public void exitCompound_stmt(Compound_stmtContext ctx) {
        String stmt = "";

        // local_decl*
        for(int i = 0; i < ctx.local_decl().size(); i++)
            stmt += newTexts.get(ctx.local_decl(i));

        // stmt*
        for(int i = 0; i < ctx.stmt().size(); i++)
            stmt += newTexts.get(ctx.stmt(i));

        newTexts.put(ctx, stmt);
    }

    /* if_stmt : IF  expr  stmt
     *		   | IF  expr  stmt ELSE stmt
     */
    @Override
    public void exitIf_stmt(If_stmtContext ctx) {
        String condExpr= newTexts.get(ctx.expr());          // condition
        String thenStmt = newTexts.get(ctx.stmt(0));     // then

        String lthen = symbolTable.newLabel();
        String lelse = symbolTable.newLabel();

        String stmt = condExpr + addTab(tab) + "ifeq ";

        // IF expr stmt
        if(noElse(ctx)) {
            stmt += lthen + "\n" + thenStmt
                    + "\n" + lthen + ":\n";
        }
        // IF expr stmt ELSE stmt
        else {
            String elseStmt = newTexts.get(ctx.stmt(1));

            stmt += lelse + "\n" + thenStmt;

            if(!thenStmt.contains("ireturn")) {
                stmt += addTab(tab) + "goto " + lthen + "\n";
            }

            stmt += "\n" + lelse + ":\n"
                    + elseStmt
                    + "\n" + lthen + ":\n";
        }
        newTexts.put(ctx, stmt);
    }

    /* return_stmt : RETURN
     *		       | RETURN expr
     *		       | RETURN expr ',' expr
     */
    @Override
    public void exitReturn_stmt(Return_stmtContext ctx) {
        // exclude "RETURN expr ',' expr"

        String stmt = "";

        // RETURN expr
        if(ctx.getChildCount() == 2) {
            stmt += newTexts.get(ctx.expr(0))
                    + addTab(tab) + "ireturn" + "\n";
        }
        // RETURN
        else
            stmt += addTab(tab) + "return" + "\n";

        newTexts.put(ctx, stmt);
    }

    // warning! Too many holes. You should check the rules rather than use them as is.
    @Override
    public void exitExpr(ExprContext ctx) {
        String expr = "";

        if(ctx.getChildCount() <= 0) {
            newTexts.put(ctx, "");
            return;
        }

        // IDENT | LITERAL
        if(ctx.getChildCount() == 1) {
            // IDENT
            if(ctx.IDENT() != null) {
                String idName = ctx.IDENT().getText();

                if(symbolTable.getVarType(idName) == Type.INT) {
                    expr += addTab(tab) + "iload " + symbolTable.getVarId(idName) + " \n";
                }
            }
            // LITERAL
            else if (ctx.LITERAL() != null) {
                String literalStr = ctx.LITERAL().getText();
                expr += addTab(tab) + "ldc " + literalStr + " \n";
            }
        }
        // op=('-'|'+'|'--'|'++'|'!') expr
        else if(ctx.getChildCount() == 2) {
            expr = handleUnaryExpr(ctx, newTexts.get(ctx.expr(0)));
        }
        else if(ctx.getChildCount() == 3) {
            // '(' expr ')'
            if(ctx.getChild(0).getText().equals("(")) {
                expr += newTexts.get(ctx.expr(0));
            }
            // IDENT '=' expr
            else if(ctx.getChild(1).getText().equals("=")) {
                expr += newTexts.get(ctx.expr(0)) + addTab(tab) + "istore_"
                        + symbolTable.getVarId(ctx.IDENT().getText()) + " \n";
            }
            // left=expr op=('*'|'/'|'%'|'+'|'-') right=expr
            // left=expr op=(EQ|NE|LE|'<'|GE|'>'|AND|OR) right=expr
            else {
                expr = handleBinExpr(ctx, expr);
            }
        }
        // IDENT '(' args ')' | IDENT '[' expr ']'
        else if(ctx.getChildCount() == 4) {
            // IDENT '(' args ')'
            if(ctx.args() != null) {
                expr = handleFunCall(ctx, expr);
            }
            // IDENT '[' expr ']'
            else {
            }
        }
        // IDENT '[' expr ']' '=' expr
        else {
        }
        newTexts.put(ctx, expr);
    }

    private String handleUnaryExpr(ExprContext ctx, String expr) {
        String l1 = symbolTable.newLabel();
        String l2 = symbolTable.newLabel();
        String lend = symbolTable.newLabel();

        String varId = symbolTable.getVarId(ctx.getChild(1).getText());

        expr += addTab(tab);

        switch(ctx.getChild(0).getText()) {
            case "-":
                expr += "ineg" + "\n";
                break;

            case "--":
                expr += "ldc 1" + "\n"
                        + addTab(tab) + "isub" + "\n"
                        + addTab(tab) + "istore " + varId + "\n";
                break;

            case "++":
                expr += "ldc 1" + "\n"
                        + addTab(tab) + "iadd" + "\n"
                        + addTab(tab) + "istore " + varId + "\n";
                break;

            case "!":
                expr += "ifeq " + l2 + "\n"
                        + "\n" + l1 + ":\n"
                        + addTab(tab) + "ldc 0" + "\n"
                        + addTab(tab) + "goto " + lend + "\n"
                        + "\n" + l2 + ":\n"
                        + addTab(tab) + "ldc 1" + "\n"
                        + "\n" + lend + ":\n";
                break;
        }
        return expr;
    }

    private String handleBinExpr(ExprContext ctx, String expr) {
        String l2 = symbolTable.newLabel();
        String lend = symbolTable.newLabel();

        expr += newTexts.get(ctx.expr(0));
        expr += newTexts.get(ctx.expr(1));
        expr += addTab(tab);

        switch (ctx.getChild(1).getText()) {
            case "*":
                expr += "imul \n";
                break;

            case "/":
                expr += "idiv \n";
                break;

            case "%":
                expr += "irem \n";
                break;

            case "+":
                expr += "iadd \n";
                break;

            case "-":
                expr += "isub \n";
                break;

            case "==":
                expr += "isub " + "\n"
                        + addTab(tab) + "ifeq " + l2 + "\n"
                        + addTab(tab) + "ldc 0" + "\n"
                        + addTab(tab) + "goto " + lend + "\n"
                        + "\n" + l2 + ":\n"
                        + addTab(tab) + "ldc 1" + "\n"
                        + "\n" + lend + ":\n";
                break;

            case "!=":
                expr += "isub " + "\n"
                        + addTab(tab) + "ifne " + l2 + "\n"
                        + addTab(tab) + "ldc 0" + "\n"
                        + addTab(tab) + "goto " + lend + "\n"
                        + "\n" + l2 + ":\n"
                        + addTab(tab) + "ldc 1" + "\n"
                        + "\n" + lend + ":\n";
                break;

            case "<=":
                expr += "isub " + "\n"
                        + addTab(tab) + "ifle " + l2 + "\n"
                        + addTab(tab) + "ldc 0" + "\n"
                        + addTab(tab) + "goto " + lend + "\n"
                        + "\n" + l2 + ":\n"
                        + addTab(tab) + "ldc 1" + "\n"
                        + "\n" + lend + ":\n";
                break;

            case "<":
                expr += "isub " + "\n"
                        + addTab(tab) + "iflt " + l2 + "\n"
                        + addTab(tab) + "ldc 0" + "\n"
                        + addTab(tab) + "goto " + lend + "\n"
                        + "\n" + l2 + ":\n"
                        + addTab(tab) + "ldc 1" + "\n"
                        + "\n" + lend + ":\n";
                break;

            case ">=":
                expr += "isub " + "\n"
                        + addTab(tab) + "ifge " + l2 + "\n"
                        + addTab(tab) + "ldc 0" + "\n"
                        + addTab(tab) + "goto " + lend + "\n"
                        + "\n" + l2 + ":\n"
                        + addTab(tab) + "ldc 1" + "\n"
                        + "\n" + lend + ":\n";
                break;

            case ">":
                expr += "isub " + "\n"
                        + addTab(tab) + "ifgt " + l2 + "\n"
                        + addTab(tab) + "ldc 0" + "\n"
                        + addTab(tab) + "goto " + lend + "\n"
                        + "\n" + l2 + ":\n"
                        + addTab(tab) + "ldc 1" + "\n"
                        + "\n" + lend + ":\n";
                break;

            case "and":
                expr += "ifne "+ lend + "\n"
                        + addTab(tab) + "pop" + "\n"
                        + addTab(tab) + "ldc 0" + "\n\n"
                        + lend + ":\n";
                break;

            case "or":
                expr += "ifeq "+ lend + "\n"
                        + addTab(tab) + "pop" + "\n"
                        + addTab(tab) + "ldc 1" + "\n"
                        + "\n" + lend + ":\n";
                break;
        }
        return expr;
    }

    private String handleFunCall(ExprContext ctx, String expr) {
        String fname = getFunName(ctx);

        // call _print()
        if (fname.equals("_print")) {
            expr += addTab(tab) + "getstatic java/lang/System/out Ljava/io/PrintStream;" + "\n"
                    + newTexts.get(ctx.args()) + addTab(tab) + "invokevirtual "
                    + symbolTable.getFunSpecStr("_print") + "\n";
        }
        else {
            expr += newTexts.get(ctx.args())
                    + addTab(tab) + "invokestatic " + getCurrentClassName()+ "/"
                    + symbolTable.getFunSpecStr(fname) + "\n";
        }
        return expr;
    }

    /* args	: expr (',' expr)* | */
    @Override
    public void exitArgs(ArgsContext ctx) {
        String argsStr = "";

        for (int i = 0; i < ctx.expr().size() ; i++) {
            argsStr += newTexts.get(ctx.expr(i)) ;
        }
        newTexts.put(ctx, argsStr);
    }
}
