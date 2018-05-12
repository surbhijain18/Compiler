package cop5556sp18;

import org.objectweb.asm.Label;

import cop5556sp18.AST.Declaration;

public class LocalVarsVisit {
	
	public static String className = "cop5556sp18/LocalVarsVisit";
	
	public Declaration dec;
	public Label begin = new Label();
	public Label end = new Label();
	
	public LocalVarsVisit(Declaration d, Label l1, Label l2) {
		this.dec = d;
		this.begin = l1;
		this.end = l2;
	}	
	
}
