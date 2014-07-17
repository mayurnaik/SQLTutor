package edu.gatech.sqltutor.rules.util;

import java.util.ArrayList;
import java.util.List;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.AllResultColumn;
import com.akiban.sql.parser.FromList;
import com.akiban.sql.parser.FromTable;
import com.akiban.sql.parser.NodeTypes;
import com.akiban.sql.parser.ResultColumn;
import com.akiban.sql.parser.ResultColumnList;
import com.akiban.sql.parser.SelectNode;

import edu.gatech.sqltutor.SQLTutorException;

/**
 * Converts <code>*</code> columns to those of the individual tables.
 * 
 * <p>For example:<br>
 * <code>SELECT * FROM table1 t1, table2 t2</code><br>
 * Becomes:<br>
 * <code>SELECT t1.*, t2.* FROM table t1, table2 t2</code></p>
 */
public class StarColumnSplitter {

	public StarColumnSplitter() {
	}
	
	/**
	 * Split if necessary, updating the given query.
	 */
	public void split(SelectNode select) {
		final ResultColumnList resultColumns = select.getResultColumns();
		List<AllResultColumn> starColumns = getStarColumns(resultColumns);
		if( starColumns.size() == 0 )
			return;
		
		// remove the */*.* columns
		AllResultColumn origColumn = null;
		for( AllResultColumn c: starColumns )
			resultColumns.remove(origColumn = c);
		
		// add t1.*, t2.*, ...
		FromList fromList = select.getFromList();
		for( int i = 0, len = fromList.size(); i < len; ++i ) {
			FromTable fromTable = fromList.get(i);
			
			AllResultColumn newCol = new AllResultColumn();
			newCol.setParserContext(origColumn.getParserContext());
			newCol.setNodeType(NodeTypes.ALL_RESULT_COLUMN);
			newCol.setBeginOffset(origColumn.getBeginOffset());
			newCol.setEndOffset(origColumn.getEndOffset());
			try {
				newCol.copyFrom(origColumn);
				newCol.init(fromTable.getTableName());
			} catch (StandardException e) {
				throw new SQLTutorException(e);
			}
			
			resultColumns.addResultColumn(newCol);
		}
	}

	private List<AllResultColumn> getStarColumns(final ResultColumnList resultColumns) {
		List<AllResultColumn> starColumns = new ArrayList<AllResultColumn>(1);
		for( int i = 0, len = resultColumns.size(); i < len; ++i ) {
			ResultColumn column = resultColumns.get(i);
			if( column instanceof AllResultColumn ) {
				AllResultColumn all = (AllResultColumn)column;
				if( all.getTableNameObject() == null )
					starColumns.add(all);
			}
		}
		return starColumns;
	}

}
