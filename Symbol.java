package cop5556sp18;

import cop5556sp18.AST.Declaration;

public class Symbol {
		public int scopeId;
		public Declaration dec;
		
		public Symbol (Declaration dec) {
			this.dec = dec;
		}
		
		public int getScopeId() {
			return scopeId;
		}
		public void setScopeId(int varscope) {
			this.scopeId = varscope;
		}
		
		public Declaration getDeclaration() {
			return dec;
		}
		
		public void setDeclaration(Declaration dec) {
			this.dec = dec;
		}
	}

