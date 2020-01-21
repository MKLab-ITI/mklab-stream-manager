package gr.iti.mklab.framework.utils.booleanparse;

import gr.iti.mklab.framework.utils.booleanparse.BooleanQueryParser;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link BooleanQueryParser}.
 */
public interface BooleanQueryListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link BooleanQueryParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(BooleanQueryParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BooleanQueryParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(BooleanQueryParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BooleanQueryParser#not}.
	 * @param ctx the parse tree
	 */
	void enterNot(BooleanQueryParser.NotContext ctx);
	/**
	 * Exit a parse tree produced by {@link BooleanQueryParser#not}.
	 * @param ctx the parse tree
	 */
	void exitNot(BooleanQueryParser.NotContext ctx);
	/**
	 * Enter a parse tree produced by {@link BooleanQueryParser#and}.
	 * @param ctx the parse tree
	 */
	void enterAnd(BooleanQueryParser.AndContext ctx);
	/**
	 * Exit a parse tree produced by {@link BooleanQueryParser#and}.
	 * @param ctx the parse tree
	 */
	void exitAnd(BooleanQueryParser.AndContext ctx);
	/**
	 * Enter a parse tree produced by {@link BooleanQueryParser#or}.
	 * @param ctx the parse tree
	 */
	void enterOr(BooleanQueryParser.OrContext ctx);
	/**
	 * Exit a parse tree produced by {@link BooleanQueryParser#or}.
	 * @param ctx the parse tree
	 */
	void exitOr(BooleanQueryParser.OrContext ctx);
	/**
	 * Enter a parse tree produced by {@link BooleanQueryParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterAtom(BooleanQueryParser.AtomContext ctx);
	/**
	 * Exit a parse tree produced by {@link BooleanQueryParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitAtom(BooleanQueryParser.AtomContext ctx);
}