package gr.iti.mklab.framework.utils.booleanparse.ast;

import java.util.List;

import gr.iti.mklab.framework.utils.booleanparse.ast.BooleanExpression;
import gr.iti.mklab.framework.utils.booleanparse.ast.Node;
import gr.iti.mklab.framework.utils.booleanparse.ast.visitor.BooleanQueryASTVisitor;

public class AndNode extends BooleanExpression {

	public AndNode(List<Node> operands) {
		super();
		setOperands(operands);
	}

	@Override
	public Object accept(BooleanQueryASTVisitor visitor) {
		return visitor.visitAnd(this);
	}

	@Override
	public String toString() {
		return "and";
	}
}