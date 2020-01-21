package gr.iti.mklab.framework.utils.booleanparse.ast;

import gr.iti.mklab.framework.utils.booleanparse.ast.Node;

import java.util.List;

public abstract class BooleanExpression extends Node {

	public List<Node> operands;

	public List<Node> getOperands() {
		return operands;
	}
	public void setOperands(List<Node> operands) {
		this.operands = operands;
	}
	
}