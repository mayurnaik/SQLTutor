/**
 * CodeMirror, copyright (c) by Marijn Haverbeke and others
 * Distributed under an MIT license: http://codemirror.net/LICENSE
 * 
 * This is the modified version of 
 * https://github.com/codemirror/CodeMirror/blob/master/addon/hint/sql-hint.js
 * to work with primefaces-extensions version of CodeMirror
 * 
 * @modifier: Noel Vo
 */

(function() {
  "use strict";

  var tables;
  var defaultTable;
  var keywords;
  var CONS = {
    QUERY_DIV: ";",
    ALIAS_KEYWORD: "AS"
  };
  var Pos = CodeMirror.Pos;

  function getKeywords(editor) {
	  return (
	  	// a
	  	"abort accept access add all alter and any array arraylen as asc " +
	  	"assert assign at attributes audit authorization avg " +
	  	// b
	  	"base_table begin between binary_integer body boolean by " +
	  	// c
	  	"case cast char char_base check close cluster clusters colauth column comment commit compress " +
	  	"connect connected constant constraint crash create current currval cursor " +
	  	// d
	  	"data_base database date dba deallocate debugoff debugon decimal declare default " +
	  	"definition delay delete desc digits dispose distinct do drop " +
	  	// e
	  	"else elseif elsif enable end entry escape exception exception_init exchange exclusive exists exit external " +
	  	// f
	  	"fast fetch file for force form from function " +
	  	// g
	  	"generic goto grant group " +
	  	// h
	  	"having " +
	  	// i
	  	"identified if immediate in increment index indexes indicator initial initrans insert interface intersect into is " +
	  	// j
	  	// k
	  	"key " +
	  	// l
	  	"level library like limited local lock log logging long loop limit" +
	  	// m
	  	"master maxextents maxtrans member minextents minus mislabel mode modify multiset " +
	  	// n
	  	"new next no noaudit nocompress nologging noparallel not nowait number_base " +
	  	// o
	  	"object of off offline on online only open option or order out overlaps" +
	  	// p
	  	"package parallel partition pctfree pctincrease pctused pls_integer positive positiven " +
	  	"pragma primary prior private privileges procedure public " +
	  	// q
	  	// r
	  	"raise range raw read rebuild record ref references refresh release rename replace resource restrict " +
	  	"return returning returns reverse revoke rollback row rowid rowlabel rownum rows run " +
	  	// s
	  	"savepoint schema segment select separate session set share snapshot some space split sql start " +
	  	"statement storage subtype successful synonym " + 
	  	// t
	  	"tabauth table tables tablespace task terminate then to trigger truncate type " + 
	  	// u
	  	"union unique unlimited unrecoverable unusable update use using " +
	  	// v
	  	"validate value values variable view views " +
	  	// w
	  	"when whenever where while with work").split(" ");
  }

  function match(string, word) {
    var len = string.length;
    var sub = word.substr(0, len);
    return string.toUpperCase() === sub.toUpperCase();
  }

  function addMatches(result, search, wordlist, formatter) {
    for (var word in wordlist) {
      if (!wordlist.hasOwnProperty(word)) continue;
      if (Array.isArray(wordlist)) {
        word = wordlist[word];
      }
      if (match(search, word)) {
        result.push(formatter(word));
      }
    }
  }

  function nameCompletion(cur, token, result, editor) {
    var useBacktick = (token.string.charAt(0) == "`");
    var string = token.string.substr(1);
    var prevToken = editor.getTokenAt(Pos(cur.line, token.start));
    if (token.string.charAt(0) == "." || prevToken.string == "."){
      //Suggest colunm names
      if (prevToken.string == ".") {
        var prevToken = editor.getTokenAt(Pos(cur.line, token.start - 1));
      }
      var table = prevToken.string;
      //Check if backtick is used in table name. If yes, use it for columns too.
      var useBacktickTable = false;
      if (table.match(/`/g)) {
        useBacktickTable = true;
        table = table.replace(/`/g, "");
      }
      //Check if table is available. If not, find table by Alias
      if (!tables.hasOwnProperty(table))
        table = findTableByAlias(table, editor);
      var columns = tables[table];
      if (!columns) return;

      if (useBacktick) {
        addMatches(result, string, columns, function(w) {return "`" + w + "`";});
      }
      else if(useBacktickTable) {
        addMatches(result, string, columns, function(w) {return ".`" + w + "`";});
      }
      else {
        addMatches(result, string, columns, function(w) {return "." + w;});
      }
    }
    else {
      //Suggest table names or colums in defaultTable
      while (token.start && string.charAt(0) == ".") {
        token = editor.getTokenAt(Pos(cur.line, token.start - 1));
        string = token.string + string;
      }
      if (useBacktick) {
        addMatches(result, string, tables, function(w) {return "`" + w + "`";});
        addMatches(result, string, defaultTable, function(w) {return "`" + w + "`";});
      }
      else {
        addMatches(result, string, tables, function(w) {return w;});
        addMatches(result, string, defaultTable, function(w) {return w;});
      }
    }
  }

  function eachWord(lineText, f) {
    if (!lineText) return;
    var excepted = /[,;]/g;
    var words = lineText.split(" ");
    for (var i = 0; i < words.length; i++) {
      f(words[i]?words[i].replace(excepted, '') : '');
    }
  }

  function convertCurToNumber(cur) {
    // max characters of a line is 999,999.
    return cur.line + cur.ch / Math.pow(10, 6);
  }

  function convertNumberToCur(num) {
    return Pos(Math.floor(num), +num.toString().split('.').pop());
  }

  function findTableByAlias(alias, editor) {
    var doc = editor.doc;
    var fullQuery = doc.getValue();
    var aliasUpperCase = alias.toUpperCase();
    var previousWord = "";
    var table = "";
    var separator = [];
    var validRange = {
      start: Pos(0, 0),
      end: Pos(editor.lastLine(), editor.getLineHandle(editor.lastLine()).length)
    };

    //add separator
    var indexOfSeparator = fullQuery.indexOf(CONS.QUERY_DIV);
    while(indexOfSeparator != -1) {
      separator.push(doc.posFromIndex(indexOfSeparator));
      indexOfSeparator = fullQuery.indexOf(CONS.QUERY_DIV, indexOfSeparator+1);
    }
    separator.unshift(Pos(0, 0));
    separator.push(Pos(editor.lastLine(), editor.getLineHandle(editor.lastLine()).text.length));

    //find valid range
    var prevItem = 0;
    var current = convertCurToNumber(editor.getCursor());
    for (var i=0; i< separator.length; i++) {
      var _v = convertCurToNumber(separator[i]);
      if (current > prevItem && current <= _v) {
        validRange = { start: convertNumberToCur(prevItem), end: convertNumberToCur(_v) };
        break;
      }
      prevItem = _v;
    }

    var query = doc.getRange(validRange.start, validRange.end, false);

    for (var i = 0; i < query.length; i++) {
      var lineText = query[i];
      eachWord(lineText, function(word) {
        var wordUpperCase = word.toUpperCase();
        if (wordUpperCase === aliasUpperCase && tables.hasOwnProperty(previousWord)) {
            table = previousWord;
        }
        if (wordUpperCase !== CONS.ALIAS_KEYWORD) {
          previousWord = word;
        }
      });
      if (table) break;
    }
    return table;
  }

  CodeMirror.sqlHint = function(editor, options){
    tables = (options && options.tables) || {};
    var defaultTableName = options && options.defaultTable;
    defaultTable = (defaultTableName && tables[defaultTableName] || []);
    keywords = getKeywords(editor);
    
    var cur = editor.getCursor();
    var result = [];
    var token = editor.getTokenAt(cur), start, end, search;
    
    if (token.end > cur.ch) {
      token.end = cur.ch;
      token.string = token.string.slice(0, cur.ch - token.start);
    }

    if (token.string.match(/^[.`\w@]\w*$/)) {
      search = token.string;
      start = token.start;
      end = token.end;
    } else {
      start = end = cur.ch;
      search = "";
    }
    
    if (search.charAt(0) == "." || search.charAt(0) == "`") {
    	nameCompletion(cur, token, result, editor);
    } else {
      addMatches(result, search, tables, function(w) {return w;});
      addMatches(result, search, defaultTable, function(w) {return w;});
      addMatches(result, search, keywords, function(w) {return w.toUpperCase();});
    }

    return {list: result, 
    		from: {line: cur.line, ch: token.start},
    		to  : {line: cur.line, ch: token.end}};
  };
})();
