package cop5556sp18;


import java.util.HashMap;
import java.util.ArrayList;
import java.util.Stack;
import cop5556sp18.Symbol;

import cop5556sp18.AST.Declaration;

public class SymbolTable {
	int current, next;
	HashMap<String, ArrayList<Symbol>> record = new HashMap<String, ArrayList<Symbol>>();
	Stack<Integer> scopes = new Stack<Integer>();
	Declaration dec;
	
	public SymbolTable() {
		this.current = 0;
		this.next = 0;
		scopes.push(0);
	}
	
	public void enterScope() {
		current = ++next;
		scopes.push(current);
	}


	public void exitScope() {
		scopes.pop();
		current = scopes.peek();
	}

	public void add(String ident, Symbol symbol) {
		symbol.setScopeId(current);
		if(!(record.containsKey(ident))) {
			record.put(ident, new ArrayList<Symbol>());
		}
		record.get(ident).add(symbol);
	}

	public boolean duplicateCheck (Declaration dec) {
		if(record.containsKey(dec.name)) {
			ArrayList<Symbol> data = record.get(dec.name);
			for(Symbol s : data) {
				if(s.scopeId == current) {
					return true;
				}
			}
		}
		return false;
	}
	
	public Declaration lookup(String ident) {
		if (!record.containsKey(ident)) {
			return null;
		}
		ArrayList<Symbol> data = record.get(ident);
		int size = scopes.size();
		if(!data.isEmpty()) {
			for (int i = size-1; i >= 0; i--) {
				for (Symbol sym: data)  {
					if (scopes.get(i) == sym.getScopeId()) {
						return sym.getDeclaration();
					}
				}
			}
		}
		return null;	
	}
}


