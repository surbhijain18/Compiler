package cop5556sp18;

import cop5556sp18.Scanner.Kind;
import cop5556sp18.Scanner.Token;
import cop5556sp18.Types.Type;
import cop5556sp18.AST.ASTNode;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
import cop5556sp18.AST.Expression;
import cop5556sp18.AST.ExpressionBinary;
import cop5556sp18.AST.ExpressionBooleanLiteral;
import cop5556sp18.AST.ExpressionConditional;
import cop5556sp18.AST.ExpressionFloatLiteral;
import cop5556sp18.AST.ExpressionFunctionAppWithExpressionArg;
import cop5556sp18.AST.ExpressionFunctionAppWithPixel;
import cop5556sp18.AST.ExpressionIdent;
import cop5556sp18.AST.ExpressionIntegerLiteral;
import cop5556sp18.AST.ExpressionPixel;
import cop5556sp18.AST.ExpressionPixelConstructor;
import cop5556sp18.AST.ExpressionPredefinedName;
import cop5556sp18.AST.ExpressionUnary;
import cop5556sp18.AST.LHS;
import cop5556sp18.AST.LHSIdent;
import cop5556sp18.AST.LHSPixel;
import cop5556sp18.AST.LHSSample;
import cop5556sp18.AST.PixelSelector;
import cop5556sp18.AST.Program;
import cop5556sp18.AST.StatementAssign;
import cop5556sp18.AST.StatementIf;
import cop5556sp18.AST.StatementInput;
import cop5556sp18.AST.StatementShow;
import cop5556sp18.AST.StatementSleep;
import cop5556sp18.AST.StatementWhile;
import cop5556sp18.AST.StatementWrite;

public class TypeChecker implements ASTVisitor {


	TypeChecker() {
	}

	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}
	SymbolTable symbolTable = new SymbolTable();

	// Name is only used for naming the output file.
	// Visit the child block to type check program.
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		program.block.visit(this, arg);
		return null;
	}

	
	/*
	Block ::= enterScope ( Declaration | Statement )* leaveScope
	*/
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		symbolTable.enterScope();
		for (ASTNode node: block.decsOrStatements)
		{	node.visit(this, arg);
		}
		symbolTable.exitScope();

		
		return null;
	}

	
	/*
	 * Declaration.name ← IDENTIFIER.name
	 * Declaration.name SymbolTable.∈/ currentScope
	 * 	Expression 0 == ε or (Expression 0 .type == integer and type == image)
	 * Expression 1 == ε or Expression 1 .type == integer and type == image)
	 * (Expression 0 == ε) == (Expression 1 == ε)
	 * SymbolTable ← SymbolTable ∪ (name, Declaration)
	*/
	@Override
	public Object visitDeclaration(Declaration declaration, Object arg) throws Exception {
		Expression width = declaration.width;
		Expression height = declaration.height;
		if (width != null)
			width.visit(this, null);
		if (height != null)
			height.visit(this, null);
		
		if (!symbolTable.duplicateCheck(declaration)) 
		{
			/*
			if (width == null && height == null) {
				symbolTable.add(declaration.name, new Symbol(declaration));
			} else if (width == null || (width.type == Type.INTEGER && Types.getType(declaration.type) == Type.IMAGE)){
				symbolTable.add(declaration.name, new Symbol(declaration));
			} else if (height == null || (height.type == Type.INTEGER && Types.getType(declaration.type) == Type.IMAGE)) {
				symbolTable.add(declaration.name, new Symbol(declaration));
			} 
			*/
			
			if (Types.getType(declaration.type) == Type.IMAGE)
			{
				if((width == null && height == null) || (width.type == Type.INTEGER && height.type == Type.INTEGER))
				{
					symbolTable.add(declaration.name, new Symbol(declaration));
				}
				else
					throw new SemanticException(declaration.firstToken, "Type Mismatch of width and height");
			}
			else
				symbolTable.add(declaration.name, new Symbol(declaration));
		}	
		else {
			//symbolTable.add(declaration.name, new Symbol(declaration));
			throw new SemanticException(declaration.firstToken, "Variable reinitialized");
			}
		return declaration.type;
	}

	/*
	 * StatementInput.destName ← IDENTIFIER.name
	 * StatementInput.dec ←SymbolTable.lookup(StatementInput.destName)
	 * StatementInput.dec != null
	 * Expression.type ==integer
	 */
	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws Exception {
		statementInput.dec = symbolTable.lookup(statementInput.destName);
		
		if (statementInput.e != null)
			statementInput.e.visit(this, null);
		
		if (statementInput.dec != null) {
			if (statementInput.e.type == Type.INTEGER)
			{
				return null;
			}
			else
				throw new SemanticException(statementInput.firstToken, "Type mismatch in sourcename and destination name");
		}
		else
			throw new SemanticException(statementInput.firstToken, "No entry found in Symbol table");
	}

	
	/*
	 * StatementWrite.sourceName ← IDENTIFIER 0 .name
	 * StatementWrite.sourceDec ←symbolTable.lookup(StatementWrite.sourceName)
	 * StatementWrite.sourceDec != null
	 * StatementWrite.destName ← IDENTIFIER 1 .name
	 * StatementWrite.destDec ← symbolTable.lookup(StatementWrite.destName)
	 * StatementWrite.destDec != null
	 * sourceDec.type == image
	 * destDec.type == filename
	*/
	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg) throws Exception {
		statementWrite.sourceDec = symbolTable.lookup(statementWrite.sourceName);//.getDecl();

		if (statementWrite.sourceDec!= null) 
		{
			statementWrite.destDec = symbolTable.lookup(statementWrite.destName);//.getDecl();
			if (statementWrite.destDec != null) {
				if (Types.getType(statementWrite.sourceDec.type) == Type.IMAGE && 	Types.getType(statementWrite.destDec.type) == Type.FILE)
					return null;
				else
					throw new SemanticException(statementWrite.firstToken, "Type mismatch in sourcename and destination name");

			}
			else
				throw new SemanticException(statementWrite.firstToken, "No entry found in Symbol table for destination");
		}

		else
			throw new SemanticException(statementWrite.firstToken, "No entry found in Symbol table for source");

	}

	/*
	LHS.type == Expression.type
	*/
	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
		Expression ex = statementAssign.e;
		LHS lhs = statementAssign.lhs;
		if (ex !=null)
			ex.visit(this, null);
		if (lhs != null)
			lhs.visit(this, null);
	//	System.out.print("here:"+lhs.type +"\n"+statementAssign.e.type);	

		if (lhs.type == statementAssign.e.type)
			return null;
		else
			throw new SemanticException(statementAssign.firstToken, "statementAssign has incorrect lhs type");
	}

	
	/*
	Expression.type == boolean
	*/
	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws Exception {

		Expression exGuard = statementIf.guard;
		if (exGuard!=null)
			exGuard.visit(this, null);
		
		if (statementIf.b!=null)
			statementIf.b.visit(this, null);

		if(exGuard.type != Type.BOOLEAN)
			throw new SemanticException(statementIf.firstToken, "Illegal Type in if statement");
		return null;
	}

	
	/*
	Expression.type == boolean
	*/
	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws Exception {

		Expression exGuard = statementWhile.guard;
	//	System.out.println(exGuard.type);
		if (exGuard!=null)
			exGuard.visit(this, null);
		
		if (statementWhile.b!=null)
			statementWhile.b.visit(this, null);

		if(exGuard.type != Type.BOOLEAN)
			throw new SemanticException(statementWhile.firstToken, "Illegal Type in while statement");
		return null;
	}


	/*
	Expression.type ∈ {int, boolean, float, image}
	*/
	
	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg) throws Exception {
		Expression ex = statementShow.e;
	//	System.out.println(statementShow.e.type);
		if (ex!=null)
			ex.visit(this, null);
		if((ex.type == Type.INTEGER || ex.type == Type.BOOLEAN || ex.type == Type.FLOAT || ex.type == Type.IMAGE ))
			return null;
		throw new SemanticException(statementShow.firstToken, "Illegal Type in Show statement");

	}


	/*
	Expression.type == integer
	*/
	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg) throws Exception {
		Expression duration = statementSleep.duration;
		if (duration!=null)
			duration.visit(this, null);
//	/	System.out.println(duration.type);

		if(duration.type != Type.INTEGER)
			throw new SemanticException(statementSleep.firstToken, "Illegal Type in Sleep statement");
		return null;

	}


	/*
	 * LHSIdent.name ← IDENTIFIER.name
	 * LHSIdent.dec ←SymbolTable.lookup(LHSIdent.name)
	 * LHSIdent.dec != null
	 *LHSIdent.type ← LHSIdent.dec.type
	*/
	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg) throws Exception {
		String name = lhsIdent.name;
		lhsIdent.dec = symbolTable.lookup(name);
		//System.out.println(lhsIdent.dec.name);

		if (lhsIdent.dec != null) {
			lhsIdent.type = Types.getType(lhsIdent.dec.type);
		} else {
			throw new SemanticException(lhsIdent.firstToken, "No entry found in Symbol table");	
		}
		return lhsIdent.type;
	}

	/*
	 * LHSPixel.name ← IDENTIFIER.name
	 * LHSPixel.dec ← SymbolTable.lookup(LHSPixel.name)
	 * LHSPixel.dec != null
	 * LHSPixel.dec.type == image
	 * LHSPixel.type ← integer
	*/
	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg) throws Exception
	{
		lhsPixel.dec = symbolTable.lookup(lhsPixel.name);
		PixelSelector pixel = lhsPixel.pixelSelector;
		if(pixel != null)
			pixel.visit(this, null);
		
		if (lhsPixel.dec == null) 
		{
			throw new SemanticException(lhsPixel.firstToken, "No entry found in Symbol table");
		}
		if (Types.getType(lhsPixel.dec.type) == Type.IMAGE) {
			lhsPixel.type = Type.INTEGER;
		} 
		else 
		{
			throw new SemanticException(lhsPixel.firstToken,"Type Mismatch occuredin LHS pixel");
		}
		return lhsPixel.type;
	}

	/*
	 * LHSSample.name ← IDENTIFIER.name
	 * LHSSample.dec ← SymbolTable.lookup(LHSSample.name)
	 * LHSSample.dec != null
	 * LHSSample.dec.type == image
	 * LHSSample.type ← integer
	 */
	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg) throws Exception {
		
		lhsSample.dec = symbolTable.lookup(lhsSample.name);
		PixelSelector pixel = lhsSample.pixelSelector;
		if(pixel != null)
			pixel.visit(this, null);

		if(lhsSample.dec != null)
		{
			if (Types.getType(lhsSample.dec.type) == Type.IMAGE)
				lhsSample.type=Type.INTEGER;
			else
				throw new SemanticException(lhsSample.firstToken, "Type mismatch in LHSSample");
		}
		else
			throw new SemanticException(lhsSample.firstToken, "No entry found in Symbol table");

		return lhsSample.type;

	}

	/*
	 * Expression 0 .type == Expression 1 .type
	 *Expression 0.type == integer or Expression 0.type == float
	 */
	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		Expression e1 = pixelSelector.ex;
		Expression e2 = pixelSelector.ey;
		if(e1!=null)
			e1.visit(this, null);
		if(e2!=null)
			e2.visit(this, null);
		
		if (e1.type == e2.type)// && (e1.type == Type.INTEGER || e1.type == Type.FLOAT))
		{
			if(e1.type == Type.INTEGER || e1.type == Type.FLOAT)
				return null;
			else
				throw new SemanticException(pixelSelector.firstToken,"Type not found");
		}
		else
			throw new SemanticException(pixelSelector.firstToken,"Type Mismatch");
		}
	
	/*
	 * Expression 0 .type == boolean
	 *Expression 1 .type == Expression 2 .type
	 *ExpressionConditional.type == Expression 1 .type
	*/
	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		Expression ex1_true = expressionConditional.trueExpression;
		Expression ex2_false = expressionConditional.falseExpression;
		Expression exGuard = expressionConditional.guard;

		if (ex1_true!=null)
			ex1_true.visit(this,null);
		if (ex2_false != null)
			ex2_false.visit(this, null);
		if(exGuard != null)
			exGuard.visit(this,null);

		if(!(exGuard.type == Type.BOOLEAN && ex1_true.type == ex2_false.type))
			throw new SemanticException(expressionConditional.firstToken,"Type Mismatch occured");
		expressionConditional.type=ex1_true.type;

		return expressionConditional.type;
	}

	/*
	 * ExpressionBinary.type ← inferredType(Expression0.type,Expression1.type, op)
	*/
	
	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		Expression left=expressionBinary.leftExpression;
		Expression right=expressionBinary.rightExpression;
		
		if(left != null)
			left.visit(this, null);
		if(right != null)
			right.visit(this,null);
			
		if(expressionBinary.op==Kind.OP_AND || expressionBinary.op==Kind.OP_OR)
		{
			if(left.type == Type.INTEGER && right.type==Type.INTEGER)
				expressionBinary.type = left.type;
			else if (left.type == Type.BOOLEAN && right.type==Type.BOOLEAN)
				expressionBinary.type = left.type;
		}
		
		else if (expressionBinary.op==Kind.OP_NEQ || expressionBinary.op==Kind.OP_EQ || expressionBinary.op==Kind.OP_GT ||expressionBinary.op==Kind.OP_GE || expressionBinary.op==Kind.OP_LT || expressionBinary.op==Kind.OP_LE)
		{
			if (left.type == right.type){
				if (left.type == Type.INTEGER || left.type == Type.FLOAT || left.type == Type.BOOLEAN)
				{
					expressionBinary.type = Type.BOOLEAN;
				}
			}
		}
		
		else if (expressionBinary.op==Kind.OP_MOD)
		{
			if (left.type == Type.INTEGER && right.type == Type.INTEGER)
				expressionBinary.type = Type.INTEGER;
		}
		
		else if (expressionBinary.op==Kind.OP_PLUS || expressionBinary.op==Kind.OP_MINUS||expressionBinary.op==Kind.OP_DIV||expressionBinary.op==Kind.OP_POWER|| expressionBinary.op==Kind.OP_TIMES)
		{
			if (left.type == Type.INTEGER){
				if (right.type == Type.INTEGER)
					expressionBinary.type = Type.INTEGER;
				
				else if (right.type == Type.FLOAT)
					expressionBinary.type = Type.FLOAT;
			}
			else if (left.type == Type.FLOAT)
			{
				if (right.type == Type.FLOAT  || right.type == Type.INTEGER)
					expressionBinary.type = Type.FLOAT;
			}
		}
		else 
			expressionBinary.type = null;
		if(expressionBinary.type == null)
			throw new SemanticException(expressionBinary.firstToken, "Type mismatched error");
		//System.out.println(expressionBinary.type);
		return expressionBinary.type;
	}

	
	

	/*
	 * ExpressionUnary.type ← Expression.type
	*/
	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		Expression ex = expressionUnary.expression;
		if(ex != null)
			ex.visit(this, null);
		expressionUnary.type = ex.type;
		return expressionUnary.type;

	}
	
	/*
	 * ExpressionIdent.dec ←
	 *SymbolTable.lookup(ExpressionIdent.name)
	 *ExpressionIdent.dec != null
	 *ExpressionIdent.type ← ExpressionIdent.dec.type
	*/
	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws Exception {
		
		
		expressionIdent.dec = symbolTable.lookup(expressionIdent.name);
		
	//	System.out.println(expressionIdent.dec);
		if (expressionIdent.dec != null)
		{
			expressionIdent.type = Types.getType(expressionIdent.dec.type);
		}
		else
			throw new SemanticException(expressionIdent.firstToken, "No entry found in Symbol table");
		return expressionIdent.type;
	}
	
	/*
	ExpressionIntegerLiteral.type ← integer
	*/
	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		expressionIntegerLiteral.type=Type.INTEGER;
		return expressionIntegerLiteral.type;
	}

	/*
	 * ExpressionBooleanLiteral.type ← boolean
	*/
	@Override
	public Object visitBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
		expressionBooleanLiteral.type=Type.BOOLEAN;
		return expressionBooleanLiteral.type;
	}


	/*
	 *ExpressionFloatLiteral.type ← float
	*/
	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg) throws Exception
		{
			expressionFloatLiteral.type=Type.FLOAT;
			return expressionFloatLiteral.type;
		}

	
	/*
	 * Expression alpha .type == integer
	*Expression red .type == integer
	*Expression green .type == integer
	*Expression blue .type == integer
	*Expression.type← integer
	*/
	@Override
	public Object visitExpressionPixelConstructor(ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		Expression alpha = expressionPixelConstructor.alpha;
		Expression red = expressionPixelConstructor.red;
		Expression green = expressionPixelConstructor.green;
		Expression blue = expressionPixelConstructor.blue;
		
		if (alpha != null)
			alpha.visit(this, null);

		if (red != null)
			red.visit(this, null);

		if (green != null)
			green.visit(this, null);

		if (blue != null)
			blue.visit(this, null);
		
		//System.out.println(alpha.type + ":   :"+ red.type + ":   :"+ green.type + ":   :"+ blue.type);
	  if (alpha.type == Type.INTEGER && red.type == Type.INTEGER && green.type == Type.INTEGER && blue.type == Type.INTEGER)
		{
		 expressionPixelConstructor.type = Type.INTEGER;
		 return expressionPixelConstructor.type;
		}
	  else
		  throw new SemanticException(expressionPixelConstructor.firstToken, "Illegal Type.");
	}

	/*
	 * ExpressionPixel.name ← IDENTIFIER.name
	 * ExpressionPixel.dec ←  SymbolTable.lookup(ExpressionPixel.name)
	 * ExpressionPixel.dec != null
	 * ExpressionPixel.dec.type == image
	 * ExpressionPixel.type ← integer
	*/
	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel, Object arg) throws Exception {
		expressionPixel.dec = symbolTable.lookup(expressionPixel.name);//.getDecl();
		
		if (expressionPixel.pixelSelector !=null)
			expressionPixel.pixelSelector.visit(this, null);
		
		if (expressionPixel.dec != null)
		{
			if (Types.getType(expressionPixel.dec.type) == Type.IMAGE)
				expressionPixel.type = Type.INTEGER;
		}
		else
			throw new SemanticException(expressionPixel.firstToken, "Illegal Type match");
		return expressionPixel.type;
	}


	/*
	*ExpressionFunctionAppWithExpressionArg.type ←
	*inferredTypeFunctionApp(FunctionName,
	*Expression.type)
	*/
	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg, Object arg) throws Exception {
		Expression ex = expressionFunctionAppWithExpressionArg.e;
		if(ex!=null)
			ex.visit(this,null);
		
		if (expressionFunctionAppWithExpressionArg.function == Kind.KW_abs)
		{
			if(ex.type == Type.INTEGER)
				expressionFunctionAppWithExpressionArg.type = Type.INTEGER;
			else if (ex.type == Type.FLOAT)
				expressionFunctionAppWithExpressionArg.type = Type.FLOAT;
			else
				throw new SemanticException(expressionFunctionAppWithExpressionArg.firstToken,"Type mismatch");
			
		}
		else if (expressionFunctionAppWithExpressionArg.function == Kind.KW_alpha || expressionFunctionAppWithExpressionArg.function == Kind.KW_red || expressionFunctionAppWithExpressionArg.function == Kind.KW_green || expressionFunctionAppWithExpressionArg.function == Kind.KW_blue)
		{	//System.out.println(ex.type);
			if(ex.type == Type.INTEGER)
				expressionFunctionAppWithExpressionArg.type = Type.INTEGER;
			else
				throw new SemanticException(expressionFunctionAppWithExpressionArg.firstToken,"Type mismatch");
		}
		
		else if (expressionFunctionAppWithExpressionArg.function == Kind.KW_atan || expressionFunctionAppWithExpressionArg.function == Kind.KW_log || expressionFunctionAppWithExpressionArg.function == Kind.KW_cos || expressionFunctionAppWithExpressionArg.function == Kind.KW_sin)
		{
			if(ex.type == Type.FLOAT)
				expressionFunctionAppWithExpressionArg.type = Type.FLOAT;
			else
				throw new SemanticException(expressionFunctionAppWithExpressionArg.firstToken,"Type mismatch");
		}
		
		else if (expressionFunctionAppWithExpressionArg.function == Kind.KW_height || expressionFunctionAppWithExpressionArg.function == Kind.KW_width)
		{
			if(ex.type == Type.IMAGE)
				expressionFunctionAppWithExpressionArg.type = Type.INTEGER;
			else
				throw new SemanticException(expressionFunctionAppWithExpressionArg.firstToken,"Type mismatch");
		}
		else if (expressionFunctionAppWithExpressionArg.function == Kind.KW_float) 
		{
			if(ex.type == Type.INTEGER)
				expressionFunctionAppWithExpressionArg.type = Type.FLOAT;
			else if(ex.type == Type.FLOAT)
				expressionFunctionAppWithExpressionArg.type = Type.FLOAT;
			else
				throw new SemanticException(expressionFunctionAppWithExpressionArg.firstToken,"Type mismatch");
		}
		else if (expressionFunctionAppWithExpressionArg.function == Kind.KW_int) {
			if(ex.type == Type.INTEGER)
				expressionFunctionAppWithExpressionArg.type = Type.INTEGER;
			else if(ex.type == Type.FLOAT)
				expressionFunctionAppWithExpressionArg.type = Type.INTEGER;
			else
				throw new SemanticException(expressionFunctionAppWithExpressionArg.firstToken,"Type mismatch");
		}
		else 
			throw new SemanticException(expressionFunctionAppWithExpressionArg.firstToken,"Type mismatch");
		return expressionFunctionAppWithExpressionArg.type;
	}


	/*
	*if (FunctionName == cart_x || FunctionName == cart_y)
	*Expression 0 .type == float
	*Expression 1 .type == float
	*ExpressionFunctionAppWithPixel ← integer
	*if (FunctionName == polar_a || FunctionName == polar_r)
	*Expression 0 .type == integer
	*Expression 1 .type == integer
	*ExpressionFunctionAppWithPixel ← float
	*/
	@Override
	public Object visitExpressionFunctionAppWithPixel(ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		Expression ex1 = expressionFunctionAppWithPixel.e0;
		Expression ex2 = expressionFunctionAppWithPixel.e1;
		Kind k1 = expressionFunctionAppWithPixel.name;

		if (ex1 !=null)
			ex1.visit(this, null);
		
		if (ex2 != null)
			ex2.visit(this, null);
		
		if (k1 == Kind.KW_cart_x || k1 == Kind.KW_cart_y )
		{
			if (ex1.type == Type.FLOAT && ex2.type == Type.FLOAT)
				expressionFunctionAppWithPixel.type = Type.INTEGER;
		}
		else if (k1 == Kind.KW_polar_a || k1 == Kind.KW_polar_r )
		{
			if (ex1.type == Type.INTEGER && ex2.type == Type.INTEGER)
				expressionFunctionAppWithPixel.type = Type.FLOAT;
		}
		else
			throw new SemanticException(expressionFunctionAppWithPixel.firstToken, "Illegal Type.");
		return expressionFunctionAppWithPixel.type;
	}


	/*
	*ExpressionPredefinedName.type ← integer
	*/
	@Override
	public Object visitExpressionPredefinedName(ExpressionPredefinedName expressionPredefinedName, Object arg)throws Exception
	{
		expressionPredefinedName.type= Type.INTEGER;
		return expressionPredefinedName.type;
	}






}
