package gr.iti.mklab.framework.utils.booleanparse;

import gr.iti.mklab.framework.utils.booleanparse.BooleanQueryParser;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link BooleanQueryParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface BooleanQueryVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link BooleanQueryParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(BooleanQueryParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BooleanQueryParser#not}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNot(BooleanQueryParser.NotContext ctx);
	/**
	 * Visit a parse tree produced by {@link BooleanQueryParser#and}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnd(BooleanQueryParser.AndContext ctx);
	/**
	 * Visit a parse tree produced by {@link BooleanQueryParser#or}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOr(BooleanQueryParser.OrContext ctx);
	/**
	 * Visit a parse tree produced by {@link BooleanQueryParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtom(BooleanQueryParser.AtomContext ctx);
}