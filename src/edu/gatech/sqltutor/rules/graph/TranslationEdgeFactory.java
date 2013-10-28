package edu.gatech.sqltutor.rules.graph;

import java.io.Serializable;

import org.jgrapht.EdgeFactory;

public class TranslationEdgeFactory
implements EdgeFactory<LabelNode, TranslationEdge>,
    Serializable
{
	private static final long serialVersionUID = 3618135658586388792L;

	public TranslationEdgeFactory() { }

	/**
	 * @see EdgeFactory#createEdge(Object, Object)
	 */
	public TranslationEdge createEdge(LabelNode source, LabelNode target) {
		try {
			return new TranslationEdge(source, target);
		} catch( Exception ex ) {
			throw new RuntimeException("Edge factory failed", ex);
		}
	}
}
