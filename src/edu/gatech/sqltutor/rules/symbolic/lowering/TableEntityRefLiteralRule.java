package edu.gatech.sqltutor.rules.symbolic.lowering;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.EnumSet;
import java.util.List;

import org.deri.iris.api.basics.IQuery;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ISymbolicTranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.TranslationPhase;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicPredicates;
import edu.gatech.sqltutor.rules.lang.StandardSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicException;
import edu.gatech.sqltutor.rules.symbolic.SymbolicQueries;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;
import edu.gatech.sqltutor.rules.symbolic.SymbolicUtil;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SequenceToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityRefToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityToken;
import edu.gatech.sqltutor.rules.util.Literals;

public class TableEntityRefLiteralRule 
		extends StandardSymbolicRule implements ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(TableEntityRefLiteralRule.class);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(SymbolicPredicates.type, "?ref", SymbolicType.TABLE_ENTITY_REF)
	);
	
	public TableEntityRefLiteralRule() {}
	public TableEntityRefLiteralRule(int precedence) { super(precedence); }
	
	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		// FIXME need to account for context, for now just substitute the labels
		boolean applied = false;
		
		final SymbolicQueries queries = state.getQueries();
		
		while( ext.nextTuple() ) {
			TableEntityRefToken ref = ext.getToken("?ref");
			TableEntityToken token = ref.getTableEntity();
			
			PartOfSpeech pos = ref.getPartOfSpeech();
			boolean isSingular;
			switch( pos ) {
			case NOUN_SINGULAR_OR_MASS:
				isSingular = true;
				break;
			case NOUN_PLURAL:
				isSingular = false;
				break;
			default:
				throw new SymbolicException("FIXME: Can't handle part of speech: " + pos);
			}

			
			// some references need to use an id/variable due to ambiguous labels
			boolean insertIdAfter = false;
			ISymbolicToken replacementToken = null;
			boolean checkDeterminer = true;
			if( ref.getNeedsId() ) {
				TableEntityRefToken earliest = queries.getEarliestRef(token);
				String idLabel = "_" + token.getId() + "_";
				LiteralToken idToken = new LiteralToken(idLabel, 
					isSingular ? PartOfSpeech.PROPER_NOUN_SINGULAR : PartOfSpeech.PROPER_NOUN_PLURAL);
				
				if( ref == earliest ) {
					_log.trace(Markers.SYMBOLIC, "Replacing earliest id'd ref: {}", ref);
					SequenceToken seq = new SequenceToken(PartOfSpeech.NOUN_PHRASE);
					seq.addChild(new LiteralToken(isSingular ? token.getSingularLabel() : token.getPluralLabel(), 
							ref.getPartOfSpeech()));
					seq.addChild(idToken);
					replacementToken = seq;
					insertIdAfter = true;
				} else {
					_log.trace(Markers.SYMBOLIC, "Replacing non-earliest id'd ref: {}", ref);
					replacementToken = idToken;
					checkDeterminer = false;
				}
				
			} else {
				_log.trace(Markers.SYMBOLIC, "Non-id reference: {}", ref);
				replacementToken = new LiteralToken(isSingular ? token.getSingularLabel() : token.getPluralLabel(), ref.getPartOfSpeech());
			}
			
			// see if we need to insert a determiner
			if( checkDeterminer ) {
				ISymbolicToken before = SymbolicUtil.getPotentialDeterminer(ref);
				if( before == null || before.getPartOfSpeech() != PartOfSpeech.DETERMINER ) {
					ISymbolicToken parent = ref.getParent();
					List<ISymbolicToken> siblings = parent.getChildren();
					int idx = siblings.indexOf(ref);
					if( before.getPartOfSpeech().isPossessive() ) {
						// FIXME we really need a smarter check here
						_log.warn("Skipping determiner insertion due to possessive ending.");
					} else if ( insertIdAfter || !ref.getNeedsId() ) {
						LiteralToken det = Literals.the(); // FIXME "a"/"an"?
						siblings.add(idx, det);
						det.setParent(parent);
						_log.debug(Markers.SYMBOLIC, "Inserted determiner into: {}", parent);
					}
						
				}
			}
			
			SymbolicUtil.replaceChild(ref, replacementToken);
			_log.debug(Markers.SYMBOLIC, "Replaced token {} with {}", ref, replacementToken);
			applied = true;
		}
		return applied;
	}
	
	@Override
	protected IQuery getQuery() { return QUERY; }
	
	@Override
	protected int getVariableEstimate() {
		return 1;
	}
	
	@Override
	protected int getDefaultPrecedence() {
		return DefaultPrecedence.LOWERING;
	}
	
	@Override
	protected EnumSet<TranslationPhase> getDefaultPhases() {
		return EnumSet.of(TranslationPhase.LOWERING);
	}
}
