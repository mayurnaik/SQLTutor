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
import com.akiban.sql.parser.TableName;

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
		FromList fromList = select.getFromList();
		if( starColumns.size() == 0 )
			return;
		
		// remove the */*.* columns
		for( AllResultColumn c : starColumns )
			resultColumns.remove(c);
		
		// add t1.*, t2.*, ...
		for( int i = 0; i < fromList.size(); i++ ) {
			FromTable fromTable = fromList.get(i);

			AllResultColumn newCol = new AllResultColumn();
			newCol.setNodeType(NodeTypes.ALL_RESULT_COLUMN);
			try {
				TableName tName = new TableName();
				tName.init(null, fromTable.getExposedName());
				newCol.init(tName);
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
