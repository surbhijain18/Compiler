package cop5556sp18;

import java.util.List;
import java.util.ArrayList;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556sp18.RuntimeFunctions;
import cop5556sp18.RuntimeImageSupport;
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

import cop5556sp18.CodeGenUtils;
import cop5556sp18.Scanner.Kind;

public class CodeGenerator implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */

	static final int Z = 255;

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	final int defaultWidth;
	final int defaultHeight;
	static int slotNumber = 1;
	List <LocalVarsVisit> decs = new ArrayList<LocalVarsVisit>();

	
	// final boolean itf = false;
	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 * @param defaultWidth
	 *            default width of images
	 * @param defaultHeight
	 *            default height of images
	 */
	public CodeGenerator(boolean DEVEL, boolean GRADE, String sourceFileName,
			int defaultWidth, int defaultHeight) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		// cw = new ClassWriter(0); //If the call to mv.visitMaxs(1, 1) crashes,
		// it is
		// sometime helpful to
		// temporarily run it without COMPUTE_FRAMES. You probably
		// won't get a completely correct classfile, but
		// you will be able to see the code that was
		// generated.
		className = program.progName;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null,"java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
			// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();

				// add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);

		CodeGenUtils.genLog(DEVEL, mv, "entering main");

		program.block.visit(this, arg);

				// generates code to add string to log
		CodeGenUtils.genLog(DEVEL, mv, "leaving main");
			// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

				// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart,
						mainEnd, 0);
		int counter = 0;
					//System.out.print("size: " + decs.size());
		while(decs.size() > counter)
		{	
			String name = decs.get(counter).dec.name;
			Label a = decs.get(counter).begin;
			Label z = decs.get(counter).end;
			int slot = decs.get(counter).dec.getSlot();
			Type decType = Types.getType(decs.get(counter).dec.type);
			//		System.out.println(decType);
			if (decType == Type.BOOLEAN )
					mv.visitLocalVariable(name, "Z", null, a, z, slot);
			else if (decType == Type.INTEGER)
					mv.visitLocalVariable(name, "I", null, a, z, slot);
			else if (decType == Type.FLOAT)
					mv.visitLocalVariable(name, "F", null, a, z, slot);
			else if (decType == Type.IMAGE)
					mv.visitLocalVariable(name, RuntimeImageSupport.ImageDesc, null, a, z, slot);
			else if (decType == Type.FILE)
					mv.visitLocalVariable(name, "Ljava/lang/String;", null, a, z, slot);
			counter++;
						}
				// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the
				// constructor,
				// asm will calculate this itself and the parameters are ignored.
				// If you have trouble with failures in this routine, it may be useful
				// to temporarily change the parameter in the ClassWriter constructor
				// from COMPUTE_FRAMES to 0.
				// The generated classfile will not be correct, but you will at least be
				// able to see what is in it.
		mv.visitMaxs(0, 0);
		// terminate construction of main method
		mv.visitEnd();

				// terminate class construction
		cw.visitEnd();

				// generate classfile as byte array and return
		return cw.toByteArray();
	}
	
	
	
	
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception 
	{
		Label start_block = new Label();
		Label end_block = new Label();

		mv.visitLabel(start_block);
		
		for (ASTNode node : block.decsOrStatements) {
			node.visit(this, null);
		}
		
		mv.visitLabel(end_block);
		
		for (ASTNode node : block.decsOrStatements) {
			if (node instanceof Declaration)
			{
				LocalVarsVisit obj = new LocalVarsVisit((Declaration)node,start_block,end_block);
				decs.add(obj);
			}
		}
		
		return null;
	}

	
	@Override
	public Object visitDeclaration(Declaration declaration, Object arg)
			throws Exception {
		// TODO Auto-generated method stub

		Expression width = declaration.width;
		Expression height = declaration.height;
		declaration.setSlot(slotNumber++);
		//slotNumber++;
		Type dectype = Types.getType(declaration.type);
		if (dectype == Type.IMAGE)
		{
			if (width != null && height != null)
			{
				width.visit(this, arg);
				height.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "makeImage", RuntimeImageSupport.makeImageSig, false);
			}
			
			else if (width == null && height == null)
			{
				
				mv.visitLdcInsn(defaultWidth);
				mv.visitLdcInsn(defaultHeight);
				mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "makeImage", RuntimeImageSupport.makeImageSig, false);
			}
		//	mv.visitFieldInsn(PUTFIELD, className, declaration.name, "Ljava/awt/image/BufferedImage");
			
			mv.visitVarInsn(ASTORE, declaration.getSlot());
		}
				
		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		
		Declaration dec = statementInput.dec;
		
		
		mv.visitVarInsn(ALOAD, 0);
		statementInput.e.visit(this, arg);
		mv.visitInsn(AALOAD);

		if (Types.getType(dec.type) == Type.IMAGE)
		{
			Expression wd = dec.width;
			Expression ht = dec.height;
			if (wd == null && ht == null)
			{
				mv.visitInsn(ACONST_NULL); //pushes null
				mv.visitInsn(ACONST_NULL);
			}
			else
			{
				wd.visit(this, null);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;",false);
				ht.visit(this, null);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;",false);
			}
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className,"readImage", RuntimeImageSupport.readImageSig,false);
			mv.visitVarInsn(ASTORE, dec.getSlot());
		}
		
		else if (Types.getType(dec.type) == Type.INTEGER)
		{
			
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitVarInsn(ISTORE, dec.getSlot());
		}
		
		else if (Types.getType(dec.type) == Type.FLOAT)
		{
			
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "parseFloat", "(Ljava/lang/String;)F", false);
			mv.visitVarInsn(FSTORE, dec.getSlot());
		}
		else if (Types.getType(dec.type) == Type.BOOLEAN)
		{
			
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitVarInsn(ISTORE, dec.getSlot());
		}
		else if (Types.getType(dec.type) == Type.FILE)
		{
			mv.visitVarInsn(ASTORE, dec.getSlot());
		}
		
	return null;
	}


	@Override
	public Object visitStatementAssign(StatementAssign statementAssign,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression ex = statementAssign.e;
		LHS lhs = statementAssign.lhs;
		ex.visit(this,arg);
		lhs.visit(this,arg);
		return null;
	}
	
	
	
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg)
			throws Exception {
		/**
		 * TODO refactor and complete implementation.
		 * 
		 * For integers, booleans, and floats, generate code to print to
		 * console. For images, generate code to display in a frame.
		 * 
		 * In all cases, invoke CodeGenUtils.genLogTOS(GRADE, mv, type); before
		 * consuming top of stack.
		 */
		statementShow.e.visit(this, arg);
		Type type = statementShow.e.type;
		switch (type) {
			case INTEGER : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(I)V", false);
			}
				break;
			case BOOLEAN : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out","Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(Z)V", false);
			}
			break;
			case FLOAT : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out","Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(F)V", false);
			}
//			case FILE : {
//				CodeGenUtils.genLogTOS(GRADE, mv, type);
//				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out","Ljava/io/PrintStream;");
//				mv.visitInsn(Opcodes.SWAP);
//				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
//						"println", "()V", false);
//			}
			
			break; 
			case IMAGE : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "makeFrame", RuntimeImageSupport.makeFrameSig, false);

			}

		}
		return null;
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg)
			throws Exception {
		Expression duration = statementSleep.duration;
		if (duration!=null)
			duration.visit(this, null);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
}

	
	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg)
			throws Exception {
		Type type = lhsIdent.type;
	
		
		if(type == Type.INTEGER ||type == Type.BOOLEAN ) {
			mv.visitVarInsn(ISTORE, lhsIdent.dec.getSlot());
		}
		else if (type == Type.FLOAT)
		{
			mv.visitVarInsn(FSTORE, lhsIdent.dec.getSlot());
		}
		else if (type == Type.FILE) {
			mv.visitVarInsn(ASTORE, lhsIdent.dec.getSlot());
		
		}else if (type == Type.IMAGE)
		{	
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "deepCopy", RuntimeImageSupport.deepCopySig,false);
			mv.visitVarInsn(ASTORE, lhsIdent.dec.getSlot());

		}

		
		return null;
	}


	
	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression left=expressionBinary.leftExpression;
		Expression right=expressionBinary.rightExpression;
		Kind op = expressionBinary.op;
		
		Label start = new Label();
		
		if(op == Kind.OP_OR)
		{	
			right.visit(this, arg);
			left.visit(this, arg);
			if (left.type==Type.BOOLEAN || left.type==Type.INTEGER)
				mv.visitInsn(IOR);
		}
		
		else if(op == Kind.OP_AND)
		{
			right.visit(this, arg);
			left.visit(this, arg);
			if(left.type==Type.BOOLEAN||left.type==Type.INTEGER)
				mv.visitInsn(IAND);
		}
		
		else if(op == Kind.OP_PLUS)
		{
			left.visit(this, arg);
			right.visit(this, arg);
			if (left.type == Type.INTEGER && right.type == Type.INTEGER)
					mv.visitInsn(IADD);
				
			else if (left.type == Type.INTEGER)	
			{	
				mv.visitInsn(SWAP);
				mv.visitInsn(I2F);
				//mv.visitInsn(SWAP);
				if (right.type == Type.FLOAT)
					mv.visitInsn(FADD);
			}
			else if (left.type == Type.FLOAT && right.type == Type.FLOAT)
					mv.visitInsn(FADD);
					
			else if (left.type == Type.FLOAT)	
			{
				if (right.type == Type.INTEGER)
				{
					mv.visitInsn(I2F);
					mv.visitInsn(FADD);
				}
			}
			
		}
		
		else if(op == Kind.OP_MINUS)
		{	
			left.visit(this, arg);
			right.visit(this, arg);
			if (left.type == Type.INTEGER && right.type == Type.INTEGER)
					mv.visitInsn(ISUB);
				
			else if (left.type == Type.INTEGER)	
			{
				mv.visitInsn(SWAP);
				mv.visitInsn(I2F);
				mv.visitInsn(SWAP);
				if (right.type == Type.FLOAT)
					mv.visitInsn(FSUB);
			}
			else if (left.type == Type.FLOAT && right.type == Type.FLOAT)
					mv.visitInsn(FSUB);
					
			else if (left.type == Type.FLOAT)	
			{
				if (right.type == Type.INTEGER)
				{
					mv.visitInsn(I2F);
				//	mv.visitInsn(I2F);
					mv.visitInsn(FSUB);
				}
			}
			
		}
		
		else if(op == Kind.OP_DIV)
		{	
			left.visit(this, arg);
			right.visit(this, arg);
			
			if (left.type == Type.INTEGER && right.type == Type.INTEGER)
					mv.visitInsn(IDIV);
				
			else if (left.type == Type.INTEGER)	
			{	mv.visitInsn(SWAP);
				mv.visitInsn(I2F);
				mv.visitInsn(SWAP);
				if (right.type == Type.FLOAT)
					mv.visitInsn(FDIV);
			}
			else if (left.type == Type.FLOAT && right.type == Type.FLOAT)
					mv.visitInsn(FDIV);
					
			else if (left.type == Type.FLOAT)	
			{
				if (right.type == Type.INTEGER)
				{
					//mv.visitInsn(SWAP);
					mv.visitInsn(I2F);
					//mv.visitInsn(SWAP);
					//mv.visitInsn(I2F);
					mv.visitInsn(FDIV);
				}
			}
			
		}
		
		
		else if(op == Kind.OP_TIMES)
		{	
			right.visit(this, arg);
			left.visit(this, arg);
			if (left.type == Type.INTEGER && right.type == Type.INTEGER)
					mv.visitInsn(IMUL);
				
			else if (left.type == Type.INTEGER)	
			{
				mv.visitInsn(I2F);
				if (right.type == Type.FLOAT)
					mv.visitInsn(FMUL);
			}
			else if (left.type == Type.FLOAT && right.type == Type.FLOAT)
					mv.visitInsn(FMUL);
					
			else if (left.type == Type.FLOAT)	
			{
				if (right.type == Type.INTEGER)
				{	
					mv.visitInsn(SWAP);
					mv.visitInsn(I2F);
					mv.visitInsn(SWAP);
				//	mv.visitInsn(I2F);
					mv.visitInsn(FMUL);
				}
			}
			
		}
		
		
		else if(op == Kind.OP_POWER)
		{	
		//	mv.visitInsn(POP);
			//mv.visitInsn(POP);
			if (left.type == Type.INTEGER && right.type == Type.INTEGER)
			{
				left.visit(this, arg);
				mv.visitInsn(I2D);
				right.visit(this, arg);
				mv.visitInsn(I2D);
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "power", RuntimeFunctions.power, false);
				mv.visitInsn(D2I);
			}
				
			else if (left.type == Type.INTEGER && right.type == Type.FLOAT)
			{ 
				left.visit(this, arg);
				mv.visitInsn(I2D);
				right.visit(this, arg);
				mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "power", RuntimeFunctions.power, false);
				mv.visitInsn(D2F);
				
			}
			else if (left.type == Type.FLOAT && right.type == Type.FLOAT)
			{
				
				left.visit(this, arg);
				mv.visitInsn(F2D);
				right.visit(this, arg);
				mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "power", RuntimeFunctions.power, false);
				mv.visitInsn(D2F);
					
			}
			else if(left.type == Type.FLOAT && right.type == Type.INTEGER) 
				{	
					left.visit(this, arg);
					mv.visitInsn(F2D);
					right.visit(this, arg);
					mv.visitInsn(I2D);
					mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "power", RuntimeFunctions.power, false);
					mv.visitInsn(D2F);
					
				}
			}
		
		else if (expressionBinary.op==Kind.OP_MOD)
		{
			
			left.visit(this, arg);
			right.visit(this,arg);
			if (left.type == Type.INTEGER && right.type == Type.INTEGER)
			{ 	
				mv.visitInsn(IREM);
			}
		}
		else if (expressionBinary.op==Kind.OP_NEQ )//|| expressionBinary.op==Kind.OP_EQ || expressionBinary.op==Kind.OP_GT ||expressionBinary.op==Kind.OP_GE || expressionBinary.op==Kind.OP_LT || expressionBinary.op==Kind.OP_LE)
		{	right.visit(this, arg);
			left.visit(this, arg);
			if (left.type == right.type){
				if (left.type == Type.INTEGER ||  left.type == Type.BOOLEAN)
					mv.visitJumpInsn(IF_ICMPNE, start);
						
				else if(left.type == Type.FLOAT) {
					mv.visitInsn(FCMPL);
					mv.visitJumpInsn(IFNE, start);
				}
				
				mv.visitLdcInsn(false);
				
			}
		}
		
		else if (expressionBinary.op==Kind.OP_EQ )//|| expressionBinary.op==Kind.OP_EQ || expressionBinary.op==Kind.OP_GT ||expressionBinary.op==Kind.OP_GE || expressionBinary.op==Kind.OP_LT || expressionBinary.op==Kind.OP_LE)
		{
			right.visit(this, arg);
			left.visit(this, arg);
			if (left.type == right.type)
			{
				
				if (left.type == Type.INTEGER || left.type == Type.BOOLEAN)
					mv.visitJumpInsn(IF_ICMPEQ, start);
					
				else if(left.type == Type.FLOAT) {
					mv.visitInsn(FCMPL);
					mv.visitJumpInsn(IFEQ,start);
			}
				mv.visitLdcInsn(false);
			}
		}
		else if(op == Kind.OP_LT)
		{	left.visit(this, arg);
			right.visit(this, arg);
			if (left.type == right.type) 
			{
				if(left.type==Type.INTEGER || left.type == Type.BOOLEAN) 
				{
					mv.visitJumpInsn(IF_ICMPLT, start);
					mv.visitLdcInsn(false);
				}
				else if(left.type == Type.FLOAT) 
				{
					mv.visitInsn(FCMPL);
					mv.visitJumpInsn(IFLT, start);
					mv.visitLdcInsn(false);

				}
					
			}
			
		}
		else if(op == Kind.OP_GT)
		{
			left.visit(this, arg);
			right.visit(this, arg);
			
			if (left.type == right.type) 
			{
				if(left.type==Type.INTEGER || left.type == Type.BOOLEAN) 
				{
					mv.visitJumpInsn(IF_ICMPGT, start);
					mv.visitLdcInsn(false);
				}
				else if(left.type == Type.FLOAT) 
				{
					mv.visitInsn(FCMPL);
					mv.visitJumpInsn(IFGT,start);
					mv.visitLdcInsn(false);
				}
					
			}
		}
		else if(op == Kind.OP_LE)
		{
			left.visit(this, arg);
			right.visit(this, arg);
			
			if (left.type == right.type) 
			{
				if(left.type==Type.INTEGER || left.type == Type.BOOLEAN) 
				{
					mv.visitJumpInsn(IF_ICMPLE, start);
					mv.visitLdcInsn(false);
				}
				else if(left.type == Type.FLOAT) 
				{
					mv.visitInsn(FCMPL);
					mv.visitJumpInsn(IFLE, start);
					mv.visitLdcInsn(false);
				}
					
			}
		}
		else if(op == Kind.OP_GE)
		{
			left.visit(this, arg);
			right.visit(this, arg);
			if (left.type == right.type) 
			{
				if(left.type==Type.INTEGER || left.type == Type.BOOLEAN) 
				{
					mv.visitJumpInsn(IF_ICMPGE, start);
					mv.visitLdcInsn(false);
				}
				else if(left.type == Type.FLOAT) 
				{
					mv.visitInsn(FCMPL);
					mv.visitJumpInsn(IFGE,start);
					mv.visitLdcInsn(false);
				}
					
			}
		}
		
		Label end=new Label();
		mv.visitJumpInsn(GOTO, end);
		mv.visitLabel(start);
		mv.visitLdcInsn(true);
		mv.visitLabel(end);
		return null;
	}

	

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary,
			Object arg) throws Exception {
		Expression ex = expressionUnary.expression;
		Kind op = expressionUnary.op;
		ex.visit(this, null);
		
		switch (op) {
		case OP_PLUS:
			if(ex.type == Type.INTEGER || ex.type == Type.FLOAT)
				break;
		case OP_MINUS:{
			if(ex.type == Type.INTEGER) 
				mv.visitInsn(INEG);
			else if (ex.type == Type.FLOAT)
				mv.visitInsn(FNEG);
			break;}
		case OP_EXCLAMATION:
			switch (ex.type) {
				case BOOLEAN:
					mv.visitInsn(ICONST_1);
					mv.visitInsn(IXOR);
					break;
				case INTEGER:
					mv.visitLdcInsn(-1);
					mv.visitInsn(IXOR);
					break;
				default:
					throw new UnsupportedOperationException();
			}
			break;
		default:
			throw new UnsupportedOperationException();
		}
		
		return null;
	}
	
	
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type type = expressionIdent.type;
		switch(type) 
		{
		case INTEGER:
			mv.visitVarInsn(ILOAD, expressionIdent.dec.getSlot());
			break;
		case BOOLEAN:
			mv.visitVarInsn(ILOAD, expressionIdent.dec.getSlot());
			break;
		case FLOAT:
			mv.visitVarInsn(FLOAD, expressionIdent.dec.getSlot());
			break;
		case IMAGE:
			mv.visitVarInsn(ALOAD, expressionIdent.dec.getSlot());
			break;
		case FILE:
			mv.visitVarInsn(ALOAD, expressionIdent.dec.getSlot());
			break;
//		default:
//			mv.visitVarInsn(ALOAD, expressionIdent.dec.getSlot());
//
//			break;
//		
		}
		return null;
	}

	

	@Override
	public Object visitBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg)throws Exception {
		mv.visitLdcInsn(expressionBooleanLiteral.value);
		return null;
	}
	
	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)throws Exception {
		mv.visitLdcInsn(expressionIntegerLiteral.value);
		return null;
	}
	
	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)throws Exception {
		mv.visitLdcInsn(expressionFloatLiteral.value);
		return null;
	}

	
	@SuppressWarnings("incomplete-switch")
	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e1 = expressionFunctionAppWithExpressionArg.e;
		Kind function=expressionFunctionAppWithExpressionArg.function;
		if(e1!=null)
			e1.visit(this,arg);
		switch (function) {
		case KW_log: 
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "log", RuntimeFunctions.logSig, false);
			mv.visitInsn(D2F);
			break;
		case KW_sin:
			mv.visitInsn(F2D); 
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "sin", RuntimeFunctions.sinSig, false);
			mv.visitInsn(D2F);
			break;
		case KW_cos: 
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cos", RuntimeFunctions.cosSig, false);
			mv.visitInsn(D2F);	
			break;
		case KW_abs:{
			if (e1.type == Type.FLOAT)
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "abs_ft", RuntimeFunctions.absSig_ft, false);
			else if (e1.type == Type.INTEGER)
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "abs_int", RuntimeFunctions.absSig_int, false);
			break;}
		case KW_atan: 
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "atan", RuntimeFunctions.atanSig, false);
			break;
		case KW_float:
			if(e1.type == Type.INTEGER)
				mv.visitInsn(I2F);			
			break;
		case KW_int:
			if(e1.type == Type.FLOAT)
				mv.visitInsn(F2I);
			break;
		case KW_alpha:
			mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getAlpha", RuntimePixelOps.getAlphaSig, false);
			break;
		case KW_red:
			mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getRed", RuntimePixelOps.getRedSig, false);
			break;
		case KW_blue:
			mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getBlue", RuntimePixelOps.getBlueSig, false);
			break;
		case KW_green:
			mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getGreen", RuntimePixelOps.getGreenSig, false);
			break;
		case KW_width:
			//mv.visitVarInsn(ALOAD, e1.dec.getSlot());
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getWidth", RuntimeImageSupport.getWidthSig, false);
			break;
		case KW_height:
		//	mv.visitVarInsn(ALOAD, e1.dec.getSlot());
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getHeight", RuntimeImageSupport.getHeightSig, false);
			break;
			
		}
		return null;
	}

	
	@Override
	public Object visitExpressionPredefinedName(
			ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Kind kind=expressionPredefinedName.name;
		if (kind == Kind.KW_Z)
			mv.visitLdcInsn(Z);
		else if (kind == Kind.KW_default_height) {
			mv.visitLdcInsn(defaultHeight);
		}else if (kind == Kind.KW_default_width){	
			mv.visitLdcInsn(defaultWidth);
		}
		
		return null;	
}

	
	
	
	
	
	/*********************HW6******************************/
	@SuppressWarnings("incomplete-switch")
	@Override
	public Object visitExpressionFunctionAppWithPixel(
			ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		//hw6
		Expression ex1 = expressionFunctionAppWithPixel.e0;
		Expression ex2 = expressionFunctionAppWithPixel.e1;
		Kind kind = expressionFunctionAppWithPixel.name;
		
		switch (kind) {
		case KW_cart_x : {
			ex1.visit(this, arg);
			mv.visitInsn(F2D);
			ex2.visit(this, arg);
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
//			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
//			mv.visitInsn(DMUL);
			mv.visitInsn(D2I);
			break;
		}
		case KW_cart_y :{
			ex1.visit(this, arg);
			mv.visitInsn(F2D);
			ex2.visit(this, arg);
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);
//			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
//			mv.visitInsn(DMUL);
			mv.visitInsn(D2I);
			break;
		}
		
		case KW_polar_r:{
			ex1.visit(this, arg);
			mv.visitInsn(I2D);
			ex2.visit(this, arg);
			mv.visitInsn(I2D);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
			mv.visitInsn(D2F);
			break;

		}
		case KW_polar_a:{
			ex1.visit(this, arg);
			mv.visitInsn(I2D);
			ex2.visit(this, arg);
			mv.visitInsn(I2D);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
			mv.visitInsn(D2F);
			break;

			}
		}
		return null;
	}
	
	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		// hw6
		
		
		mv.visitVarInsn(ALOAD, lhsPixel.dec.getSlot());
		lhsPixel.pixelSelector.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "setPixel", RuntimeImageSupport.setPixelSig, false);

		return null;
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		// hw 6
		PixelSelector pixel = lhsSample.pixelSelector;
		Kind color = lhsSample.color;
		
		mv.visitVarInsn(ALOAD, lhsSample.dec.getSlot());
		pixel.visit(this, arg);
		
		if (color == Kind.KW_red)
			mv.visitFieldInsn(GETSTATIC, RuntimePixelOps.className, "RED", "I");
		if (color == Kind.KW_blue)
			mv.visitFieldInsn(GETSTATIC, RuntimePixelOps.className, "BLUE", "I");
		if (color == Kind.KW_green)
			mv.visitFieldInsn(GETSTATIC, RuntimePixelOps.className, "GREEN", "I");
		if (color == Kind.KW_alpha)
			mv.visitFieldInsn(GETSTATIC, RuntimePixelOps.className, "ALPHA", "I");
		
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "updatePixelColor", RuntimeImageSupport.updatePixelColorSig, false);

		return null;
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//hw6
		Expression e1 = pixelSelector.ex;
		Expression e2 = pixelSelector.ey;
		e1.visit(this, arg);
		e2.visit(this, arg);
		return null;
	}

	
	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		//hw6		
		mv.visitVarInsn(ALOAD, expressionPixel.dec.getSlot());
		expressionPixel.pixelSelector.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getPixel", RuntimeImageSupport.getPixelSig,false);
		return null;
}

	@Override
	public Object visitExpressionPixelConstructor(
			ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//hw6
		expressionPixelConstructor.alpha.visit(this, arg);
		expressionPixelConstructor.red.visit(this, arg);
		expressionPixelConstructor.green.visit(this, arg);
		expressionPixelConstructor.blue.visit(this, arg);
//		expressionPixelConstructor.blue.visit(this, arg);
//		expressionPixelConstructor.green.visit(this, arg);
//		expressionPixelConstructor.red.visit(this, arg);
//		expressionPixelConstructor.alpha.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "makePixel", RuntimePixelOps.makePixelSig,false);

		return null;	
}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//hw6
		statementIf.guard.visit(this,arg);
		Label next = new Label();
		mv.visitJumpInsn(IFEQ, next);
		statementIf.b.visit(this, arg);
		mv.visitLabel(next);
		return null;
	}

	
	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//hw6
		Label Guard = new Label();
		Label block = new Label();
		mv.visitJumpInsn(GOTO, Guard);
		mv.visitLabel(block);
		statementWhile.b.visit(this, arg);
		mv.visitLabel(Guard);
		statementWhile.guard.visit(this, arg);
		mv.visitJumpInsn(IFNE, block);
		return null;		
	
	}
	
	@Override
	public Object visitExpressionConditional(
			ExpressionConditional expressionConditional, Object arg)
			throws Exception {
		
		Label false_label = new Label();
		Label block = new Label();
		expressionConditional.guard.visit(this, arg);
		
		mv.visitJumpInsn(IFEQ, false_label);
		expressionConditional.trueExpression.visit(this, arg);
		
		mv.visitJumpInsn(GOTO, block);
		
		mv.visitLabel(false_label);
		expressionConditional.falseExpression.visit(this, arg);
		mv.visitLabel(block);
		return null;
	}
	

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//hw6
		mv.visitVarInsn(ALOAD, statementWrite.sourceDec.getSlot());
		mv.visitVarInsn(ALOAD, statementWrite.destDec.getSlot());
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "write", RuntimeImageSupport.writeSig, false);
	  		
		return null;	
}

}
