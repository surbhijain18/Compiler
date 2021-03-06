 /**
 * JUunit tests for the Scanner 
 */

package cop5556sp18;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Scanner.LexicalException;
import cop5556sp18.Scanner.Token;
import static cop5556sp18.Scanner.Kind.*;

public class ScannerTest {

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	//To make it easy to print objects and turn this output on and off
	static boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 *Retrieves the next token and checks that it is an EOF token. 
	 *Also checks that this was the last token.
	 *
	 * @param scanner
	 * @return the Token that was retrieved
	 */
	
	Token checkNextIsEOF(Scanner scanner) {
		Scanner.Token token = scanner.nextToken();
		assertEquals(Scanner.Kind.EOF, token.kind);
		assertFalse(scanner.hasTokens());
		return token;
	}


	/**
	 * Retrieves the next token and checks that its kind, position, length, line, and position in line
	 * match the given parameters.
	 * 
	 * @param scanner
	 * @param kind
	 * @param pos
	 * @param length
	 * @param line
	 * @param pos_in_line
	 * @return  the Token that was retrieved
	 */
	Token checkNext(Scanner scanner, Scanner.Kind kind, int pos, int length, int line, int pos_in_line) {
		Token t = scanner.nextToken();
		assertEquals(kind, t.kind);
		assertEquals(pos, t.pos);
		assertEquals(length, t.length);
		assertEquals(line, t.line());
		assertEquals(pos_in_line, t.posInLine());
		return t;
	}

	/**
	 * Retrieves the next token and checks that its kind and length match the given
	 * parameters.  The position, line, and position in line are ignored.
	 * 
	 * @param scanner
	 * @param kind
	 * @param length
	 * @return  the Token that was retrieved
	 */
	Token checkNext(Scanner scanner, Scanner.Kind kind, int length) {
		Token t = scanner.nextToken();
		assertEquals(kind, t.kind);
		assertEquals(length, t.length);
		return t;
	}
	


	/**
	 * Simple test case with an empty program.  The only Token will be the EOF Token.
	 *   
	 * @throws LexicalException
	 */
	@Test
	public void testEmpty() throws LexicalException {
		String input = "";  //The input is the empty string.  This is legal
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
	//	checkNextIsEOF(scanner);  //Check that the only token is the EOF token.
	}
	
	@Test
	public void test123() throws LexicalException {

		String input = "\" hello \" 123 \" 456\"";  //The input is the empty string.  This is legal
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
	}
	
	
	
	/**
	 * Test illustrating how to put a new line in the input program and how to
	 * check content of tokens.
	 * 
	 * Because we are using a Java String literal for input, we use \n for the
	 * end of line character. (We should also be able to handle \n, \r, and \r\n
	 * properly.)
	 * 
	 * Note that if we were reading the input from a file, the end of line 
	 * character would be inserted by the text editor.
	 * Showing the input will let you check your input is 
	 * what you think it is.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void testSemi() throws LexicalException {
		String input = "00077.50977(.88)|0000001299003456";//";>=9.98;\n;||;";
		
		show(input);
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it

		/*thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
//			Assert.fail("Illegal Integral literal");
			
//			fail("Failed");
		} catch (LexicalException e) {  //Catch the exception
//			show("Illegal Integer literal");                    //Display it
			
			assertEquals(2,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
		*/
		show(scanner);
		/*checkNext(scanner, SEMI, 0, 1, 1, 1);
		checkNext(scanner, OP_GE, 1, 2, 1, 2);
		checkNext(scanner, SEMI, 3, 1, 1, 4);
		checkNext(scanner, SEMI, 5, 1, 2, 1);
		checkNext(scanner, OP_OR, 6, 2, 2, 3);
		checkNext(scanner, SEMI, 8,1 , 2, 4);
		checkNextIsEOF(scanner);*/
	}
	
	
	@Test
	public void testOps() throws LexicalException
	{
		String input = "||>>{false77}(35.bac)<<+>=(show)<=";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
	//	checkNext(scanner,KW_show, 11, 1, 1, 2);
		
	//	checkNextIsEOF(scanner);
		
		
		
		
	}
	
	@Test
	public void Testing() throws LexicalException
	{
		String input = "0. .0";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
	}
	@Test
	public void testOps1() throws LexicalException
	{
		String input = "surbhi.77+jain==!=>)";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		}
	
	@Test
	public void testOps12() throws LexicalException
	{
		String input = "jhdshjvch/*g*/";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		}
	
	@Test
	public void RandomTest() throws LexicalException
	{
		String input = "7285....857(000.000)|sleep|(vfndj..89421)";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		}
	
	
	
	@Test
	public void test10() throws LexicalException {
	String input = "0. 002.";  //The input is the empty string.  This is legal
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner);		
	}
	
	
	@Test
	public void failIllegalCom() throws LexicalException {
		String input = "0. .0";//\nbbhjbds//h\nbb";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
			Scanner scanner = new Scanner(input).scan(); 
			show(scanner);
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(3,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	} 
	/**
	 * This example shows how to test that your scanner is behaving when the
	 * input is illegal.  In this case, we are giving it an illegal character '~' in position 2
	 * 
	 * The example shows catching the exception that is thrown by the scanner,
	 * looking at it, and checking its contents before rethrowing it.  If caught
	 * but not rethrown, then JUnit won't get the exception and the test will fail.  
	 * 
	 * The test will work without putting the try-catch block around 
	 * new Scanner(input).scan(); but then you won't be able to check 
	 * or display the thrown exception.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void failIllegalChar() throws LexicalException {
		String input = ";;~";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(2,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}

	@Test
	public void failIllegalno() throws LexicalException {
		String input = "!===";
		show(input);
		new Scanner(input).scan();
			Scanner scanner = new Scanner(input).scan(); 
			show(scanner);
			}

	
	@Test
	public void failIllegalnumbers() throws LexicalException {
		String input = "/**/*/*";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
			Scanner scanner = new Scanner(input).scan(); 
			show(scanner);
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(7,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}



	@Test
	public void testParens() throws LexicalException {
		String input = "(:)";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
	//	checkNext(scanner, LPAREN, 0, 1, 1, 1);
		//checkNext(scanner, RPAREN, 1, 1, 1, 2);
		//checkNextIsEOF(scanner);
	}
	

	
}
	

