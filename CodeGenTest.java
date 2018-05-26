package *******;

import static org.junit.Assert.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.RuntimeLog;
import cop5556sp18.CodeGenUtils.DynamicClassLoader;
import cop5556sp18.AST.Program;

public class CodeGenTest implements ImageFiles{
	
	//determines whether show prints anything
	static boolean doPrint = true;
	
	static void show(Object s) {
		if (doPrint) {
			System.out.println(s);
		}
	}

	//determines whether a classfile is created
	static boolean doCreateFile = false;

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	

	//values passed to CodeGenerator constructor to control grading and debugging output
	private boolean devel = true; //if true, print devel output
	private boolean grade = true; //if true, print grade output
	
//	private boolean devel = false; 
//	private boolean grade = false; 
	
	//sets the default width and height of newly created images.  Should be small enough to fit on screen.
	public static final int defaultWidth = 1024;
	public static final int defaultHeight = 1024;

	
	/**
	 * Generates bytecode for given input.
	 * Throws exceptions for Lexical, Syntax, and Type checking errors
	 * 
	 * @param input   String containing source code
	 * @return        Generated bytecode
	 * @throws Exception
	 */
	byte[] genCode(String input) throws Exception {
		
		//scan, parse, and type check
		Scanner scanner = new Scanner(input);
		show(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		Program program = parser.parse();
		TypeChecker v = new TypeChecker();
		program.visit(v, null);
//		show(program);  //It may be useful useful to show this here if code generation fails

		//generate code
		CodeGenerator cv = new CodeGenerator(devel, grade, null, defaultWidth, defaultHeight);
		byte[] bytecode = (byte[]) program.visit(cv, null);
		show(program); //doing it here shows the values filled in during code gen
		//display the generated bytecode
		show(CodeGenUtils.bytecodeToString(bytecode));
		
		//write byte code to file 
		if (doCreateFile) {
			String name = ((Program) program).progName;
			String classFileName = "bin/" + name + ".class";
			OutputStream output = new FileOutputStream(classFileName);
			output.write(bytecode);
			output.close();
			System.out.println("wrote classfile to " + classFileName);
		}
		
		//return generated classfile as byte array
		return bytecode;
	}
	
	/**
	 * Run main method in given class
	 * 
	 * @param className    
	 * @param bytecode    
	 * @param commandLineArgs  String array containing command line arguments, empty array if none
	 * @throws + 
	 * @throws Throwable 
	 */
	void runCode(String className, byte[] bytecode, String[] commandLineArgs) throws Exception  {
		RuntimeLog.initLog(); //initialize log used for grading.
		DynamicClassLoader loader = new DynamicClassLoader(Thread.currentThread().getContextClassLoader());
		Class<?> testClass = loader.define(className, bytecode);
		@SuppressWarnings("rawtypes")
		Class[] argTypes = {commandLineArgs.getClass()};
		Method m = testClass.getMethod("main", argTypes );
		show("Output from " + m + ":");  //print name of method to be executed
		Object passedArgs[] = {commandLineArgs};  //create array containing params, in this case a single array.
		try {
		m.invoke(null, passedArgs);	
		}
		catch (Exception e) {
			Throwable cause = e.getCause();
			if (cause instanceof Exception) {
				Exception ec = (Exception) e.getCause();
				throw ec;
			}
			throw  e;
		}
	}
	

	/**
	 * When invoked from JUnit, Frames containing images will be shown and then immediately deleted.
	 * To prevent this behavior, waitForKey will pause until a key is pressed.
	 * 
	 * @throws IOException
	 */
	void waitForKey() throws IOException {
		System.out.println("enter any char to exit");
		System.in.read();
	}

	/**
	 * When invoked from JUnit, Frames containing images will be shown and then immediately deleted.
	 * To prevent this behavior, keepFrame will keep the frame visible for 5000 milliseconds.
	 * 
	 * @throws Exception
	 */
	void keepFrame() throws Exception {
		Thread.sleep(5000);
	}
	
	
	
	


	/**
	 * Since we are not doing any optimization, the compiler will 
	 * still create a class with a main method and the JUnit test will
	 * execute it.  
	 * 
	 * The only thing it will do is append the "entering main" and "leaving main" messages to the log.
	 * 
	 * @throws Exception
	 */
	@Test
	public void emptyProg() throws Exception {
		String prog = "emptyProg";	
		String input = prog + "{}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {};
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n "+RuntimeLog.globalLog);
		assertEquals("entering main;leaving main;",RuntimeLog.globalLog.toString());
	}
	

	
	@Test
	public void testadd() throws Exception {
		String prog = "emptyProg";	
		String input = prog + "{show 1 + 3.0; show 1.0 + 2.0; show 3.0 +1;show 2 +2;}";//;show 9 - 4;show 9 * 4;show 9 / 4;show 9 ** 4;show 9 % 4;show 9 & 4;show 9 | 4;}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {};
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n "+RuntimeLog.globalLog);
		assertEquals("entering main;4.0;3.0;4.0;4;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void testsub() throws Exception {
		String prog = "emptyProg";	
		String input = prog + "{show 1 - 3.0; show 1.0 - 2.0; show 3.0 - 1;show 2  - 2;}";//;show 9 - 4;show 9 * 4;show 9 / 4;show 9 ** 4;show 9 % 4;show 9 & 4;show 9 | 4;}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {};
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n "+RuntimeLog.globalLog);
		assertEquals("entering main;-2.0;-1.0;2.0;0;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void unaryOpNot1() throws Exception {
		String prog = "Prog";	
		String input = prog + "{show !false; show !true; show !(true | false); show !(true & false);} ";//how 9 - 4;show 9 * 4;show 9 / 4;show 9 ** 4;show 9 % 4;show 9 & 4;show 9 | 4;}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {};
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n "+RuntimeLog.globalLog);
		assertEquals("entering main;-2.0;-1.0;2.0;0;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void unaryOpNot2() throws Exception {
		String prog = "Prog";	
		String input = prog + "{show !1; show !-1; show !0; show !-2;} ";//how 9 - 4;show 9 * 4;show 9 / 4;show 9 ** 4;show 9 % 4;show 9 & 4;show 9 | 4;}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {};
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n "+RuntimeLog.globalLog);
		assertEquals("entering main;-2;0;-1;1;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	
	@Test
	public void testdiv() throws Exception {
		String prog = "emptyProg";	
		String input = prog + "{show 6.0 / 3; show 2.0 / 1.0; show 3.0 / 1;show 2  / 2;}";//;show 9 - 4;show 9 * 4;show 9 / 4;show 9 ** 4;show 9 % 4;show 9 & 4;show 9 | 4;}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {};
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n "+RuntimeLog.globalLog);
		assertEquals("entering main;2.0;2.0;3.0;1;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	
	@Test
	public void testBinary4() throws Exception {
		String prog = "emptyProg";	
		String input = prog + "{show 9.4 + 4; show 9.5 - 4; show 9.0 * 4; show 9.0 / 4; show 9.0 ** 4;}";//;show 9 - 4;show 9 * 4;show 9 / 4;show 9 ** 4;show 9 % 4;show 9 & 4;show 9 | 4;}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {};
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n "+RuntimeLog.globalLog);
		assertEquals("entering main;13.4;5.5;36.0;2.25;6561.0;leaving main;",RuntimeLog.globalLog.toString());
	}
	@Test
	public void testBinary3() throws Exception {
		String prog = "emptyProg";	
		String input = prog + "{show 9 + 4.5;show 9 - 4.5;show 9 * 4.5;show 8 / 4.2;show 9 ** 4.0;}";//;show 9 - 4;show 9 * 4;show 9 / 4;show 9 ** 4;show 9 % 4;show 9 & 4;show 9 | 4;}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {};
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n "+RuntimeLog.globalLog);
		assertEquals("entering main;13.5;4.5;40.5;1.904762;6561.0;leaving main;",RuntimeLog.globalLog.toString());
	}
	@Test
	public void testBinary2() throws Exception {
		String prog = "emptyProg";	
		String input = prog + "{show 5.5 + 4.5 ; show 9.5 - 4.5; show 9.0 * 4.5; show 15.0 / 2.5; show 9.1 ** 4.1;}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {};
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n "+RuntimeLog.globalLog);
		assertEquals("entering main;10.0;5.0;40.5;6.0;8552.039;leaving main;",RuntimeLog.globalLog.toString());
	}
	@Test
	public void testBinary1() throws Exception {
		String prog = "emptyProg";	
		String input = prog + "{show 9 % 4;show 9 - 4;show 9 * 4;show 9 / 4;show 9 ** 4;show 9 % 4;show 9 & 4;show 9 | 4;}";//;show 9 - 4;show 9 * 4;show 9 / 4;show 9 ** 4;show 9 % 4;show 9 & 4;show 9 | 4;}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {};
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n "+RuntimeLog.globalLog);
		assertEquals("entering main;1;5;36;2;6561;1;0;13;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	
	
	
	
	
	
	@Test
	public void testmul() throws Exception {
		String prog = "emptyProg";	
		String input = prog + "{show 1 * 3.0; show 1.0 * 2.0; show 3.0 * 1;show 2  * 2;}";//;show 9 - 4;show 9 * 4;show 9 / 4;show 9 ** 4;show 9 % 4;show 9 & 4;show 9 | 4;}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {};
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n "+RuntimeLog.globalLog);
		assertEquals("entering main;3.0;2.0;3.0;4;leaving main;",RuntimeLog.globalLog.toString());
	}
	@Test
	public void testpower() throws Exception {
		String prog = "emptyProg";	
		String input = prog + "{show 3 ** 3.0; show 4.0 ** 2.0; show 3.0 ** 3;show 2  ** 2;}";//;show 9 - 4;show 9 * 4;show 9 / 4;show 9 ** 4;show 9 % 4;show 9 & 4;show 9 | 4;}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {};
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n "+RuntimeLog.globalLog);
		assertEquals("entering main;27.0;16.0;27.0;4;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void test1Prog() throws Exception {
		String prog = "emptyProg";	
		String input = prog + "{show 9 - 4;show 9 * 4;show 9 / 4;show 9 ** 4;show 9 % 4;show 9 & 4;show 9 | 4;}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {};
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n "+RuntimeLog.globalLog);
		assertEquals("entering main;5;36;2;6561;1;0;13;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void testBool() throws Exception {
		String prog = "Prog";	
		String input = prog + "{boolean c; boolean d; d:=false; c := true; show !c; show !d; }";//;show 9 - 4;show 9 * 4;show 9 / 4;show 9 ** 4;show 9 % 4;show 9 & 4;show 9 | 4;}";
		byte[] bytecode = genCode(input);
		String[] commandLineArgs = {};
		runCode(prog, bytecode, commandLineArgs);
		show("Log:\n "+RuntimeLog.globalLog);
		assertEquals("entering main;false;true;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void assignImageWithDifferentSize() throws Exception {
		String prog = "Prog";
		String input = prog + "{image y;\n  show y;}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void declareAssignBoolean() throws Exception {
		String prog = "Prog";
		String input = prog + "{ boolean y; y := true; show y; y := false; show y;}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;true;false;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void integerLit() throws Exception {
		String prog = "prog";
		String input = prog + "{ int var1; float var2; image var3;var1 := width(var3); var1 := height(var3); var2 := float(1); var1 := int(1.0);}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	
	@Test
	public void assignFile() throws Exception {
		String prog = "Prog";
		String input = prog + "{ filename f1;\n filename f2; \n input f1 from @ 0 ;\n f2 := f1;}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {file1};//"C:\\Users\\Surbhi\\eclipse-workspace\\cop5556sp18\\src\\cop5556sp18\\test_file.txt"}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		//assertEquals("entering main;3.0;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void unaryOpMinus() throws Exception {
		String prog = "Prog";
		String input = prog + "{show -3; show -4.5; show -3-4.5;}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;-3;-4.5;-7.5;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void readImageFromCommandLineAndScale() throws Exception {
		String prog = "Prog";
		String input = prog + "{image y[300,400];\n  input y from @ 0 ;\n show y;sleep(4000);}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {file3}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
//		BufferedImage refImage0 = RuntimeImageSupport.readFromFile(file3);
//		BufferedImage loggedImage0 = RuntimeLog.globalImageLog.get(0);
	}
	@Test
	public void declareAssignFloat() throws Exception {
		String prog = "Prog";
		String input = prog + "{float y; y := 6.6; show y; y := -0.5; show y;}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;6.6;-0.5;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void assignImage1() throws Exception {
		String prog = "Prog";
		String input = prog + "{image x; input x from @ 0 ; show x; image y; y := x; show y;}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {file1}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
//		BufferedImage refImage0 = RuntimeImageSupport.readFromFile(file1);
//		BufferedImage loggedImage0 = RuntimeLog.globalImageLog.get(0);
	}
	
	@Test
	public void assignImage2() throws Exception {
		String prog = "Prog";
		String input = prog + "{image y[1000,1000]; image copy[1000,1000]; input y from @ 0 ; show y; copy := y; show copy;}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {file1}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
//		BufferedImage refImage0 = RuntimeImageSupport.readFromFile(file1);
//		BufferedImage loggedImage0 = RuntimeLog.globalImageLog.get(0);
	}
	
	@Test
	public void testExpressionFuncArg1() throws Exception {
		String prog = "Prog";
		String input = prog + "{image b[512,256]; show width(b); show height(b);\nimage c;}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;512;256;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void testExpressionFuncArg2() throws Exception {
		String prog = "Prog";
		String input = prog + "{show abs(1); show abs(-1); show abs(5.3); show abs(-5.3); show abs(1-5.3);}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;1;1;5.3;5.3;4.3;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void testExpressionFuncArg3() throws Exception {
		String prog = "Prog";
		String input = prog + "{int a; a := 123456789;\n show red(a); show green(a); show blue(a);}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;91;205;21;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void testExpressionFuncArg4() throws Exception {
		String prog = "Prog";
		String input = prog + "{int a; a := 123456789; show alpha(a);\n a := -1; show alpha(a);}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;7;255;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	
	@Test
	public void testExpressionFuncArg5() throws Exception {
		String prog = "Prog";
		String input = prog + "{float a; a := float(-3.7); show a; a := float(4); show a;}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;-3.7;4.0;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void testExpressionFuncArg6() throws Exception {
		String prog = "Prog";
		String input = prog + "{int a; a := int(-3.7); show a; a := int(4); show a;}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;-3;4;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void testAssociativityPrecedence4() throws Exception {
		String prog = "Prog";
		String input = prog + "{show 4 ** 3 ** 2;}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;262144;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	
	@Test
	public void declareAssignInteger() throws Exception {
		String prog = "Prog";
		String input = prog + "{int y; y := 55; show y; y := -234; show y;}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;55;-234;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void sometest() throws Exception {
		String prog = "Prog";
		String input = prog + "{show sin(10.0);}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;55;-234;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	
	
	@Test
	public void createNonDefaultSizeImage() throws Exception {
		String prog = "Prog";
		String input = prog + "{image y[512,256];\n  show y;}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {file1}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
//		BufferedImage refImage0 = RuntimeImageSupport.readFromFile(file1);
//		BufferedImage loggedImage0 = RuntimeLog.globalImageLog.get(0);
	}
	
	@Test
	public void readFromCommandLine() throws Exception {
		String prog = "Prog";
		String input = prog + "{int x; input x from @ 0 ; show x;\nfloat y; input y from @ 1; show y;\nboolean z; input z from @ 2; show z;\ninput z from @ 3; show z;}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {"3","5.0","true","false"}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;3;5.0;true;false;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void readImageFromCommandLine() throws Exception {
		String prog = "Prog";
		String input = prog + "{image y; input y from @ 0 ; show y; sleep(2000);}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {file1}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;leaving main;",RuntimeLog.globalLog.toString());

		//BufferedImage refImage0 = RuntimeImageSupport.readFromFile(file1);
		//BufferedImage loggedImage0 = RuntimeLog.globalImageLog.get(0);
	}
	
	
	@Test
	public void imagetest() throws Exception {
		String prog = "prog";
		String input = prog + "{image y; image copy[128,256]; input y from @ 0 ; show y; copy := y; show copy;}";
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {file1}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);
		keepFrame();	
	}
	

	

	
	@Test
	public void checkif() throws Exception {
		String prog = "Prog";
		String input = prog + "{int a; a :=2; int b ;b:=5; if(b>a){show a+b;};}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;7;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void checkif2() throws Exception {
		String prog = "Prog";
		String input = prog + "{float a; a :=5.0; float b ;b:=5.0; if(b <= a){show a+b;};}";// if (a < b) {show b;};}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;7.0;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void checkif3() throws Exception {
		String prog = "Prog";
		String input = prog + "{boolean a; a := true; boolean b; b := true; if(a&b) {show b;};}";// if (a < b) {show b;};}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;true;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void checkwhile1() throws Exception {
		String prog = "Prog";
		String input = prog + "{boolean a; a := true; boolean b; b := true; while(a & b){ b := false; show b;};}";// if (a < b) {show b;};}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;false;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void checkConditional() throws Exception {
		String prog = "Prog";
		String input = prog + "{boolean cond; cond:= true & true; int var1; var := 2; var1 := cond ? var1+ 1 : var1; show var1;}"; //float var2; var2 := 3.0; var2 := cond ? var2 + 1.0 : var2;}";// if (a < b) {show b;};}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;true;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void checkWrite() throws Exception {
		String prog = "Prog";
		String input = prog + "{image image1; input image1 from @ 0; show image1; sleep(4000); filename f1; }";//write image1 to f1;}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {file3}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void checkvalidFunctionAppWithPixel2() throws Exception {
		String prog = "Prog";
		String input = prog + "{float var1; var1 := polar_a[1,1];show var1; var1 := polar_r[1,1]; show var1; }";//write image1 to f1;}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;0.7853982;1.4142135;leaving main;",RuntimeLog.globalLog.toString());
	}
	@Test
	public void checkvalidFunctionAppWithPixel1() throws Exception {
		String prog = "Prog";
		String input = prog + "{int var1; var1 := cart_x[2.0,0.0];show var1; var1 := cart_y[2.0,90.0]; show var1; }";//write image1 to f1;}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;2;1;leaving main;",RuntimeLog.globalLog.toString());
	}
	@Test
	public void checkExpressionPixel() throws Exception {
		String prog = "Prog";
		String input = prog + "{image var1; int var2; var2 := var1[0,0]; }";//write image1 to f1;}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;leaving main;",RuntimeLog.globalLog.toString());
	}
	@Test
	public void checkvalidFunctionAppWithPixel3() throws Exception {
		String prog = "Prog";
		String input = prog + "{int var1; var1 := cart_x[2.0,0.0];show var1; var1 := cart_y[2.0,90.0]; show var1; }";//write image1 to f1;}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;2;1;leaving main;",RuntimeLog.globalLog.toString());
	}
	@Test
	public void checkPixelConstructor() throws Exception {
		String prog = "Prog";
		String input = prog + "{image im[1024,1024];input im from @ 0;int x; x:=0; int y; y:=0; im[x,y] := <<0,0,0,0>>; show im; sleep(4000); }";//write image1 to f1;}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {file3}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;leaving main;",RuntimeLog.globalLog.toString());
	}
	@Test
	public void makeRedImage() throws Exception {
		String prog = "Prog";
		String input = prog + "{"
				+ "image im[256,256];\n"
				+ "int x;\n"
				+ "int y; \n"
				+ "x := 0; \n"
				+ "y := 0; \n"
				+ "while (x < width(im)){ \n"
				+ "y := 0;\n "
				+ "while (y < height(im)) { \n"
				+ "im[x,y] := <<255,255,0,0>>; \n"
				+ "y := y + 1; \n"
				+ "};\n"
				+ "x := x +1;};\n"
				+ "show im;\n}";	

		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		keepFrame();
}
	@Test
	public void testExpressionPixelConstructor1() throws Exception {
		String prog = "prog";
		String input = prog + "{show <<0,0,0,0>>; show <<255,255,255,255>>;}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		//assertEquals("entering main;leaving main;",RuntimeLog.globalLog.toString());
	}
	

	
	@Test
	public void polarR2() throws Exception {  //arrayoutofboundexception 1
		String prog = "Prog";
		String input = prog + "{ image im[1024,1024];"
				//+ "input im from @ 0;"
				+ "int x;\n"
				+ "x :=0; \n"
				+ "show width(im);"
				+ "while (x < width(im)) { \n"
				+ "int y; \n"
				+ "y := 0;"
				+ "while (y < height (im)) {\n"
				+ "float p;\n"
				+ "p := polar_r[x,y];\n"
				+ "int r;\n"
				+ "r := int(p)%Z;\n"
				+ "im[x,y] := <<Z,0,0,r>>; \n"
				+ "y := y+1; \n"
				+ "};\n"
				+ "x := x +1;};\n"
				+ "show im;\n"
				+ "}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {file3}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		keepFrame();
		
		System.out.println("pixel(0,0,0,1)=" + RuntimePixelOps.makePixel(0,0,0,1));
	}
	
	
	@Test
	public void demo1() throws Exception {
		String prog = "prog";
		String input = prog + "{image h;input h from @0;show h; sleep(4000); image g[width(h),height(h)];int x;x:=0;while(x<width(g)){int y;y:=0;while(y<height(g)){g[x,y]:=h[y,x];y:=y+1;};x:=x+1;};show g;sleep(4000);}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {file3}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	

	@Test
	public void samples() throws Exception {
		String prog = "prog";
		String input = prog + "{image bird; input bird from @ 0;show bird;sleep(4000);image bird2[width(bird),height(bird)];int x;x:=0;while(x<width(bird2)) {int y;y:=0;while(y<height(bird2)) {blue(bird2[x,y]):=red(bird[x,y]);green(bird2[x,y]):=blue(bird[x,y]);red(bird2[x,y]):=green(bird[x,y]);alpha(bird2[x,y]):=Z;y:=y+1;};x:=x+1;};show bird2;sleep(4000);}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {file1}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void writeRedImage() throws Exception { //color blue coming instead of red
		String prog = "prog";
		String input = prog + "{image im[256,256]; \nfilename f; \ninput f from @0; \nint x;\n int y; \nx := 0; \ny := 0; \nwhile (x < width(im)){ \n y := 0; while (y < height(im)){\nim[x,y] := <<255,255,0,0>>; \nint z; z := im[x,y];y := y + 1; \n};\nx := x + 1;};\nwrite im to f;\n}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {RedImage}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void writeModifiedImage() throws Exception {
		String prog = "prog";
		String input = prog + "{image im; \ninput im from @0; \nfilename f; \ninput f from @1; \nint x;\n int y; \nx := 0; \ny := 0; \nwhile (x < width(im)){ \n y := 0; while (y < height(im)){\nim[x,y] := <<15,255,0,0>>; \nint z; z := im[x,y];y := y + 1; \n};\nx := x + 1;};\nwrite im to f;\n}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {file3, check}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void testBlockScope2() throws Exception {
		String prog = "prog";
		String input = prog + "{ if(true){ int x; }; int x; x := 5; show x;}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; 
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;5;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	
	@Test
	public void writeTransparentImage() throws Exception {
		String prog = "prog";
		String input = prog + "{ image im[256,256]; \nfilename f; \ninput f from @0; \nint x;\n int y; \nx := 0; \ny := 0; \nwhile (x < width(im)){ \n y := 0; while (y < height(im)){\nim[x,y] := <<15,255,0,0>>; \nint z; z := im[x,y];y := y + 1; \n};\nx := x + 1;};\nwrite im to f;}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {transparent}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void writeImageToFile() throws Exception {
		String prog = "prog";
		String input = prog + "{image y;\n filename f;\n input y from @ 0 ; input f from @1; \n show y; write y to f;}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {file1, file}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		assertEquals("entering main;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	@Test
	public void testExpressionPixelConstructor2() throws Exception {
		String prog = "prog";
		String input = prog + "{show <<25,137,10,67>>; show <<1000,-50,1000,-10>>;}";	
		byte[] bytecode = genCode(input);		
		String[] commandLineArgs = {}; //create command line argument array to initialize params, none in this case		
		runCode(prog, bytecode, commandLineArgs);	
		show("Log:\n"+RuntimeLog.globalLog);
		//assertEquals("entering main;leaving main;",RuntimeLog.globalLog.toString());
	}
	
	
}
