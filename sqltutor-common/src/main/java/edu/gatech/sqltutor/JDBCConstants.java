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
package edu.gatech.sqltutor;

import java.sql.DatabaseMetaData;

/**
 * Constants related to using JDBC.
 */
public class JDBCConstants {
	/** Constant column positions for <code>ResultSet</code>s that are specified by the API. */ 
	public static class ColumnPositions {
		
		/** Constants for {@link DatabaseMetaData#getTables(String, String, String, String[])} */
		public static class GetTables {
			public static final int 
				TABLE_CAT = 1,
				TABLE_SCHEM = 2,
				TABLE_NAME = 3,
				TABLE_TYPE = 4,
				REMARKS = 5,
				TYPE_CAT = 6,
				TYPE_SCHEM = 7,
				TYPE_NAME = 8,
				SELF_REFERENCING_COL_NAME = 9,
				REF_GENERATION = 10;
		}
		
		/** Constants for {@link DatabaseMetaData#getColumns(String, String, String, String)} */
		public static class GetColumns {
			public static final int 
				TABLE_CAT = 1,
				TABLE_SCHEM = 2,
				TABLE_NAME = 3,
				COLUMN_NAME = 4,
				DATA_TYPE = 5,
				TYPE_NAME = 6,
				COLUMN_SIZE = 7,
				BUFFER_LENGTH = 8,
				DECIMAL_DIGITS = 9,
				NUM_PREC_RADIX = 10,
				NULLABLE = 11,
				REMARKS = 12,
				COLUMN_DEF = 13,
				SQL_DATA_TYPE = 14,
				SQL_DATETIME_SUB = 15,
				CHAR_OCTET_LENGTH = 16,
				ORDINAL_POSITION = 17,
				IS_NULLABLE = 18,
				SCOPE_CATALOG = 19,
				SCOPE_SCHEMA = 20,
				SCOPE_TABLE = 21,
				SOURCE_DATA_TYPE = 22,
				IS_AUTOINCREMENT = 23,
				IS_GENERATEDCOLUMN = 24;
		}
	}
}
