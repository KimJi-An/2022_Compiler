package listener;

import java.util.Hashtable;

import generated.MiniGoParser;
import generated.MiniGoParser.*;

import listener.SymbolTable;
import listener.SymbolTable.*;


public class BytecodeGenListenerHelper {
	/* <boolean functions> */
	static boolean isFunDecl(ProgramContext ctx, int i) {
		return ctx.getChild(i).getChild(0) instanceof Fun_declContext;
	}

	static boolean isArrayDecl(Var_declContext ctx) {
		return ctx.getChildCount() == 6;
	}

	static boolean isDeclWithInit(Var_declContext ctx) {
		return ctx.getChildCount() == 5 ;
	}

	/* <information extraction> */
	static String getStackSize(Fun_declContext ctx) { return "32"; }

	static String getLocalVarSize(Fun_declContext ctx) { return "32"; }

	static String getTypeText(Type_specContext typespec) {
		if(typespec.INT() != null)
			return "I";

		return "V";
	}

	// params
	static String getParamName(ParamContext param) {
		return param.IDENT().getText();
	}
	
	static String getParamTypesText(ParamsContext params) {
		String typeText = "";
		
		for(int i = 0; i < params.param().size(); i++) {
			Type_specContext typespec = (Type_specContext) params.param(i).getChild(1);
			typeText += getTypeText(typespec);
		}
		return typeText;
	}
	
	static String getLocalVarName(Local_declContext local_decl) {
		return local_decl.var_decl().IDENT().getText();
	}
	
	static String getFunName(Fun_declContext ctx) {
		return ctx.IDENT().getText();
	}
	
	static String getFunName(ExprContext ctx) {
		return ctx.IDENT().getText();
	}
	
	static boolean noElse(If_stmtContext ctx) {
		return ctx.getChildCount() < 5;
	}
	
	static String getFunProlog() {
		return ".class public " + getCurrentClassName() + "\n"
				+ ".super java/lang/Object" + "\n"
				+ ".method public <init>()V" + "\n"
				+ "\t" + "aload_0" + "\n"
				+ "\t" + "invokenonvirtual java/lang/Object/<init>()V" + "\n"
				+ "\t" + "return" + "\n"
				+ ".end method" + "\n";
	}
	
	static String getCurrentClassName() {
		return "Test";
	}
}
