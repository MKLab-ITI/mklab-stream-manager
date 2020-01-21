package gr.iti.mklab.framework.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gr.iti.mklab.framework.utils.booleanparse.BooleanQueryLexer;
import gr.iti.mklab.framework.utils.booleanparse.BooleanQueryParser;
import gr.iti.mklab.framework.utils.booleanparse.BooleanQueryParser.ExpressionContext;
import gr.iti.mklab.framework.utils.booleanparse.ast.BooleanExpression;
import gr.iti.mklab.framework.utils.booleanparse.ast.Node;
import gr.iti.mklab.framework.utils.booleanparse.ast.visitor.ASTBuilderVisitor;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import com.bpodgursky.jbool_expressions.And;
import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.Not;
import com.bpodgursky.jbool_expressions.Or;
import com.bpodgursky.jbool_expressions.Variable;
import com.bpodgursky.jbool_expressions.rules.RuleSet;

public class QueryUtils {

	public static boolean isBooleanExpression(String q) {
		if(q.matches(".* OR .*") || q.matches(".* AND .*")) {
			return true;
		}
		return false;
	}
	
	public static Set<Set<String>> parse(String q) {

		Node ast = toAST(q);

        Expression<String> expr = toBooleanExpression(ast);    
        expr = RuleSet.simplify(expr);
        expr = RuleSet.toSop(expr);

        q = expr.toString();
        q = q.replaceAll(" & ", " AND ");
        q = q.replaceAll(" \\| ", " OR ");
        
        Node sopAst = toAST(q); 
        return astToSoP(sopAst);

	}
	
	public static Set<Set<String>> astToSoP(Node ast) {
		Set<Set<String>> queries = new HashSet<Set<String>>(); 
		BooleanExpression tree = (BooleanExpression) ast;
		if("or".equals(tree.toString())) {
			if(tree.getOperands() != null && !tree.getOperands().isEmpty()) {
				for(Node andNode : tree.getOperands()) {
					BooleanExpression andBranch = (BooleanExpression) andNode;
					if("and".toString().equals(andBranch.toString())) {
						Set<String> tags = new HashSet<String>(); 
						for(Node terminalNode : andBranch.getOperands()) {
							tags.add(terminalNode.toString());
						}
						queries.add(tags);
					}
					else {
						Set<String> tags = new HashSet<String>(); 
						tags.add(andBranch.toString());
						queries.add(tags);
					}
				}
			}
		}
		
		return queries;
	}
	
	public static Node toAST(String q) {
		BooleanQueryLexer lexer = new BooleanQueryLexer(new ANTLRInputStream(q));
        TokenStream tokens = new CommonTokenStream(lexer);
        BooleanQueryParser parser = new BooleanQueryParser(tokens);
        
        ExpressionContext expession = parser.expression();
        ASTBuilderVisitor builder = new ASTBuilderVisitor();
        Node ast = builder.visit(expession);
        
        return ast;
	}
	
	public static Expression<String> toBooleanExpression(Node ast) {
		BooleanExpression tree = (BooleanExpression) ast;
		if(tree.operands == null) {
			return Variable.of(tree.toString());
		}
		else {
			List<Expression<String>> expressions = new ArrayList<Expression<String>>();
			for(Node child : tree.operands) {
				Expression<String> expression = toBooleanExpression(child);
				expressions.add(expression);
			}
			
			String type = ast.toString();
			if(type.equals("and") || type.equals("atom")) {
				return And.of(expressions);
			}
			else if(type.equals("or")) {
				return Or.of(expressions);
			}
			else if(type.equals("not")) {
				if(expressions.size() == 1) {
					return Not.of(expressions.get(0));
				}
				else {
					Expression<String> expression = expressions.remove(0);
					return And.of(Not.of(And.of(expressions)), expression);
				}
			}
		}
		return null;
	}
	
	public static Query toBNF(Node ast) {
		Builder bq = new BooleanQuery.Builder();
		BooleanExpression tree = (BooleanExpression) ast;
		if(tree.operands == null) {
			Query query = new TermQuery(new Term("", tree.toString()));
			return query;
		}
		else {
			List<Query> expressions = new ArrayList<Query>();
			for(Node child : tree.operands) {
				Query expression = toBNF(child);
				expressions.add(expression);
			}
			
			String type = ast.toString();
			if(type.equals("and")) {
				for(Query exp : expressions) {
					bq.add(exp, Occur.MUST);
				}
			}
			else if(type.equals("atom")) {
				for(Query exp : expressions) {
					bq.add(exp, Occur.SHOULD);
				}
			}
			else if(type.equals("or")) {
				for(Query exp : expressions) {
					bq.add(exp, Occur.SHOULD);
				}
			}
			else if(type.equals("not")) {
				if(expressions.size() == 1) {
					bq.add(expressions.get(0), Occur.MUST_NOT);
				}
				else {
					bq.add(expressions.remove(0), Occur.MUST);
					for(Query exp : expressions) {
						bq.add(exp, Occur.MUST_NOT);
					}
				}
			}
		}
		return bq.build();
	}
	
	public static void printTree(Node ast) {
		printTree(ast, 0);
	}
	
	private static void printTree(Node ast, int depth) {
		BooleanExpression tree = (BooleanExpression) ast;
		System.out.print(StringUtils.repeat("\t", depth));
		System.out.println(ast.toString());
		
		depth++;
		if(tree.operands != null) {
			for(Node child : tree.operands) {
				printTree(child, depth);
			}
		}
	}
	
	public static void main(String...args) {
		String q = "(recyclicing OR waste) AND (food OR waste) NOT(waste time)";
	
		Set<Set<String>> queries = QueryUtils.parse(q);
		System.out.println(queries);
		
		System.out.println(QueryUtils.isBooleanExpression(q));
	}
	
}
