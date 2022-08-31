/*
 * Copyright 2020 Philipp Salvisberg <philipp.salvisberg@trivadis.com>
 * 
 * Licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 
 * Unported License (the "License"); you may not use this file except 
 * in compliance with the License. You may obtain a copy of the License at
 * 
 *     https://creativecommons.org/licenses/by-nc-nd/3.0/
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.trivadis.tvdcc.validators

import com.trivadis.oracle.plsql.plsql.DeleteStatement
import com.trivadis.oracle.plsql.plsql.HintOrComment
import com.trivadis.oracle.plsql.plsql.InsertIntoClause
import com.trivadis.oracle.plsql.plsql.InsertStatement
import com.trivadis.oracle.plsql.plsql.MergeStatement
import com.trivadis.oracle.plsql.plsql.QueryBlock
import com.trivadis.oracle.plsql.plsql.TableReference
import com.trivadis.oracle.plsql.plsql.UpdateStatement
import com.trivadis.oracle.plsql.validation.PLSQLCopGuideline
import com.trivadis.oracle.plsql.validation.PLSQLCopValidator
import com.trivadis.oracle.plsql.validation.PLSQLValidator
import com.trivadis.oracle.plsql.validation.Remediation
import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap
import java.util.HashSet
import java.util.List
import java.util.regex.Pattern
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.validation.Check
import org.eclipse.xtext.validation.EValidatorRegistrar

class Hint extends PLSQLValidator implements PLSQLCopValidator {
	HashMap<Integer, PLSQLCopGuideline> guidelines

	// based on the query (19.16) SELECT name FROM v$sql_hint ORDER BY name;
	val ALL_HINTS = new HashSet<String>(Arrays.asList(
		"ADAPTIVE_PLAN",
		"ALL_ROWS",
		"AND_EQUAL",
		"ANSI_REARCH",
		"ANSWER_QUERY_USING_STATS",
		"ANTIJOIN",
		"APPEND",
		"APPEND_VALUES",
		"AUTO_REOPTIMIZE",
		"AV_CACHE",
		"BATCH_TABLE_ACCESS_BY_ROWID",
		"BIND_AWARE",
		"BITMAP",
		"BITMAP_AND",
		"BITMAP_TREE",
		"BUFFER",
		"BUSHY_JOIN",
		"BYPASS_RECURSIVE_CHECK",
		"BYPASS_UJVC",
		"CACHE",
		"CACHE_CB",
		"CARDINALITY",
		"CHANGE_DUPKEY_ERROR_INDEX",
		"CHECK_ACL_REWRITE",
		"CHOOSE",
		"CLUSTER",
		"CLUSTERING",
		"CLUSTER_BY_ROWID",
		"COALESCE_SQ",
		"COLUMN_STATS",
		"CONNECT_BY_CB_WHR_ONLY",
		"CONNECT_BY_COMBINE_SW",
		"CONNECT_BY_COST_BASED",
		"CONNECT_BY_ELIM_DUPS",
		"CONNECT_BY_FILTERING",
		"CONTAINERS",
		"COST_XML_QUERY_REWRITE",
		"CPU_COSTING",
		"CUBE_AJ",
		"CUBE_GB",
		"CUBE_SJ",
		"CURRENT_INSTANCE",
		"CURSOR_SHARING_EXACT",
		"DATA_SECURITY_REWRITE_LIMIT",
		"DATA_VALIDATE",
		"DBMS_STATS",
		"DB_VERSION",
		"DECORRELATE",
		"DEREF_NO_REWRITE",
		"DISABLE_PARALLEL_DML",
		"DIST_AGG_PROLLUP_PUSHDOWN",
		"DML_UPDATE",
		"DOMAIN_INDEX_FILTER",
		"DOMAIN_INDEX_NO_SORT",
		"DOMAIN_INDEX_SORT",
		"DRIVING_SITE",
		"DST_UPGRADE_INSERT_CONV",
		"DYNAMIC_SAMPLING",
		"DYNAMIC_SAMPLING_EST_CDN",
		"ELIMINATE_JOIN",
		"ELIMINATE_OBY",
		"ELIMINATE_SQ",
		"ELIM_GROUPBY",
		"ENABLE_PARALLEL_DML",
		"EXPAND_GSET_TO_UNION",
		"EXPAND_TABLE",
		"EXPR_CORR_CHECK",
		"FACT",
		"FACTORIZE_JOIN",
		"FBTSCAN",
		"FIRST_ROWS",
		"FORCE_XML_QUERY_REWRITE",
		"FRESH_MV",
		"FULL",
		"FULL_OUTER_JOIN_TO_OUTER",
		"GATHER_OPTIMIZER_STATISTICS",
		"GATHER_PLAN_STATISTICS",
		"GBY_CONC_ROLLUP",
		"GBY_PUSHDOWN",
		"HASH",
		"HASH_AJ",
		"HASH_SJ",
		"HWM_BROKERED",
		"IGNORE_OPTIM_EMBEDDED_HINTS",
		"IGNORE_ROW_ON_DUPKEY_INDEX",
		"IGNORE_WHERE_CLAUSE",
		"INCLUDE_VERSION",
		"INDEX",
		"INDEX_ASC",
		"INDEX_COMBINE",
		"INDEX_DESC",
		"INDEX_FFS",
		"INDEX_JOIN",
		"INDEX_RRS",
		"INDEX_RS_ASC",
		"INDEX_RS_DESC",
		"INDEX_SS",
		"INDEX_SS_ASC",
		"INDEX_SS_DESC",
		"INDEX_STATS",
		"INLINE",
		"INLINE_XMLTYPE_NT",
		"INMEMORY",
		"INMEMORY_PRUNING",
		"JSON_LENGTH",
		"LEADING",
		"LOCAL_INDEXES",
		"MATERIALIZE",
		"MEMOPTIMIZE_WRITE",
		"MERGE",
		"MERGE_AJ",
		"MERGE_CONST_ON",
		"MERGE_SJ",
		"MODEL_COMPILE_SUBQUERY",
		"MODEL_DONTVERIFY_UNIQUENESS",
		"MODEL_DYNAMIC_SUBQUERY",
		"MODEL_MIN_ANALYSIS",
		"MODEL_NO_ANALYSIS",
		"MODEL_PUSH_REF",
		"MONITOR",
		"MV_MERGE",
		"NATIVE_FULL_OUTER_JOIN",
		"NESTED_TABLE_FAST_INSERT",
		"NESTED_TABLE_GET_REFS",
		"NESTED_TABLE_SET_SETID",
		"NLJ_BATCHING",
		"NLJ_PREFETCH",
		"NL_AJ",
		"NL_SJ",
		"NOAPPEND",
		"NOCACHE",
		"NOPARALLEL",
		"NO_ACCESS",
		"NO_ADAPTIVE_PLAN",
		"NO_ANSI_REARCH",
		"NO_ANSWER_QUERY_USING_STATS",
		"NO_AUTO_REOPTIMIZE",
		"NO_BASETABLE_MULTIMV_REWRITE",
		"NO_BATCH_TABLE_ACCESS_BY_ROWID",
		"NO_BIND_AWARE",
		"NO_BUFFER",
		"NO_BUSHY_JOIN",
		"NO_CARTESIAN",
		"NO_CHECK_ACL_REWRITE",
		"NO_CLUSTERING",
		"NO_CLUSTER_BY_ROWID",
		"NO_COALESCE_SQ",
		"NO_CONNECT_BY_CB_WHR_ONLY",
		"NO_CONNECT_BY_COMBINE_SW",
		"NO_CONNECT_BY_COST_BASED",
		"NO_CONNECT_BY_ELIM_DUPS",
		"NO_CONNECT_BY_FILTERING",
		"NO_COST_XML_QUERY_REWRITE",
		"NO_CPU_COSTING",
		"NO_DATA_SECURITY_REWRITE",
		"NO_DECORRELATE",
		"NO_DIST_AGG_PROLLUP_PUSHDOWN",
		"NO_DOMAIN_INDEX_FILTER",
		"NO_DST_UPGRADE_INSERT_CONV",
		"NO_ELIMINATE_JOIN",
		"NO_ELIMINATE_OBY",
		"NO_ELIMINATE_SQ",
		"NO_ELIM_GROUPBY",
		"NO_EXPAND",
		"NO_EXPAND_GSET_TO_UNION",
		"NO_EXPAND_TABLE",
		"NO_FACT",
		"NO_FACTORIZE_JOIN",
		"NO_FULL_OUTER_JOIN_TO_OUTER",
		"NO_GATHER_OPTIMIZER_STATISTICS",
		"NO_GBY_PUSHDOWN",
		"NO_INDEX",
		"NO_INDEX_FFS",
		"NO_INDEX_SS",
		"NO_INMEMORY",
		"NO_INMEMORY_PRUNING",
		"NO_LOAD",
		"NO_MERGE",
		"NO_MODEL_PUSH_REF",
		"NO_MONITOR",
		"NO_MONITORING",
		"NO_MULTIMV_REWRITE",
		"NO_NATIVE_FULL_OUTER_JOIN",
		"NO_NLJ_BATCHING",
		"NO_NLJ_PREFETCH",
		"NO_ORDER_ROLLUPS",
		"NO_OR_EXPAND",
		"NO_OUTER_JOIN_TO_ANTI",
		"NO_OUTER_JOIN_TO_INNER",
		"NO_PARALLEL",
		"NO_PARALLEL_INDEX",
		"NO_PARTIAL_COMMIT",
		"NO_PARTIAL_JOIN",
		"NO_PARTIAL_ROLLUP_PUSHDOWN",
		"NO_PLACE_DISTINCT",
		"NO_PLACE_GROUP_BY",
		"NO_PQ_CONCURRENT_UNION",
		"NO_PQ_EXPAND_TABLE",
		"NO_PQ_REPLICATE",
		"NO_PQ_SKEW",
		"NO_PRUNE_GSETS",
		"NO_PULL_PRED",
		"NO_PUSH_HAVING_TO_GBY",
		"NO_PUSH_PRED",
		"NO_PUSH_SUBQ",
		"NO_PX_FAULT_TOLERANCE",
		"NO_PX_JOIN_FILTER",
		"NO_QKN_BUFF",
		"NO_QUERY_TRANSFORMATION",
		"NO_REF_CASCADE",
		"NO_REORDER_WIF",
		"NO_RESULT_CACHE",
		"NO_REWRITE",
		"NO_SEMIJOIN",
		"NO_SEMI_TO_INNER",
		"NO_SET_GBY_PUSHDOWN",
		"NO_SET_TO_JOIN",
		"NO_SQL_TUNE",
		"NO_STAR_TRANSFORMATION",
		"NO_STATEMENT_QUEUING",
		"NO_STATS_GSETS",
		"NO_SUBQUERY_PRUNING",
		"NO_SUBSTRB_PAD",
		"NO_SWAP_JOIN_INPUTS",
		"NO_TABLE_LOOKUP_BY_NL",
		"NO_TRANSFORM_DISTINCT_AGG",
		"NO_UNNEST",
		"NO_USE_CUBE",
		"NO_USE_DAGG_UNION_ALL_GSETS",
		"NO_USE_HASH",
		"NO_USE_HASH_AGGREGATION",
		"NO_USE_HASH_GBY_FOR_DAGGPSHD",
		"NO_USE_HASH_GBY_FOR_PUSHDOWN",
		"NO_USE_INVISIBLE_INDEXES",
		"NO_USE_MERGE",
		"NO_USE_NL",
		"NO_USE_PARTITION_WISE_DISTINCT",
		"NO_USE_PARTITION_WISE_GBY",
		"NO_USE_PARTITION_WISE_WIF",
		"NO_USE_SCALABLE_GBY_INVDIST",
		"NO_USE_VECTOR_AGGREGATION",
		"NO_VECTOR_TRANSFORM",
		"NO_VECTOR_TRANSFORM_DIMS",
		"NO_VECTOR_TRANSFORM_FACT",
		"NO_XDB_FASTPATH_INSERT",
		"NO_XMLINDEX_REWRITE",
		"NO_XMLINDEX_REWRITE_IN_SELECT",
		"NO_XML_DML_REWRITE",
		"NO_XML_QUERY_REWRITE",
		"NO_ZONEMAP",
		"NUM_INDEX_KEYS",
		"OLD_PUSH_PRED",
		"OPAQUE_TRANSFORM",
		"OPAQUE_XCANONICAL",
		"OPTIMIZER_FEATURES_ENABLE",
		"OPT_ESTIMATE",
		"OPT_PARAM",
		"ORDERED",
		"ORDERED_PREDICATES",
		"ORDER_KEY_VECTOR_USE",
		"ORDER_SUBQ",
		"OR_EXPAND",
		"OUTER_JOIN_TO_ANTI",
		"OUTER_JOIN_TO_INNER",
		"OUTLINE",
		"OUTLINE_LEAF",
		"OVERFLOW_NOMOVE",
		"PARALLEL_INDEX",
		"PARTIAL_JOIN",
		"PARTIAL_ROLLUP_PUSHDOWN",
		"PDB_LOCAL_ONLY",
		"PIV_GB",
		"PIV_SSF",
		"PLACE_DISTINCT",
		"PLACE_GROUP_BY",
		"PQ_CONCURRENT_UNION",
		"PQ_DISTRIBUTE",
		"PQ_DISTRIBUTE_WINDOW",
		"PQ_EXPAND_TABLE",
		"PQ_FILTER",
		"PQ_MAP",
		"PQ_NOMAP",
		"PQ_REPLICATE",
		"PQ_SKEW",
		"PRECOMPUTE_SUBQUERY",
		"PRESERVE_OID",
		"PULL_PRED",
		"PUSH_HAVING_TO_GBY",
		"PUSH_PRED",
		"PUSH_SUBQ",
		"PX_FAULT_TOLERANCE",
		"PX_JOIN_FILTER",
		"QB_NAME",
		"QUARANTINE",
		"QUEUE_CURR",
		"QUEUE_ROWP",
		"RBO_OUTLINE",
		"REF_CASCADE_CURSOR",
		"REMOTE_MAPPED",
		"REORDER_WIF",
		"RESERVOIR_SAMPLING",
		"RESTORE_AS_INTERVALS",
		"RESTRICT_ALL_REF_CONS",
		"RESULT_CACHE",
		"RETRY_ON_ROW_CHANGE",
		"REWRITE",
		"REWRITE_OR_ERROR",
		"ROWID",
		"RULE",
		"SAVE_AS_INTERVALS",
		"SCN_ASCENDING",
		"SEMIJOIN",
		"SEMIJOIN_DRIVER",
		"SEMI_TO_INNER",
		"SET_GBY_PUSHDOWN",
		"SET_TO_JOIN",
		"SHARED",
		"SKIP_EXT_OPTIMIZER",
		"SKIP_PROXY",
		"SKIP_UNQ_UNUSABLE_IDX",
		"SQLLDR",
		"SQL_SCOPE",
		"STAR",
		"STAR_TRANSFORMATION",
		"STATEMENT_QUEUING",
		"STREAMS",
		"SUBQUERY_PRUNING",
		"SUPPRESS_LOAD",
		"SWAP_JOIN_INPUTS",
		"SYSTEM_STATS",
		"SYS_DL_CURSOR",
		"SYS_PARALLEL_TXN",
		"SYS_RID_ORDER",
		"TABLE_LOOKUP_BY_NL",
		"TABLE_STATS",
		"TIV_GB",
		"TIV_SSF",
		"TRACING",
		"TRANSFORM_DISTINCT_AGG",
		"UNNEST",
		"USE_ANTI",
		"USE_CONCAT",
		"USE_CUBE",
		"USE_DAGG_UNION_ALL_GSETS",
		"USE_HASH",
		"USE_HASH_AGGREGATION",
		"USE_HASH_GBY_FOR_DAGGPSHD",
		"USE_HASH_GBY_FOR_PUSHDOWN",
		"USE_HIDDEN_PARTITIONS",
		"USE_INVISIBLE_INDEXES",
		"USE_MERGE",
		"USE_MERGE_CARTESIAN",
		"USE_NL",
		"USE_NL_WITH_INDEX",
		"USE_PARTITION_WISE_DISTINCT",
		"USE_PARTITION_WISE_GBY",
		"USE_PARTITION_WISE_WIF",
		"USE_SCALABLE_GBY_INVDIST",
		"USE_SEMI",
		"USE_TTT_FOR_GSETS",
		"USE_VECTOR_AGGREGATION",
		"USE_WEAK_NAME_RESL",
		"VECTOR_READ",
		"VECTOR_READ_TRACE",
		"VECTOR_TRANSFORM",
		"VECTOR_TRANSFORM_DIMS",
		"VECTOR_TRANSFORM_FACT",
		"WITH_PLSQL",
		"XDB_FASTPATH_INSERT",
		"XMLINDEX_REWRITE",
		"XMLINDEX_REWRITE_IN_SELECT",
		"XMLINDEX_SEL_IDX_TBL",
		"XMLTSET_DML_ENABLE",
		"XML_DML_RWT_STMT",
		"X_DYN_PRUNE",
		"ZONEMAP",
		// manually added, because these hints are missing in v$sqlhint
		"BEGIN_OUTLINE_DATA",
		"END_OUTLINE_DATA",
		"NOPARALLEL_INDEX",
		"NO_USE_BAND",
		"PARALLEL",
		"USE_BAND"
	));

	// must be overridden to avoid duplicate issues when used via ComposedChecks 
	override register(EValidatorRegistrar registrar) {
		val ePackages = getEPackages
		if (registrar.registry.get(ePackages.get(0)) === null) {
			// standalone validator, default registration required
			super.register(registrar)
		}
	}

	override getGuidelines() {
		if (guidelines === null) {
			guidelines = new HashMap<Integer, PLSQLCopGuideline>
			// register guidelines
			guidelines.put(9600,
				new PLSQLCopGuideline(9600, '''Never define more than one comment with hints.''', CRITICAL,
					INSTRUCTION_RELIABILITY, Remediation.createConstantPerIssue(5)))
			guidelines.put(9601,
				new PLSQLCopGuideline(9601, '''Never use unknown hints.''', CRITICAL, INSTRUCTION_RELIABILITY,
					Remediation.createConstantPerIssue(5)))
			guidelines.put(9602,
				new PLSQLCopGuideline(9602, '''Always use the alias name instead of the table name.''', CRITICAL,
					INSTRUCTION_RELIABILITY, Remediation.createConstantPerIssue(5)))
			guidelines.put(9603,
				new PLSQLCopGuideline(9603, '''Never reference an unknown table/alias.''', CRITICAL,
					INSTRUCTION_RELIABILITY, Remediation.createConstantPerIssue(5)))
			guidelines.put(9604,
				new PLSQLCopGuideline(9604, '''Never use an invalid stats method.''', CRITICAL,
					INSTRUCTION_RELIABILITY, Remediation.createConstantPerIssue(5)))
			guidelines.put(9605,
				new PLSQLCopGuideline(9605, '''Never use an invalid stats keyword.''', CRITICAL,
					INSTRUCTION_RELIABILITY, Remediation.createConstantPerIssue(5)))
		}
		return guidelines
	}

	// special warning signature to set a violation context within a comment
	def private warning(int id, String msg, HintOrComment h, int offset, int length) {
		val node = NodeModelUtils.getNode(h)
		acceptWarning('''«getGuidelineId(id)»: «getGuidelines.get(id).msg» «msg»''', h, node.offset + 3 + offset,
			length, getGuidelineId(id), #[h.text]);
	}

	def private dispatch getAllHints(QueryBlock stmt) {
		return stmt.hints
	}

	def private dispatch getAllHints(InsertStatement stmt) {
		return stmt.hints
	}

	def private dispatch getAllHints(UpdateStatement stmt) {
		return stmt.hints
	}

	def private dispatch getAllHints(DeleteStatement stmt) {
		return stmt.hints
	}

	def private dispatch getAllHints(MergeStatement stmt) {
		return stmt.hints
	}

	def private isFirstHint(HintOrComment h) {
		if (h.eContainer.allHints.findFirst[it.looksLikeHint] === h) {
			return true
		}
		return false
	}

	def private looksLikeHint(HintOrComment h) {
		if (h.text.length > 2 && h.text.substring(2, 3) == "+") {
			return true
		}
		return false
	}

	def private isHint(HintOrComment h) {
		if (h.looksLikeHint) {
			if (h.isFirstHint) {
				return true;
			}
			warning(9600, h)
			return false;
		}
		return false
	}

	def private getHintText(HintOrComment h) {
		var int endCommentLength = 0
		if (h.text.startsWith("/*")) {
			endCommentLength = 2
		}
		return h.text.substring(3, h.text.length - endCommentLength)
	}

	def private void checkUnknownHint(HintOrComment h) {
		val pattern = Pattern.compile('''(?i)([^\s\(]+)(\s*\([^\(\)]+\)*)*''')
		val matcher = pattern.matcher(h.hintText)
		while (matcher.find) {
			val hint = matcher.group(1)
			if (!ALL_HINTS.contains(hint.toUpperCase)) {
				warning(9601, '''"«hint»" is unknown.''', h, matcher.start(1), hint.length)
			}
		}
	}

	def private dispatch getTableReference(QueryBlock stmt) {
		val result = new ArrayList<Pair<String, String>>
		val tabrefs = EcoreUtil2.getAllContentsOfType(stmt, TableReference)
		for (tabref : tabrefs) {
			val parent = EcoreUtil2.getContainerOfType(tabref, QueryBlock)
			if (parent === stmt) {
				// table reference is defined on the same level as the hint (ignoring subqueries)
				result.add(new Pair(tabref?.queryTableExpression?.qteName?.value, tabref?.tableAlias?.alias?.value))
			}
		}
		return result
	}

	def private dispatch getTableReference(InsertStatement stmt) {
		val result = new ArrayList<Pair<String, String>>
		val tabrefs = EcoreUtil2.getAllContentsOfType(stmt, InsertIntoClause)
		for (tabref : tabrefs) {
			result.add(new Pair(tabref?.dmlExpressionClause?.dmlName?.value, tabref?.alias?.value))
		}
		return result
	}

	def private dispatch getTableReference(UpdateStatement stmt) {
		val result = new ArrayList<Pair<String, String>>
		result.add(new Pair(stmt.dmlTableExpressionClause?.dmlName?.value, stmt.alias?.value))
		return result
	}

	def private dispatch getTableReference(DeleteStatement stmt) {
		val result = new ArrayList<Pair<String, String>>
		result.add(new Pair(stmt.dmlTableExpressionClause?.dmlName?.value, stmt.alias?.value))
		return result
	}

	def private dispatch getTableReference(MergeStatement stmt) {
		val result = new ArrayList<Pair<String, String>>
		result.add(new Pair(stmt.intoClause?.table?.value, stmt.intoClause?.alias?.value))
		result.add(new Pair(stmt.usingClause?.table?.value, stmt.usingClause?.alias?.value))
		return result
	}

	// return tableName->alias pairs as unified structure	
	def private getTableReferences(HintOrComment h) {
		return h.eContainer.tableReference
	}

	def private getTableAliasOfReferencedTable(String tabspec, List<Pair<String, String>> tabrefs) {
		for (tabref : tabrefs) {
			val tableName = tabref.key
			val alias = tabref.value
			if (alias !== null && tableName !== null) {
				if (tableName.toLowerCase == tabspec.toLowerCase) {
					return alias
				}
			}
		}
		return null
	}

	def private isTableReference(String tabspec, List<Pair<String, String>> tabrefs) {
		for (tabref : tabrefs) {
			val tableName = tabref.key
			val alias = tabref.value
			if (alias?.toLowerCase == tabspec.toLowerCase) {
				return true
			}
			if (tableName?.toLowerCase == tabspec.toLowerCase) {
				return true
			}
		}
		return false
	}

	def private void checkTabspec(HintOrComment h, String hint, String tabspec, int tabspecOffset,
		List<Pair<String, String>> tabrefs) {
		if (tabspec.startsWith(">") || tabspec.contains("@")) {
			// do not check validity of tabspec to avoid false positives, see issue #33
		} else {
			val alias = tabspec.getTableAliasOfReferencedTable(tabrefs)
			if (alias !== null) {
				warning(9602, '''Use «alias» instead of «tabspec» in «hint» hint.''', h, tabspecOffset, tabspec.length)
			}
			if (!tabspec.isTableReference(tabrefs)) {
				warning(9603, '''(«tabspec» in «hint» hint).''', h, tabspecOffset, tabspec.length)
			}
		}
	}

	// syntax: https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/Comments.html#GUID-C18CDA01-D24C-4861-AA10-C57DF20C7E0F
	// allow multiple tabspec for all these hints to simplify implementation
	// allow comma as tabspec seperator, not documented, but working
	def private void checkLeadingHintSyntaxStyle(HintOrComment h) {
		val hints = Arrays.asList("CACHE", "CLUSTER", "DRIVING_SITE", "FACT", "FULL", "HASH", "INMEMORY",
			"INMEMORY_PRUNING", "LEADING", "NOCACHE", "NO_FACT", "NO_INMEMORY", "NO_INMEMORY_PRUNING", "NOPARALLEL",
			"NO_PARALLEL", "NO_PQ_SKEW", "NO_USE_BAND", "NO_USE_CUBE", "NO_USE_HASH", "NO_USE_MERGE", "NO_USE_NL",
			"PQ_DISTRIBUTE", "PQ_SKEW", "PX_JOIN_FILTER", "USE_BAND", "USE_CUBE", "USE_HASH", "USE_MERGE", "USE_NL")
		val pattern = Pattern.
			compile('''(?i)(\s|\+)(«FOR hint : hints SEPARATOR '|'»«hint»«ENDFOR»)\s*\((@[^\s]+)?([^\)]+)\)''')
		val matcher = pattern.matcher(h.hintText)
		var List<Pair<String, String>> tabrefs = null
		while (matcher.find) {
			if (tabrefs === null) {
				// populate only if hints are found
				tabrefs = h.tableReferences
			}
			val hint = matcher.group(2)
			val tabspecPattern = Pattern.compile('''[^\s,]+''')
			val tabspecMatcher = tabspecPattern.matcher(matcher.group(4))
			while (tabspecMatcher.find()) {
				h.checkTabspec(hint, tabspecMatcher.group, matcher.start(4) + tabspecMatcher.start, tabrefs)
			}
		}
	}

	// syntax: https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/Comments.html#GUID-BE83A338-FE21-444F-8CD9-455FC79C0057
	def private void checkIgnoreRowOnDupkeyIndexSyntaxStyle(HintOrComment h) {
		val pattern = Pattern.
			compile('''(?i)(\s|\+)(CHANGE_DUPKEY_ERROR_INDEX|IGNORE_ROW_ON_DUPKEY_INDEX)\s*\(\s*([^, \(]+)\s*((,\s*([^ \)]+)|(\([^\)]+\))\s*\)))''')
		val matcher = pattern.matcher(h.hintText)
		var List<Pair<String, String>> tabrefs = null
		while (matcher.find) {
			if (tabrefs === null) {
				// populate only if hints are found
				tabrefs = h.tableReferences
			}
			val hint = matcher.group(2)
			val tabspec = matcher.group(3)
			h.checkTabspec(hint, tabspec, matcher.start(3), tabrefs)
		}
	}

	// syntax: https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/Comments.html#GUID-72207153-4785-45D6-B1AA-CDB78D685FD1
	// similar to checkIndexSyntaxStyle, but must be handled separately because this case: "DYNAMIC_SAMPLING(10)", 10 is not a tabspec
	def private void checkDynamicSamplingSyntaxStyle(HintOrComment h) {
		val pattern = Pattern.compile('''(?i)(\s|\+)(DYNAMIC_SAMPLING)\s*\(\s*(@\s*[^\s]*)?\s*([^\s]*)?\s*(\d)+\)''')
		val matcher = pattern.matcher(h.hintText)
		var List<Pair<String, String>> tabrefs = null
		while (matcher.find) {
			val tabspec = matcher.group(4)
			if (tabspec !== null) {
				val hint = matcher.group(2)
				if (tabrefs === null) {
					// populate only if hints are found
					tabrefs = h.tableReferences
				}
				h.checkTabspec(hint, tabspec, matcher.start(4), tabrefs)
			}
		}
	}

	// syntax: https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/Comments.html#GUID-EC0D9F8A-20E7-4281-A16A-6B9C993F2930
	// simplified syntax treating integer/DEFAULT as indexspec for PARALLEL_INDEX, OK because interested in tablespec only
	def private void checkIndexSyntaxStyle(HintOrComment h) {
		val hints = Arrays.asList("INDEX", "INDEX_ASC", "INDEX_COMBINE", "INDEX_DESC", "INDEX_FFS", "INDEX_JOIN",
			"INDEX_SS", "INDEX_SS_ASC", "INDEX_SS_DESC", "NO_INDEX", "NO_INDEX_FFS", "NO_INDEX_SS", "NOPARALLEL_INDEX",
			"NO_PARALLEL_INDEX", "NO_ZONEMAP", "PARALLEL_INDEX", "USE_NL_WITH_INDEX")
		val pattern = Pattern.
			compile('''(?i)(\s|\+)(«FOR hint : hints SEPARATOR '|'»«hint»«ENDFOR»)\s*\(\s*(@\s*[^\s]*)?\s*([^\s]*)?\s*([^\s\)]+\s*)*\)''')
		val matcher = pattern.matcher(h.hintText)
		var List<Pair<String, String>> tabrefs = null
		while (matcher.find) {
			if (tabrefs === null) {
				// populate only if hints are found
				tabrefs = h.tableReferences
			}
			val hint = matcher.group(2)
			val tabspec = matcher.group(4)
			h.checkTabspec(hint, tabspec, matcher.start(4), tabrefs)
		}
	}

	// syntax: https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/Comments.html#GUID-4E431B5D-F61B-4F66-B86C-E9C8660E2FE7
	def private void checkMergeSyntaxStyle(HintOrComment h) {
		val hints = Arrays.asList("MERGE", "NO_MERGE", "PUSH_PRED", "NO_PUSH_PRED")
		val pattern = Pattern.
			compile('''(?i)(\s|\+)(«FOR hint : hints SEPARATOR '|'»«hint»«ENDFOR»)\s*(\(\s*(@\s*[^\s]*)?\s*([^\s\)]*)?\s*\))?''')
		val matcher = pattern.matcher(h.hintText)
		var List<Pair<String, String>> tabrefs = null
		while (matcher.find) {
			val tabspec = matcher.group(5)
			if (tabspec !== null) {
				val hint = matcher.group(2)
				if (tabrefs === null) {
					// populate only if hints are found
					tabrefs = h.tableReferences
				}
				h.checkTabspec(hint, tabspec, matcher.start(5), tabrefs)
			}
		}
	}

	def private isInteger(String value) {
		try {
			Integer.parseInt(value)
			return true
		} catch (NumberFormatException e) {
			return false
		}
	}

	// syntax: https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/Comments.html#GUID-D25225CE-2DCE-4D9F-8E82-401839690A6E
	// similar to checkIndexSyntaxStyle, but must be handled separately because cases as: "PARALLEL(AUTO)", AUTO is not a tabspec
	def private void checkParallelSyntaxStyle(HintOrComment h) {
		val pattern = Pattern.
			compile('''(?i)(\s|\+)(PARALLEL)\s*\(\s*(@\s*[^\s]*)?\s*([^\s,]*)?\s*,?\s*([^\s\)]+\s*)?\)''')
		val matcher = pattern.matcher(h.hintText)
		var List<Pair<String, String>> tabrefs = null
		while (matcher.find) {
			val tabspec = matcher.group(4)
			if (tabspec.equalsIgnoreCase("DEFAULT") || tabspec.equalsIgnoreCase("AUTO") ||
				tabspec.equalsIgnoreCase("MANUAL") || tabspec.isInteger) {
				// ignore, no tabspec to check
			} else {
				val hint = matcher.group(2)
				if (tabrefs === null) {
					// populate only if hints are found
					tabrefs = h.tableReferences
				}
				h.checkTabspec(hint, tabspec, matcher.start(4), tabrefs)
			}
		}
	}

	// syntax: see https://github.com/Trivadis/plsql-cop-validators/issues/47
	def private void checkTableStatsStyle(HintOrComment h) {
		val pattern = Pattern.
			compile('''(?i)(\s|\+)(table_stats)\s*(\s*\(("?[^\s\.\"]+"?(\s*\.\"?[^\s\.\"]+\"?)?)[\s,]+([^\s,]+)([^\)]*)\))''')
		val pattern2 = Pattern.compile('''(?i)(([^\s]+)\s*=)''')
		val matcher = pattern.matcher(h.hintText)
		while (matcher.find) {
			val hint = matcher.group(2);
			val method = matcher.group(6);
			if (!("DEFAULT".equalsIgnoreCase(method) || "SET".equalsIgnoreCase(method) ||
				"SCALE".equalsIgnoreCase(method) || "SAMPLE".equalsIgnoreCase(method))) {
				warning(9604, '''(«method» in «hint» hint).''', h, matcher.start(6), method.length)
			}
			val settings = matcher.group(7);
			val matcher2 = pattern2.matcher(settings)
			while (matcher2.find) {
				val keyword = matcher2.group(2);
				if (!("BLOCKS".equalsIgnoreCase(keyword) || "ROWS".equalsIgnoreCase(keyword) ||
					"ROW_LENGTH".equalsIgnoreCase(keyword))) {
					warning(9605, '''(«keyword» in «hint» hint).''', h, matcher.start(7) + matcher2.start(2), keyword.length)
				}
			}
		}
	}

	@Check
	def checkHint(HintOrComment h) {
		if (h.isHint) {
			// check all hints
			h.checkUnknownHint

			// check hints in disjunct groups with similar syntax to extract and check tabspecs
			// named group by a popular member
			// named query blocks are not evaluated, tabspecs are expected to be found in the current block
			h.checkLeadingHintSyntaxStyle
			h.checkIgnoreRowOnDupkeyIndexSyntaxStyle
			h.checkDynamicSamplingSyntaxStyle
			h.checkIndexSyntaxStyle
			h.checkMergeSyntaxStyle
			h.checkParallelSyntaxStyle
			h.checkTableStatsStyle
		}
	}

}
