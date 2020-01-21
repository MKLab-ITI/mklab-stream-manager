package gr.iti.mklab.framework.utils.booleanparse.ast;


import gr.iti.mklab.framework.utils.booleanparse.ast.visitor.BooleanQueryASTVisitor;

public abstract class Node {

	public abstract Object accept(BooleanQueryASTVisitor visitor);
	
	public abstract String toString();

}