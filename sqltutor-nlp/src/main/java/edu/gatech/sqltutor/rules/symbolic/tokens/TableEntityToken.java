/*
 *   Copyright (c) 2014 Program Analysis Group, Georgia Tech
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package edu.gatech.sqltutor.rules.symbolic.tokens;

import com.akiban.sql.parser.FromTable;
import com.akiban.sql.parser.QueryTreeNode;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.Utils;
import edu.gatech.sqltutor.rules.er.ERAttribute.DescriptionType;
import edu.gatech.sqltutor.rules.er.EREdgeConstraint;
import edu.gatech.sqltutor.rules.er.EREntity;
import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicException;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;

public class TableEntityToken extends AbstractSymbolicToken
		implements ISymbolicToken, INounToken, IScopedToken {
	
	/** The referenced entity. */
	protected EREntity entity;
	
	/** The referenced table. */
	protected FromTable table;
	
	/** The conjunct scope of this token. */
	protected QueryTreeNode cscope;
	
	protected String id;
	protected String singular;
	protected String plural;
	protected boolean definite;
	protected int cardinality = EREdgeConstraint.ANY_CARDINALITY;
	protected DescriptionType described = DescriptionType.NONE;
	
	public TableEntityToken(TableEntityToken token) {
		super(token);
		this.id = token.id;
		this.cscope = token.cscope;
		this.entity = token.entity;
		this.table = token.table;
		this.singular = token.singular;
		this.plural = token.plural;
		this.cardinality = token.cardinality;
		this.described = token.described;
	}
	
	public TableEntityToken(FromTable table) {
		this(table, PartOfSpeech.NOUN_SINGULAR_OR_MASS);
	}
	
	public TableEntityToken(FromTable table, PartOfSpeech pos) {
		super(pos);
		this.table = table;
	}

	@Override
	public SymbolicType getType() {
		return SymbolicType.TABLE_ENTITY;
	}
	
	@Override
	public void setPartOfSpeech(PartOfSpeech partOfSpeech) {
		if( !partOfSpeech.isNoun() && !partOfSpeech.isProperNoun() ) // TODO: || !partOfSpeech.isPronoun() ?
			throw new SymbolicException("Table entities must be nouns, proper nouns, or noun phrases: " + partOfSpeech);
		super.setPartOfSpeech(partOfSpeech);
	}
	
	public FromTable getTable() {
		return table;
	}
	
	public void setTable(FromTable table) {
		this.table = table;
	}

	@Override
	public String getSingularLabel() {
		return singular;
	}

	@Override
	public String getPluralLabel() {
		return plural;
	}

	@Override
	public void setSingularLabel(String label) {
		this.singular = label;
	}

	@Override
	public void setPluralLabel(String label) {
		this.plural = label;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	public DescriptionType getDescribed() {
		return described;
	}
	
	public void setDescribed(DescriptionType type) {
		this.described = type;
	}
	
	public int getCardinality() {
		return cardinality;
	}
	
	public void setCardinality(int cardinality) {
		this.cardinality = cardinality;
	}
	
	@Override
	public QueryTreeNode getConjunctScope() {
		return cscope;
	}
	
	@Override
	public void setConjunctScope(QueryTreeNode cscope) {
		this.cscope = cscope;
	}
	
	@Override
	protected StringBuilder addPropertiesString(StringBuilder b) {
		super.addPropertiesString(b);
		b.append(", id=").append(id)
			.append(", table=").append(QueryUtils.nodeToString(table))
			.append(", cscope=").append(Utils.ellided(QueryUtils.nodeToString(cscope)))
			.append(", singular=").append(singular)
			.append(", plural=").append(plural)
			.append(", card=");
		if( cardinality == EREdgeConstraint.ANY_CARDINALITY )
			b.append("N");
		else
			b.append(cardinality);
		return b;
	}

	@Override
	public boolean isDefinite() {
		return definite;
	}

	@Override
	public void setDefinite(boolean definite) {
		this.definite = definite;
	}
	
	protected boolean individual;

	@Override
	public boolean isIndividual() {
		return individual;
	}

	@Override
	public void setIndividual(boolean individual) {
		this.individual = individual;
	}
}
