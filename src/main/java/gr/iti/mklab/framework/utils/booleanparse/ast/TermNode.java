package gr.iti.mklab.framework.utils.booleanparse.ast;

import gr.iti.mklab.framework.utils.booleanparse.ast.BooleanExpression;
import gr.iti.mklab.framework.utils.booleanparse.ast.visitor.BooleanQueryASTVisitor;

public class TermNode extends BooleanExpression {

	private String value;
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public TermNode(String value){
		super();
		setValue(value);
	}

	@Override
	public Object accept(BooleanQueryASTVisitor visitor) {
		return null;
	}

	@Override
	public String toString() {
		return value;
	}
	
}