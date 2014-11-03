package de.tudarmstadt.stg.monto.parser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import de.tudarmstadt.stg.monto.message.Product;
import de.tudarmstadt.stg.monto.message.ProductMessage;
import de.tudarmstadt.stg.monto.message.VersionMessage;
import de.tudarmstadt.stg.monto.server.AbstractServer;
import de.tudarmstadt.stg.monto.token.java8.Java8Lexer;
import de.tudarmstadt.stg.monto.token.java8.Java8Parser;

public class JavaParser extends AbstractServer {

	private final static Product ast = new Product("ast");

	@Override
	public void onVersionMessage(VersionMessage message) {
		if(message.getLanguage().toString().equals("java")) {
			try {
				Java8Lexer lexer = new Java8Lexer(new ANTLRInputStream(message.getContent().getReader()));
				CommonTokenStream tokens = new CommonTokenStream(lexer);
				Java8Parser parser = new Java8Parser(tokens);
				ParserRuleContext root = parser.compilationUnit();
				ParseTreeWalker walker = new ParseTreeWalker();

				Converter converter = new Converter();
				walker.walk(converter, root);
				
				emitProductMessage(
						new ProductMessage(
								message.getSource(), 
								ast, 
								message.getLanguage(), 
								ASTs.encode(converter.getRoot())));
				
			} catch (Exception e) {
				
			}
		}
	}
	
	public static class Converter implements ParseTreeListener {

		private Deque<AST> nodes = new ArrayDeque<>();
		
		@Override
		public void enterEveryRule(ParserRuleContext context) {
			String name = Java8Parser.ruleNames[context.getRuleIndex()];
			List<AST>childs = new ArrayList<>(context.getChildCount());
			NonTerminal node = new NonTerminal(name, childs);
			addChild(node);
			nodes.push(node);
		}

		@Override
		public void exitEveryRule(ParserRuleContext node) {
			// Keep the last node to return
			if(nodes.size() > 1)
				nodes.pop();
		}

		@Override
		public void visitErrorNode(ErrorNode err) {
			Interval interval = err.getSourceInterval();
			addChild(new Terminal(interval.a, interval.length()));
		}

		@Override
		public void visitTerminal(TerminalNode terminal) {
			org.antlr.v4.runtime.Token symbol = terminal.getSymbol();
			Terminal token = new Terminal(symbol.getStartIndex(), symbol.getStopIndex() - symbol.getStartIndex() + 1);
			if(nodes.size() == 0)
				nodes.push(token);
			else
				addChild(token);
		}
		
		private void addChild(AST node) {
			if(! nodes.isEmpty() && nodes.peek() instanceof NonTerminal)
				((NonTerminal) nodes.peek()).addChild(node);
		}

		public AST getRoot() {
			return nodes.peek();
		}
	}

}