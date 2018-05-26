package cop5556sp18;

import cop5556sp18.Scanner.Token;
//import cop5556sp18.Types.Type;
import cop5556sp18.Scanner.Kind;
import static cop5556sp18.Scanner.Kind.*;

import java.util.ArrayList;

import cop5556sp18.AST.*;

public class Parser {

	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}



	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}


	public Program parse() throws SyntaxException {
		Program prog = program();
		matchEOF();
		return prog;
	}

	/*
	 * Program ::= Identifier Block
	 */
	Program program() throws SyntaxException {
		Token first=t;
		Token programName = match(IDENTIFIER);
		Block block = block();
		return new Program (first , programName , block);
	}

	/*
	 * Block ::=  { (  (Declaration | Statement) ; )* }
	 */

	Kind[] firstDec = { KW_int, KW_boolean, KW_image, KW_float, KW_filename };
	Kind[] firstStatement = {KW_sleep, KW_show, KW_if, KW_while, KW_write , KW_input, KW_red ,KW_green ,KW_alpha ,KW_blue , IDENTIFIER,   };
	Kind[] colors = {KW_red ,KW_green ,KW_blue ,KW_alpha };
	Kind[] funName = {KW_sin ,KW_cos , KW_atan, KW_abs ,KW_log ,KW_cart_x ,KW_cart_y,KW_polar_a ,KW_polar_r ,KW_int ,KW_float ,KW_width ,KW_height,KW_red ,KW_green ,KW_blue ,KW_alpha};
	Kind[] Ops = { OP_LT, OP_GT, OP_LE, OP_GE };
	Kind[] predefName = { KW_Z, KW_default_height, KW_default_width};


	Block block() throws SyntaxException
	{	Token first =t;
		match(LBRACE);
		ArrayList<ASTNode> statanddec = new ArrayList<ASTNode>();
		while (isKind(firstDec)|| isKind(firstStatement))
		{
			if (isKind(firstDec))
			{	Declaration decl = declaration();
				statanddec.add(decl);
			}
			else if (isKind(firstStatement))
			{	Statement st = statement();
				statanddec.add(st);
			}

			match(SEMI);
		}
		match(RBRACE);
		return new Block(first, statanddec);

	}

	Declaration declaration() throws SyntaxException
	{
		Token first = t;
		Token type = consume();// match(KW_image);
		Token name = match (IDENTIFIER);
		Expression ex1 = null;
		Expression ex2 = null;
		if (type.kind == KW_image) {
			//match(KW_image);
			//match(IDENTIFIER);
			if(isKind(LSQUARE)) 
			{
				consume();
				ex1 = expression();
				match(COMMA);
				ex2 = expression();
				match(RSQUARE);
			}
		}
		return new Declaration(first, type, name, ex1, ex2);
	
			
	}



	Statement statement() throws SyntaxException
	{
		Statement st = null;
		if (isKind(KW_input))
		{
			st = inputSt();
		}

		else if (isKind(KW_write))
		{
			st = writeSt();
		}

		else if (isKind(KW_if))
		{
			st = ifSt();
		}

		else if (isKind(KW_while))
		{
			st = whileSt();
		}

		else if (isKind(KW_show))
		{
			st = showSt();
		}

		else if (isKind(KW_sleep))
		{
			st = sleepSt();
		}

		else if (isKind(colors) || isKind(IDENTIFIER))
		{
			st = assignmentSt();
		}

		else {
			throw new UnsupportedOperationException();
		}
		return st;
	}




	StatementInput inputSt() throws SyntaxException
	{
		Token first = t;

	//	Statement st = null;
		consume();
		Token nameDest = match(IDENTIFIER);
		match(KW_from);
		match(OP_AT);
		Expression ex = expression();
		return new StatementInput(first,nameDest, ex);

	}

	StatementWrite writeSt() throws SyntaxException
	{	Token first = t;
		//Statement st = null;
		consume();
		Token nameS = match(IDENTIFIER);
		match(KW_to);
		Token nameD = match(IDENTIFIER);
		return new StatementWrite(first, nameS , nameD);

	}

	StatementWhile whileSt() throws SyntaxException
	{	Token first = t;
		//Statement st = null;
		match(KW_while);//consume();
		match(LPAREN);
		Expression ex = expression();
		match(RPAREN);
		Block bl = block();
		return new StatementWhile(first,ex,bl);
	}

	StatementIf ifSt() throws SyntaxException
	{
		Token first = t;
		//Statement st = null;
		match(KW_if);//consume();
		match(LPAREN);
		Expression ex = expression();
		match(RPAREN);
		Block bl = block();
		return new StatementIf(first , ex, bl);

	}

	StatementShow showSt() throws SyntaxException
	{
		Token first = t;
		//Statement st = null;
		consume();
		Expression ex = expression();
		return new StatementShow(first,ex);

	}

	StatementSleep sleepSt() throws SyntaxException
	{
		Token first = t;
		//Statement st = null;
		consume();
		Expression ex = expression();
		return new StatementSleep(first,ex);

	}

	StatementAssign assignmentSt() throws SyntaxException
	{
		Token first = t;
		//Statement st = null;
		LHS lhs = LHS_exp();
		match(OP_ASSIGN);
		Expression ex = expression();
		return new StatementAssign(first, lhs, ex);

	}

	LHS LHS_exp() throws SyntaxException
	{
		Token first = t;
		Token col = null;
		if (isKind(IDENTIFIER))
		{	Token name = match(IDENTIFIER);//consume();

			if (isKind(LSQUARE))
			{
				PixelSelector pl = pixelSel();
				return new LHSPixel(first,name,pl);
			}
			return new LHSIdent(first,name);
		}

		else if (isKind(colors))
		{
			for (int i=0;i<colors.length;i++)

			{
				if(isKind(colors[i]))
				{
					col = consume();//match(colors[i]);
				}
			}
			match(LPAREN);
			Token name = match(IDENTIFIER);
			PixelSelector pl2 = pixelSel();
			match(RPAREN);
			return new LHSSample(first,name,pl2,col);
		}
		else
            throw new SyntaxException(t,"Incorrect Assignment"); //TODO  give a better error message!


	}


	PixelSelector pixelSel() throws SyntaxException
	{
		Token first = t;
		consume();
		Expression ex1 = expression();
        match(COMMA);
        Expression ex2 = expression();
        match(RSQUARE);
        return new PixelSelector (first, ex1, ex2);

	}

	Expression expression() throws SyntaxException
	{	Token first = t;
		Expression ex1 = orExpression();
		if (isKind (OP_QUESTION))
		{
			match(OP_QUESTION);
			Expression ex2 = expression();
			match(OP_COLON);
			Expression ex3 = expression();
			ex1 = new ExpressionConditional(first, ex1, ex2, ex3);
		}
		return ex1;
	}

	Expression orExpression() throws SyntaxException
	{	Token first = t;
		Expression e0 = andExpression();
        while (isKind(OP_OR))
        {
        	Token op = match(OP_OR);
        	Expression e1 = andExpression();
        	e0 = new ExpressionBinary(first, e0, op, e1);
		}
		return e0;
	}

	Expression andExpression() throws SyntaxException
	{
		Token first = t;
		Expression e0 = eqExpression();
        while (isKind(OP_AND))
        {
        	Token op = consume();
        	Expression e1 = eqExpression();
			e0 = new ExpressionBinary(first, e0, op, e1);
		}
		return e0;
	}

	Expression eqExpression() throws SyntaxException
	{
		Token first = t;
		Expression e0 = relExpression();
        while (isKind(OP_EQ) || isKind(OP_NEQ))
        {
            	Token op = consume();
            	Expression e1 = relExpression();
    			e0 = new ExpressionBinary(first, e0,op,e1);

        }
        return e0;
	}

	Expression relExpression() throws SyntaxException
	{
		Token first = t;
		Token op = null;
		Expression e0 = addExpression();
		while (isKind(OP_LT) || isKind(OP_GT) || isKind(OP_LE)|| isKind(OP_GE))
			{	op = consume();
    			Expression e1 = addExpression();
    			e0 = new ExpressionBinary(first,e0,op,e1);
			}
		return e0;
	}

	Expression addExpression() throws SyntaxException
	{
		Token first = t;
		Token op = null;
		Expression e0 = multExpression();
        while (isKind(OP_PLUS ) || isKind(OP_MINUS))
        {  	op = consume();
        	Expression e1 = multExpression();
			e0 = new ExpressionBinary(first,e0,op,e1);
        }
        return e0;
	}

	Expression multExpression() throws SyntaxException
	{
		Token first = t;
		Expression e0 = powerExpression();
		Token op =null;
        while (isKind(OP_TIMES) || isKind(OP_DIV)||isKind(OP_MOD))
        {  	op = consume();//match(OP_MOD);
           	Expression e1 = powerExpression();
			e0 = new ExpressionBinary(first,e0,op,e1);
        }
        return e0;
	}

	Expression powerExpression() throws SyntaxException
	{
		Token first = t;
		Expression e0 = unaryExpression();
		if (isKind(OP_POWER)) {
			Token op = consume();
			Expression e1 = powerExpression();
			return new ExpressionBinary(first, e0, op, e1);
		}
		return e0;
	}

	Expression unaryExpression() throws SyntaxException
	{	Token first = t;
		if(isKind(OP_PLUS) || isKind(OP_MINUS))
		{
			Token op = consume();//match(OP_PLUS);
		
			Expression ex1 = unaryExpression();
			return new ExpressionUnary(first, op, ex1);

		}
		else if(isKind(OP_EXCLAMATION))
		{
				Token op = consume();
				Expression ex2 = unaryExpression();
				return new ExpressionUnary(first,op,ex2);
		}

		else
		{
			//System.out.println("here2");

			return primary();

		}

	}


	private Expression primary() throws SyntaxException
	{	Token first = t;
        if(isKind(INTEGER_LITERAL))
        {
            Token integerliteral = match(INTEGER_LITERAL);
            return new ExpressionIntegerLiteral(first, integerliteral);
        }

        else if(isKind(BOOLEAN_LITERAL))
        {

            Token boolliteral = match(BOOLEAN_LITERAL);
			return new ExpressionBooleanLiteral(first, boolliteral);
        }

        else if(isKind(FLOAT_LITERAL))
        {
        	Token floatliteral = match(FLOAT_LITERAL);
        	return new ExpressionFloatLiteral(first, floatliteral);
        }

        else if(isKind(IDENTIFIER))
        {
            Token name = match(IDENTIFIER);
            if(isKind(LSQUARE))
            {
                PixelSelector pixel = pixelSel();
		    	return new ExpressionPixel(first, name, pixel);
            }
    		return new ExpressionIdent(first, name);
        }

        else if(isKind(LPAREN))
        {
        	consume();
            Expression ex = expression();
            match(RPAREN);
            return ex;
        }

        else if(isKind(funName) || isKind(colors))
        {
            return funAppn();
        }
        else if(isKind(LPIXEL))
        {
        	return pixelConstructor();
        }
        else if(isKind(predefName))
        {	Token t = null;
        	for (int j=0;j<predefName.length;j++)
			{
				if(isKind(predefName[j]))
				{
					t = consume();
				}
			}
        	return new ExpressionPredefinedName (first, t);
        }
        else
        {
        	throw new SyntaxException(t,"Starting of the Expression is illegal.");
        }
    }

	ExpressionPixelConstructor pixelConstructor() throws SyntaxException
	{
		Token first = t;
		ExpressionPixelConstructor epc = null;
		match(LPIXEL);
		Expression a1 = expression();
		match(COMMA);
		Expression a2 = expression();
		match(COMMA);
		Expression a3 = expression();
		match(COMMA);
		Expression a4 = expression();
		match(RPIXEL);
		epc = new ExpressionPixelConstructor(first, a1, a2, a3, a4);
		return epc;
	}




	Expression funAppn() throws SyntaxException
	{
		Token first = t;
		Token name = func();
		if(isKind(LPAREN)){
			match(LPAREN);
            Expression ex = expression();
            match(RPAREN);
			return new ExpressionFunctionAppWithExpressionArg(first, name, ex);


        }
        else if(isKind(LSQUARE)){
        	match(LSQUARE);
            Expression ex2 = expression();
            match(COMMA);
            Expression ex3 =expression();
            match(RSQUARE);
			return new ExpressionFunctionAppWithPixel(first, name, ex2, ex3);

        }
        else {
            throw new SyntaxException(t,"Parser has erros. Invalid.");
        }
	}


	Token func() throws SyntaxException
	{	Token t = null;
		if(isKind(funName))
		{
			for (int j=0;j<funName.length;j++)
			{
				if(isKind(funName[j]))
				{
					t = match(funName[j]);
				}
			}
		}
		else
			throw new SyntaxException (t, "Function name is invalid");
	return t;
	}


	/*
	ExpressionPredefinedName predefinedName() throws SyntaxException
	{	Token first = t;
		ExpressionPredefinedName ep =null;
		 for (int j=0;j<predefName.length;j++)
			{
				if(isKind(predefName[j]))
				{
					consume();
				}
			}
		 ep = new ExpressionPredefinedName (first, t);
		 return ep;
	}
	*/

	protected boolean isKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean isKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}



	/**
	 * Precondition: kind != EOF
	 *
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		Token tmp = t;
		if (isKind(kind)) {
			consume();
			return tmp;
		}
		throw new SyntaxException(t,"Token Mismatch error at "+ t.kind); //TODO  give a better error message!
	}


	private Token consume() throws SyntaxException {
		Token tmp = t;
		if (isKind( EOF)) {
			throw new SyntaxException(t,"Syntax Error. EOF checking incorrect "); //TODO  give a better error message!
			//Note that EOF should be matched by the matchEOF method which is called only in parse().
			//Anywhere else is an error. */
		}
		t = scanner.nextToken();
		return tmp;
	}


	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 *
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (isKind(EOF)) {
			return t;
		}
		throw new SyntaxException(t,"EOF not encountered."); //TODO  give a better error message!
	}


}
