package cop5556sp18;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Parser;
import cop5556sp18.Scanner;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Program;
import cop5556sp18.TypeChecker.SemanticException;

public class TypeCheckerTest {

	/*
	 * set Junit to be able to catch exceptions
	 */
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Prints objects in a way that is easy to turn on and off
	 */
	static final boolean doPrint = true;

	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 * Scans, parses, and type checks the input string
	 * 
	 * @param input
	 * @throws Exception
	 */
	void typeCheck(String input) throws Exception {
		show(input);
		// instantiate a Scanner and scan input
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		// instantiate a Parser and parse input to obtain and AST
		Program ast = new Parser(scanner).parse();
		show(ast);
		// instantiate a TypeChecker and visit the ast to perform type checking and
		// decorate the AST.
		ASTVisitor v = new TypeChecker();
		ast.visit(v, null);
	}



	/**
	 * Simple test case with an almost empty program.
	 * 
	 * @throws Exception
	 */
	@Test
	public void emptyProg() throws Exception {
		String input = "emptyProg{}";
		typeCheck(input);
	}
	
	@Test
	public void expression1() throws Exception {
		String input = "prog {show 3+4;}";
		typeCheck(input);
	}

	@Test
	public void expression2_fail() throws Exception {
		String input = "prog { float a; b:=5;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	
	 /*
		*ExpressionPredefinedName.type â†� integer
		*/@Test
    public void simpleImage() throws Exception {
        String input = "X{ image im[1,2]; }";
        typeCheck(input);
    }
    
    @Test
    public void simpleImageFail() throws Exception {
        String input = "X{ int im;image im[1.0, 2]; }";
        
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }
    
    
    /*
   	*ExpressionPredefinedName.type â†� integer
   	*/
    @Test
    public void nestedDec1() throws Exception {
        String input = "X{ int x; int y; while (x == y) {int x;}; }";
        typeCheck(input);
    }
    
    @Test
    public void nestedDec2() throws Exception {
        String input = "X{ int x; int z; while (x == y) {int x;}; }";
        thrown.expect(SemanticException.class);
        try {
            typeCheck(input);
        } catch (SemanticException e) {
            show(e);
            throw e;
        }
    }
    
    /*
   	*ExpressionPredefinedName.type â†� integer
   	*/
    @Test
        public void nestedDec3() throws Exception {
            String input = "X{ int x; int y; if (x == y) { show x;}; }";
            typeCheck(input);
    }
    
    /*
	*ExpressionPredefinedName.type â†� integer
	*/
    
    @Test
    public void nestedDec4() throws Exception {
        String input = "X{ int x; int y; while (x == y) { int z;}; show z;}";
        thrown.expect(SemanticException.class);
        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }   
        }

    @Test
	public void testvalidFunctionAppWithArg2() throws Exception {
		String input = "prog {float var2;var2 := abs(1.0); var2 := sin(1.0); "
				+ "var2 := cos(1.0); var2 := atan(1.0); var2 := float(1.0); var2 := log(1.0);}";
		typeCheck(input);
		
	}
    @Test
    public void testinvalidDeclarations1() throws Exception {
        String input = "prog{int var1; float var2; boolean var3; image var4; filename var5; image var6[500,500];if(true){int var1; float var2; boolean var3; image var4; filename var5; image var6[500,500];};float var1;}";
      thrown.expect(SemanticException.class);
        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
//       typeCheck(input);
    }
   
    @Test
    public void testinvalidDeclarations2() throws Exception {
        String input = "prog{image var1[1.0, 500];}";
        thrown.expect(SemanticException.class);
        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
    }
	
	
	@Test
	public void testvalidExpressionBinary1() throws Exception {
		String input = "prog{ int var1; boolean var2;var1 := 1 + 2; var1 := 1 - 2; "
				+ "var1 := 1 * 2; var1 := 1 / 2; var1 := 1 ** 2; var1 := 1 % 2; "
				+ "var1 := 1 & 2; var1 := 1 | 2; var2 := 1 == 2; var2 := 1 != 2; "
				+ "var2 := 1 > 2; var2 := 1 >= 2; var2 := 1 < 2; var2 := 1 <= 2;}";
		typeCheck(input);

	}
	
	@Test
	public void testvalidExpressionBinary2() throws Exception {
		String input = "prog{ float var1; boolean var2;var1 := 1.0 + 2.0; var1 := 1.0 - 2.0; "
				+ "var1 := 1.0 * 2.0; var1 := 1.0 / 2.0; var1 := 1.0 ** 2.0; var2 := 1.0 == 2.0; "
				+ "var2 := 1.0 != 2.0; var2 := 1.0 > 2.0; var2 := 1.0 >= 2.0; var2 := 1.0 < 2.0; "
				+ "var2 := 1.0 <= 2.0;}";
		typeCheck(input);

	}
	
	@Test
	public void testvalidExpressionBinary4() throws Exception {
		String input = "prog{ float var2;var2 := 1 + 2.0; var2 := 1 - 2.0; var2 := 1 * 2.0; "
				+ "var2 := 1 / 2.0; var2 := 1 ** 2.0; var2 := 1.0 + 2; var2 := 1.0 - 2; "
				+ "var2 := 1.0 * 2; var2 := 1.0 / 2; var2 := 1.0 ** 2;}";
		 typeCheck(input);
	}
	
	
	//check
	@Test
	public void testvalidFunctionAppWithPixel2() throws Exception {
		String input = "prog {float var1; var1 := polar_a[1,1]; var1 := polar_r[1,1];}";
		typeCheck(input);

	}
	
	@Test
	public void testvalidExpressionPixel1() throws Exception {
		String input = "prog{ image var1; int var2; var2 := var1[0,0];}";
		typeCheck(input);

	}
		

    @Test
    public void testinvalidStatementInput1() throws Exception {
        String input = "prog{input var from @1; int var;}";
        thrown.expect(SemanticException.class);
        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
    }
	

    @Test
    public void testinvalidStatementInput2() throws Exception {
        String input = "prog{if(true){int var;}; if(true){input var from @1;};}";
        thrown.expect(SemanticException.class);
        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
    }
	

    @Test
    public void testinvalidStatementInput3() throws Exception {
        String input = "prog{if(true){int var;}; input var from @1;}";
        thrown.expect(SemanticException.class);
        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
    }
    @Test
    public void testvalidStatementShow1() throws Exception {
        String input = "prog{boolean a; int b; float c; image d;show a; show b; show c; show d;}";
        typeCheck(input);
    }
	
    //check
    @Test
    public void testinvalidStatementWrite2() throws Exception {
        String input = "prog{filename f1; write image1 to f1;}";
        thrown.expect(SemanticException.class);
        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
    }
	
    @Test
	public void testvalidPixelSelector1() throws Exception {
		String input = "prog{image var1; red( var1[1,1]) := 5; red( var1[1.0,1.0]) := 5;}";
		typeCheck(input);

	}

    //check
    @Test
    public void testvalidStatementInput1() throws Exception {
        String input = "prog{int var; int var2;input var from @1; input var from @var2; input var from @<<1,2,3,4>>;}";
		typeCheck(input);

    }
	
    @Test
    public void testvalidStatementWhile1() throws Exception {
        String input = "prog{boolean a; boolean b; while(a & b){};}";
		typeCheck(input);

    }
	
    @Test
    public void testinvalidFunctionAppWithArg2() throws Exception {
        String input = "prog {float var1;var1 := abs(1);}";
        thrown.expect(SemanticException.class);
        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }    }
	
    //check
    @Test
    public void testScope1() throws Exception {
        String input = "p{int var; if(true) {float var; var := 5.0;}; var := 5;}";
        typeCheck(input);
    }
    
    ///* \  
    @Test
    public void testinvalidStatementInput4() throws Exception {
        String input = "prog{float var; input var from @var;}";
        thrown.expect(SemanticException.class);
        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
    }
	
	
   

  

    @Test
    public void testExpressionFunctionAppWithPixel3() throws Exception {
        String input = "prog { int var2; var2 := cart_x[1,6.5];}";
        thrown.expect(SemanticException.class);
        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
    }
	

    @Test
    public void testExpressionFunctionAppWithPixel4() throws Exception {
        String input = "prog { int var2; var2 := cart_x[1.0,6];}";
        thrown.expect(SemanticException.class);
        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
    }
	

    @Test
    public void testExpressionFunctionAppWithPixel5() throws Exception {
        String input = "prog { float var2; var2 := polar_a[1.0,6];}";
        thrown.expect(SemanticException.class);
        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
    }
	

    @Test
    public void testExpressionFunctionAppWithPixel6() throws Exception {
        String input = "prog { float var2; var2 := polar_a[1,6.5];}";
        thrown.expect(SemanticException.class);
        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
    }
	

  

    @Test
    public void testvalidStatementWrite1() throws Exception {
        String input = "prog{image image1; filename f1; write image1 to f1;}";
        typeCheck(input);
    }
	

    @Test
    public void testinvalidStatementAssign1() throws Exception {
        String input = "prog{int var1; var1 := 1.0; float var2; var2 := 1;boolean var3; var3 := 1;filename f1; f1 := 1;image var4; var4 := 1;image var5[500,500]; var5 := 1;}";
        thrown.expect(SemanticException.class);
        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
    }
	

    @Test
    public void testinvalidStatementAssign2() throws Exception {
        String input = "prog{image var; var[0,0] := 1.0;alpha(var[0,0]) := 1.0; red(var[0,0]) := 1.0; green(var[0,0]) := 1.0; blue(var[0,0]) := 1.0;}";
        thrown.expect(SemanticException.class);
        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
    }
	

    @Test
    public void testinvalidFunctionAppWithArg1() throws Exception {
        String input = "prog {int var1;var1 := abs(1.0);}";
        thrown.expect(SemanticException.class);
        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
    }
	

    

    @Test
    public void testinvalidFunctionAppWithArg3() throws Exception {
        String input = "prog {int var1;var1 := red(1.0);}";
        thrown.expect(SemanticException.class);
        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
    }
	

    @Test
    public void testinvalidFunctionAppWithArg4() throws Exception {
        String input = "prog {float var1;var1 := sin(1);}";
        thrown.expect(SemanticException.class);
        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
    }
	

    @Test
    public void testinvalidFunctionAppWithArg5() throws Exception {
        String input = "prog {int var1;var1 := width(1);}";
        thrown.expect(SemanticException.class);
        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
    }
	

    @Test
    public void testinvalidFunctionAppWithArg6() throws Exception {
        String input = "prog {int var1;var1 := cart_x(1.0);}";
        thrown.expect(SemanticException.class);
        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
    }
	

    @Test
    public void testinvalidFunctionAppWithArg7() throws Exception {
        String input = "prog {float var1;var1 := polar_a(1);}";
        thrown.expect(SemanticException.class);
        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
    }
	

    @Test
    public void testinvalidFunctionAppWithArg8() throws Exception {
        String input = "prog {int var1;var1 := cart_y(1);}";
        thrown.expect(SemanticException.class);
        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
    }
	

    @Test
    public void testinvalidFunctionAppWithArg9() throws Exception {
        String input = "prog {float var1;var1 := polar_r(1.0);}";
        thrown.expect(SemanticException.class);
        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
    }
	


	
	
	
	@Test
	public void testvalidExpressionBinary3() throws Exception {
		String input = "prog{ boolean var1;var1 := true & false; var1 := true | false; "
				+ "var1 := true == false; var1 := true != false; var1 := true > false; "
				+ "var1 := true >= false; var1 := true < false; var1 := true <= false;}";
		typeCheck(input);
	}
	


	@Test
	public void testvalidFunctionAppWithPixel1() throws Exception {
		String input = "prog {int var1; var1 := cart_x[1.0,1.0]; var1 := cart_y[1.0,1.0];}";
		typeCheck(input);
	}
	

	

	@Test
	public void testvalidDeclarations1() throws Exception {
		String input = "prog{int var1; float var2; boolean var3; image var4; "
				+ "filename var5; image var6[500,500];}";
		 typeCheck(input);
	}
	
	@Test
	public void testvalidDeclarations2() throws Exception {
		String input = "prog{int var1; float var2; boolean var3; image var4; "
				+ "filename var5; image var6[500,500];if(true){int var1; float var2; "
				+ "boolean var3; image var4; filename var5; image var6[500,500];};}";
		 typeCheck(input);
	}
	
	@Test
	public void testvalidDeclarations3() throws Exception {
		String input = "prog{if(false){int var1; float var2; boolean var3; "
				+ "image var4; filename var5; image var6[500,500];};"
				+ "if(true){int var1; float var2; boolean var3; image var4; filename var5; "
				+ "image var6[500,500];};}";
		 typeCheck(input);
	}
	
	@Test
	public void testinvalidStatementSleep1() throws Exception {
		String input = "prog{sleep 1.0;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void testvalidUnary1() throws Exception {
		String input = "prog{ int a; float b; boolean c;a := +a; a := -a; a := !a; "
				+ "b := +b; b := -b; b := !b; c := +c; c := -c; c := !c;}";
		typeCheck(input);
	}
	
	@Test
	public void testvalidUnary2() throws Exception {
		String input = "prog{ filename a; image b;a := +a; a := -a; a := !a; "
				+ "b := +b; b := -b; b := !b;}";
		typeCheck(input);
	}
	
	
	
	@Test
	public void testvalidStatementSleep1() throws Exception {
		String input = "prog{sleep 1;}";
		 typeCheck(input);}
	
	
	
	@Test
	public void testvalidStatementAssign1() throws Exception {
		String input = "prog{int var1; var1 := 1; float var2; var2 := 1.0;"
				+ "boolean var3; var3 := true;filename f1; filename f2; f1 := f2;image var4; "
				+ "image var5[500,500]; var4 := var5;}";
		typeCheck(input);
	}
	
	@Test
	public void testvalidStatementAssign2() throws Exception {
		String input = "prog{image var; var[0,0] := 1; alpha(var[0,0]) := 1; "
				+ "red(var[0,0]) := 1; green(var[0,0]) := 1; blue(var[0,0]) := 1;}";
		typeCheck(input);
	}
// */	
	
	   @Test
	    public void testvalidFunctionAppWithArg3() throws Exception {
	        String input = "prog {int var1; float var2; image var3;var1 := width(var3); var1 := height(var3); var2 := float(1); var1 := int(1.0);}";
	        typeCheck(input);
	    }
	    
	    @Test
	    public void testinvalidExpressionBinary1() throws Exception {
	        String input = "prog{ show (1.0 % 2.0);}";
	        thrown.expect(SemanticException.class);
	        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
	    }
	    
	    @Test
	    public void testinvalidExpressionBinary3() throws Exception {
	        String input = "prog{ show (1.0 % 2);}";
	        thrown.expect(SemanticException.class);
	        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
	    }
	    
	    @Test
	    public void testinvalidExpressionBinary6() throws Exception {
	        String input = "prog{ show (1 % 2.0);}";
	        thrown.expect(SemanticException.class);
	        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
	    }
	    
	    @Test
	    public void testinvalidExpressionBinary7() throws Exception {
	        String input = "prog{ show (1 & 2.0); show (1 | 2.0);}";
	        thrown.expect(SemanticException.class);
	        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
	    }
	   
	   
	    @Test
	    public void testinvalidDeclarations3() throws Exception {
	        String input = "prog{image var1[500, 1.0];}";
	        thrown.expect(SemanticException.class);
	        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
	    }
		

	    @Test
	    public void testinvalidExpressionPixel1() throws Exception {
	        String input = "prog{ int var2; var2 := var1[0,0];}";
	        thrown.expect(SemanticException.class);
	        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
	    }
		

	    @Test
	    public void testinvalidExpressionPixel2() throws Exception {
	        String input = "prog{ int var1; int var2; var2 := var1[0,0];}";
	        thrown.expect(SemanticException.class);
	        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
	    }
		

	    @Test
	    public void testinvalidLhsSample1() throws Exception {
	        String input = "prog{red (var1[0,0]) := 5;}";
	        thrown.expect(SemanticException.class);
	        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
	    }
		

	    @Test
	    public void testinvalidLhsSample2() throws Exception {
	        String input = "prog{int var1; red( var1[0,0]) := 5;}";
	        thrown.expect(SemanticException.class);
	        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
	    }
		

	    @Test
	    public void testvalidStatementIf1() throws Exception {
	        String input = "prog{boolean a; boolean b; if(a & b){};}";
	        typeCheck(input);
	        }
		

	    @Test
	    public void testinvalidLhsPixel1() throws Exception {
	        String input = "prog{var1[0,0] := 5;}";
	        thrown.expect(SemanticException.class);
	        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
	    }
		

	    @Test
	    public void testinvalidLhsPixel2() throws Exception {
	        String input = "prog{int var1; var1[0,0] := 5;}";
	        thrown.expect(SemanticException.class);
	        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
	    }
		

	    @Test
	    public void testvalidExpressionConditional1() throws Exception {
	        String input = "prog{boolean cond;int var1; var1 := cond ? var1+ 1 : var1;float var2; var2 := cond ? var2 + 1.0 : var2;}";
	        typeCheck(input);
	    }
		


	
		

	    @Test
	    public void testinvalidPixelSelector1() throws Exception {
	        String input = "prog{image var1; red( var1[0,0.0]) := 5;}";
	        thrown.expect(SemanticException.class);
	        try { 
	        	typeCheck(input); 
	        	} 
	        catch (SemanticException e) { show(e); throw e; }	    }
		

	    @Test
	    public void testinvalidPixelSelector2() throws Exception {
	        String input = "prog{image var1; red( var1[0.0,0]) := 5;}";
	        thrown.expect(SemanticException.class);
	        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
	    }
		

	    @Test
	    public void testinvalidPixelSelector3() throws Exception {
	        String input = "prog{image var1; red( var1[true,false]) := 5;}";
	        thrown.expect(SemanticException.class);
	        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
//	        typeCheck(input);
	    }
		

		

	    @Test
	    public void testScope2() throws Exception {
	        String input = "p{int var; if(true) {float var; var := 5.0;}; var := 5.0;}";
	        thrown.expect(SemanticException.class);
	        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
	    }
		

	    @Test
	    public void testScope4() throws Exception {
	        String input = "p{int var; if(true) {float var; var := 5;}; var := 5.0;}";
	        thrown.expect(SemanticException.class);
	        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
	    }
		

	    @Test
	    public void testScope3() throws Exception {
	        String input = "p{int var; if(true) {float var; var := 5;}; var := 5;}";
	        thrown.expect(SemanticException.class);
	        try { typeCheck(input); } catch (SemanticException e) { show(e); throw e; }
	    }
		
		@Test
		public void testvalidFunctionAppWithArg1() throws Exception {
			String input = "prog {int var1; var1 := abs(1); var1 := red(1); var1 := green(1); "
					+ "var1 := blue(1); var1 := int(1); var1 := alpha(1);}";
			 typeCheck(input);
		}
		
		
		

}
