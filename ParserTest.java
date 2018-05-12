 /**
 * JUunit tests for the Parser for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Spring 2018.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Spring 2018 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2018
 */

package cop5556sp18;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Scanner.Kind;
import cop5556sp18.Parser;
import cop5556sp18.Scanner;
import cop5556sp18.AST.ASTNode;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
import cop5556sp18.AST.Expression;
import cop5556sp18.AST.ExpressionBinary;
import cop5556sp18.AST.ExpressionIdent;
import cop5556sp18.AST.ExpressionIntegerLiteral;
import cop5556sp18.AST.StatementInput;
import cop5556sp18.AST.StatementShow;
import cop5556sp18.AST.LHS;
import cop5556sp18.AST.LHSIdent;
import cop5556sp18.AST.LHSPixel;
import cop5556sp18.AST.LHSSample;
import cop5556sp18.AST.PixelSelector;
import cop5556sp18.AST.Program;
import cop5556sp18.AST.Statement;
import cop5556sp18.AST.StatementAssign;
import cop5556sp18.AST.StatementWrite;
import cop5556sp18.Parser.SyntaxException;
import cop5556sp18.Scanner.LexicalException;
import cop5556sp18.Scanner.Token;
import static cop5556sp18.Scanner.Kind.*;

public class ParserTest {

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	//To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}


	//creates and returns a parser for the given input.
	private Parser makeParser(String input) throws LexicalException {
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		Parser parser = new Parser(scanner);
		return parser;
	}
	

	
	/**
	 * Simple test case with an empty program.  This throws an exception 
	 * because it lacks an identifier and a block
	 *   
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
		
	/**
	 * Smallest legal program.
	 *   
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */

	/**
	 * Checks that an element in a block is a declaration with the given type and name.
	 * The element to check is indicated by the value of index.
	 * 
	 * @param block
	 * @param index
	 * @param type
	 * @param name
	 * @return
	 */
	Declaration checkDec(Block block, int index, Kind type,
			String name) {
		ASTNode node = block.decOrStatement(index);
		assertEquals(Declaration.class, node.getClass());
		Declaration dec = (Declaration) node;
		assertEquals(type, dec.type);
		assertEquals(name, dec.name);
		return dec;
	}	
	
	@Test
	public void testDec0() throws LexicalException, SyntaxException {
		String input = "b{int c; image j;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);	
		checkDec(p.block, 0, Kind.KW_int, "c");
		checkDec(p.block, 1, Kind.KW_image, "j");
	}
	
	
	/** This test illustrates how you can test specific grammar elements by themselves by
	 * calling the corresponding parser method directly, instead of calling parse.
	 * This requires that the methods are visible (not private). 
	 * 
	 * @throws LexicalException
	 * @throws SyntaxException
	 */
	
	@Test
	public void testExpression() throws LexicalException, SyntaxException {
		String input = "x + 2";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);	
		assertEquals(ExpressionBinary.class, e.getClass());
		ExpressionBinary b = (ExpressionBinary)e;
		assertEquals(ExpressionIdent.class, b.leftExpression.getClass());
		ExpressionIdent left = (ExpressionIdent)b.leftExpression;
		assertEquals("x", left.name);
		assertEquals(ExpressionIntegerLiteral.class, b.rightExpression.getClass());
		ExpressionIntegerLiteral right = (ExpressionIntegerLiteral)b.rightExpression;
		assertEquals(2, right.value);
		assertEquals(OP_PLUS, b.op);
	}
	
	@Test
	public void testExpression1() throws LexicalException, SyntaxException {
		String input = "+2";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}
	@Test
	public void test2() throws LexicalException, SyntaxException {
		String input = "x**+2";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}
	@Test
	public void test3() throws LexicalException, SyntaxException {
		String input = "+x";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}
	@Test
	public void test4() throws LexicalException, SyntaxException {
		String input = "+0.0";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}
	@Test
	public void test5() throws LexicalException, SyntaxException {
		String input = "+Z";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}
	@Test
	public void test6() throws LexicalException, SyntaxException {
		String input = "-+2";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}
	@Test
	public void test7() throws LexicalException, SyntaxException {
		String input = "++2";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}
	@Test
	public void test8() throws LexicalException, SyntaxException {
		String input = "a**b**c*d/e%f+g*d-d*e<a+b>c+d<=e+f>=g+h==c!=d&a&b|c|d?a:b";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}
	@Test
	public void test9() throws LexicalException, SyntaxException {
		String input = "f==a-b*5";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}
	@Test
	public void test10() throws LexicalException, SyntaxException {
		String input = "a**b**c*d/e%f+g*d-d*e<a+b>c+d<=e+f>=g+h==c!=d&a&b|c|d";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}
	@Test
	public void test11() throws LexicalException, SyntaxException {
		String input = "while x {}";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}
	@Test
	public void test12() throws LexicalException, SyntaxException {
		String input = "while 1 {}";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}
	@Test
	public void test13() throws LexicalException, SyntaxException {
		String input = "while 2.0 {}";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}
	@Test
	public void test14() throws LexicalException, SyntaxException {
		String input = "while Z {}";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}
	@Test
	public void test15() throws LexicalException, SyntaxException {
		String input = "while 1+2 {}";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}
	@Test
	public void test16() throws LexicalException, SyntaxException {
		String input = "while 5&4 {}";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}
	@Test
	public void test17() throws LexicalException, SyntaxException {
		String input = "**b**c*d/e%f+g*d-d*e<a+b>c+d<=e+f>=g+h==c!=d";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}
	@Test
	public void test18() throws LexicalException, SyntaxException {
		String input = "if x {}";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}
	@Test
	public void test19() throws LexicalException, SyntaxException {
		String input = "if 1 {}";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}
	@Test
	public void test20() throws LexicalException, SyntaxException {
		String input = "if 1.0 {}";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}
	@Test
	public void test21() throws LexicalException, SyntaxException {
		String input = "if Z {}";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}
	@Test
	public void test22() throws LexicalException, SyntaxException {
		String input = "if 1+2 {}";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}
	@Test
	public void test23() throws LexicalException, SyntaxException {
		String input = "if 8&2 {}";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}
	@Test
	public void test24() throws LexicalException, SyntaxException {
		String input = "x==(1+2)*5";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}
	@Test
	public void test25() throws LexicalException, SyntaxException {
		String input = "a**b**c*d/e%f+g*d-d*e<a+b>c+d<=e+f>=g+h==c!=d&a&b";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}
	@Test
	public void test26() throws LexicalException, SyntaxException {
		String input = "a==25";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}
	@Test
	public void test27() throws LexicalException, SyntaxException {
		String input = "x==1+2";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);
	}




}
	

