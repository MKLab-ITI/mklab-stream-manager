package gr.iti.mklab.framework.utils.booleanparse.ast.visitor;

import java.util.ArrayList;
import java.util.List;

import gr.iti.mklab.framework.utils.booleanparse.BooleanQueryVisitor;
import gr.iti.mklab.framework.utils.booleanparse.BooleanQueryParser.AndContext;
import gr.iti.mklab.framework.utils.booleanparse.BooleanQueryParser.AtomContext;
import gr.iti.mklab.framework.utils.booleanparse.BooleanQueryParser.ExpressionContext;
import gr.iti.mklab.framework.utils.booleanparse.BooleanQueryParser.NotContext;
import gr.iti.mklab.framework.utils.booleanparse.BooleanQueryParser.OrContext;
import gr.iti.mklab.framework.utils.booleanparse.ast.AndNode;
import gr.iti.mklab.framework.utils.booleanparse.ast.AtomNode;
import gr.iti.mklab.framework.utils.booleanparse.ast.Node;
import gr.iti.mklab.framework.utils.booleanparse.ast.NotNode;
import gr.iti.mklab.framework.utils.booleanparse.ast.OrNode;
import gr.iti.mklab.framework.utils.booleanparse.ast.TermNode;

import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

public class ASTBuilderVisitor implements BooleanQueryVisitor<Node> {

	public static final String AND = "AND";
	public static final String OR = "OR";
	public static final String NOT = "NOT";
	
	@Override
	public Node visit(ParseTree tree) {
		return tree.accept(this);
	}
	@Override
	public Node visitChildren(RuleNode node) {
		return null;
	}
	@Override
	public Node visitTerminal(TerminalNode node) {
		String text = node.getText();
		if(text == null || text.equals("(") || text.equals(")") || text.startsWith("!")) {
			return null;
		}
		return new TermNode(node.getText());
	}
	@Override
	public Node visitErrorNode(ErrorNode node) {
		return null;
	}
	@Override
	public Node visitExpression(ExpressionContext ctx) {
		return ctx.not().accept(this);
	}
	@Override
	public Node visitNot(NotContext ctx) {
		if(ctx.getChildCount() <= 1) {
			ParseTree child = ctx.getChild(0);
			Node childOperand = child.accept(this);
			return childOperand;
		}
		
		List<Node> operands = new ArrayList<Node>();
		for(int i=0; i < ctx.getChildCount(); i++) {
			ParseTree child = ctx.getChild(i);
			String operand = child.getText();
			if(!operand.equals(NOT)) {
				Node childOperand = child.accept(this);
				operands.add(childOperand);
			}
		}
		return new NotNode(operands);
	}
	@Override
	public Node visitAnd(AndContext ctx) {
		if(ctx.getChildCount() <= 1) {
			ParseTree child = ctx.getChild(0);
			Node childOperand = child.accept(this);
			return childOperand;
		}
		else {
			List<Node> operands = new ArrayList<Node>();
			for(int i=0; i < ctx.getChildCount(); i++) {
				ParseTree child = ctx.getChild(i);
				String operand = child.getText();
				if(!operand.equals(AND)) {
					Node childOperand = child.accept(this);
					if(childOperand != null) {
						operands.add(childOperand);
					}
				}
			}
			if(operands.size() == 1) {
				return operands.get(0);
			}
			return new AndNode(operands);
		}
	}
	@Override
	public Node visitOr(OrContext ctx) {
		if(ctx.getChildCount() <= 1) {
			ParseTree child = ctx.getChild(0);
			Node childOperand = child.accept(this);
			return childOperand;
		}
		else {
			List<Node> operands = new ArrayList<Node>();
			for(int i=0; i < ctx.getChildCount(); i++) {
				ParseTree child = ctx.getChild(i);
				String operand = child.getText();
				if(!operand.equals(OR)) {
					Node childOperand = child.accept(this);
					if(childOperand != null) {
						operands.add(childOperand);
					}
				}
			}
			
			if(operands.size() == 1) {
				return operands.get(0);
			}
			return new OrNode(operands);
		}
	}
	@Override
	public Node visitAtom(AtomContext ctx) {
		if(ctx.getChildCount() <= 1) {
			ParseTree child = ctx.getChild(0);
			Node childOperand = child.accept(this);
			return childOperand;
		}
		
		List<Node> operands = new ArrayList<Node>();
		for(int i=0; i < ctx.getChildCount(); i++) {
			ParseTree child = ctx.getChild(i);
			Node childOperand = child.accept(this);
			if(childOperand != null) {
				operands.add(childOperand);
			}
		}
		if(operands.size() == 1) {
			return operands.get(0);
		}
		
		return new AtomNode(operands);
	}


}