package edu.gatech.sqltutor.rules.symbolic.tokens;

import com.akiban.sql.parser.QueryTreeNode;

import edu.gatech.sqltutor.Utils;

/**
 * An SQL token that has a noun part of speech, 
 * such as columns or table references.
 */
public class SQLNounToken extends SQLToken implements INounToken {
	protected String singular;
	protected String plural;

	public SQLNounToken(QueryTreeNode astNode) {
		super(astNode);
	}

	public SQLNounToken(SQLNounToken token) {
		super(token);
		this.singular = token.singular;
		this.plural = token.plural;
	}
	
	@Override
	protected StringBuilder addPropertiesString(StringBuilder b) {
		b = super.addPropertiesString(b);
		b.append(", singular=\"").append(Utils.escapeChars(singular, "\""))
			.append("\", plural=\"").append(Utils.escapeChars(plural, "\""))
			.append("\"");
		return b;
	}

	@Override
	public String getSingularLabel() {
		return singular;
	}

	@Override
	public void setSingularLabel(String singular) {
		this.singular = singular;
	}

	@Override
	public String getPluralLabel() {
		return plural;
	}

	@Override
	public void setPluralLabel(String plural) {
		this.plural = plural;
	}
}
