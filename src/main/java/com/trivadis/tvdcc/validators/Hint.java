/**
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
package com.trivadis.tvdcc.validators;

import com.google.common.base.Objects;
import com.trivadis.oracle.plsql.plsql.DeleteStatement;
import com.trivadis.oracle.plsql.plsql.DmlTableExpressionClause;
import com.trivadis.oracle.plsql.plsql.HintOrComment;
import com.trivadis.oracle.plsql.plsql.InsertIntoClause;
import com.trivadis.oracle.plsql.plsql.InsertStatement;
import com.trivadis.oracle.plsql.plsql.MergeIntoClause;
import com.trivadis.oracle.plsql.plsql.MergeStatement;
import com.trivadis.oracle.plsql.plsql.MergeUsingClause;
import com.trivadis.oracle.plsql.plsql.QueryBlock;
import com.trivadis.oracle.plsql.plsql.QueryTableExpression;
import com.trivadis.oracle.plsql.plsql.SimpleExpressionNameValue;
import com.trivadis.oracle.plsql.plsql.TableAlias;
import com.trivadis.oracle.plsql.plsql.TableReference;
import com.trivadis.oracle.plsql.plsql.UpdateStatement;
import com.trivadis.oracle.plsql.validation.PLSQLCopGuideline;
import com.trivadis.oracle.plsql.validation.PLSQLCopValidator;
import com.trivadis.oracle.plsql.validation.PLSQLValidator;
import com.trivadis.oracle.plsql.validation.Remediation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Pair;

@SuppressWarnings("all")
public class Hint extends PLSQLValidator implements PLSQLCopValidator {
  private HashMap<Integer, PLSQLCopGuideline> guidelines;

  private final HashSet<String> ALL_HINTS = new HashSet<String>(
    Arrays.<String>asList(
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
      "BEGIN_OUTLINE_DATA", 
      "END_OUTLINE_DATA", 
      "NOPARALLEL_INDEX", 
      "NO_USE_BAND", 
      "PARALLEL", 
      "USE_BAND"));

  @Override
  public void register(final EValidatorRegistrar registrar) {
    final List<EPackage> ePackages = this.getEPackages();
    Object _get = registrar.getRegistry().get(ePackages.get(0));
    boolean _tripleEquals = (_get == null);
    if (_tripleEquals) {
      super.register(registrar);
    }
  }

  @Override
  public HashMap<Integer, PLSQLCopGuideline> getGuidelines() {
    if ((this.guidelines == null)) {
      HashMap<Integer, PLSQLCopGuideline> _hashMap = new HashMap<Integer, PLSQLCopGuideline>();
      this.guidelines = _hashMap;
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Never define more than one comment with hints.");
      Remediation _createConstantPerIssue = Remediation.createConstantPerIssue(Integer.valueOf(5));
      PLSQLCopGuideline _pLSQLCopGuideline = new PLSQLCopGuideline(Integer.valueOf(9600), _builder.toString(), PLSQLValidator.CRITICAL, 
        PLSQLValidator.INSTRUCTION_RELIABILITY, _createConstantPerIssue);
      this.guidelines.put(Integer.valueOf(9600), _pLSQLCopGuideline);
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("Never use unknown hints.");
      Remediation _createConstantPerIssue_1 = Remediation.createConstantPerIssue(Integer.valueOf(5));
      PLSQLCopGuideline _pLSQLCopGuideline_1 = new PLSQLCopGuideline(Integer.valueOf(9601), _builder_1.toString(), PLSQLValidator.CRITICAL, PLSQLValidator.INSTRUCTION_RELIABILITY, _createConstantPerIssue_1);
      this.guidelines.put(Integer.valueOf(9601), _pLSQLCopGuideline_1);
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("Always use the alias name instead of the table name.");
      Remediation _createConstantPerIssue_2 = Remediation.createConstantPerIssue(Integer.valueOf(5));
      PLSQLCopGuideline _pLSQLCopGuideline_2 = new PLSQLCopGuideline(Integer.valueOf(9602), _builder_2.toString(), PLSQLValidator.CRITICAL, 
        PLSQLValidator.INSTRUCTION_RELIABILITY, _createConstantPerIssue_2);
      this.guidelines.put(Integer.valueOf(9602), _pLSQLCopGuideline_2);
      StringConcatenation _builder_3 = new StringConcatenation();
      _builder_3.append("Never reference an unknown table/alias.");
      Remediation _createConstantPerIssue_3 = Remediation.createConstantPerIssue(Integer.valueOf(5));
      PLSQLCopGuideline _pLSQLCopGuideline_3 = new PLSQLCopGuideline(Integer.valueOf(9603), _builder_3.toString(), PLSQLValidator.CRITICAL, 
        PLSQLValidator.INSTRUCTION_RELIABILITY, _createConstantPerIssue_3);
      this.guidelines.put(Integer.valueOf(9603), _pLSQLCopGuideline_3);
      StringConcatenation _builder_4 = new StringConcatenation();
      _builder_4.append("Never use an invalid stats method.");
      Remediation _createConstantPerIssue_4 = Remediation.createConstantPerIssue(Integer.valueOf(5));
      PLSQLCopGuideline _pLSQLCopGuideline_4 = new PLSQLCopGuideline(Integer.valueOf(9604), _builder_4.toString(), PLSQLValidator.CRITICAL, 
        PLSQLValidator.INSTRUCTION_RELIABILITY, _createConstantPerIssue_4);
      this.guidelines.put(Integer.valueOf(9604), _pLSQLCopGuideline_4);
      StringConcatenation _builder_5 = new StringConcatenation();
      _builder_5.append("Never use an invalid stats keyword.");
      Remediation _createConstantPerIssue_5 = Remediation.createConstantPerIssue(Integer.valueOf(5));
      PLSQLCopGuideline _pLSQLCopGuideline_5 = new PLSQLCopGuideline(Integer.valueOf(9605), _builder_5.toString(), PLSQLValidator.CRITICAL, 
        PLSQLValidator.INSTRUCTION_RELIABILITY, _createConstantPerIssue_5);
      this.guidelines.put(Integer.valueOf(9605), _pLSQLCopGuideline_5);
    }
    return this.guidelines;
  }

  private void warning(final int id, final String msg, final HintOrComment h, final int offset, final int length) {
    final ICompositeNode node = NodeModelUtils.getNode(h);
    StringConcatenation _builder = new StringConcatenation();
    String _guidelineId = this.getGuidelineId(Integer.valueOf(id));
    _builder.append(_guidelineId);
    _builder.append(": ");
    String _msg = this.getGuidelines().get(Integer.valueOf(id)).getMsg();
    _builder.append(_msg);
    _builder.append(" ");
    _builder.append(msg);
    int _offset = node.getOffset();
    int _plus = (_offset + 3);
    int _plus_1 = (_plus + offset);
    String _text = h.getText();
    this.acceptWarning(_builder.toString(), h, _plus_1, length, this.getGuidelineId(Integer.valueOf(id)), new String[] { _text });
  }

  private EList<HintOrComment> _getAllHints(final QueryBlock stmt) {
    return stmt.getHints();
  }

  private EList<HintOrComment> _getAllHints(final InsertStatement stmt) {
    return stmt.getHints();
  }

  private EList<HintOrComment> _getAllHints(final UpdateStatement stmt) {
    return stmt.getHints();
  }

  private EList<HintOrComment> _getAllHints(final DeleteStatement stmt) {
    return stmt.getHints();
  }

  private EList<HintOrComment> _getAllHints(final MergeStatement stmt) {
    return stmt.getHints();
  }

  private boolean isFirstHint(final HintOrComment h) {
    final Function1<HintOrComment, Boolean> _function = (HintOrComment it) -> {
      return Boolean.valueOf(this.looksLikeHint(it));
    };
    HintOrComment _findFirst = IterableExtensions.<HintOrComment>findFirst(this.getAllHints(h.eContainer()), _function);
    boolean _tripleEquals = (_findFirst == h);
    if (_tripleEquals) {
      return true;
    }
    return false;
  }

  private boolean looksLikeHint(final HintOrComment h) {
    if (((h.getText().length() > 2) && Objects.equal(h.getText().substring(2, 3), "+"))) {
      return true;
    }
    return false;
  }

  private boolean isHint(final HintOrComment h) {
    boolean _looksLikeHint = this.looksLikeHint(h);
    if (_looksLikeHint) {
      boolean _isFirstHint = this.isFirstHint(h);
      if (_isFirstHint) {
        return true;
      }
      this.warning(Integer.valueOf(9600), h);
      return false;
    }
    return false;
  }

  private String getHintText(final HintOrComment h) {
    int endCommentLength = 0;
    boolean _startsWith = h.getText().startsWith("/*");
    if (_startsWith) {
      endCommentLength = 2;
    }
    String _text = h.getText();
    int _length = h.getText().length();
    int _minus = (_length - endCommentLength);
    return _text.substring(3, _minus);
  }

  private void checkUnknownHint(final HintOrComment h) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("(?i)([^\\s\\(]+)(\\s*\\([^\\(\\)]+\\)*)*");
    final Pattern pattern = Pattern.compile(_builder.toString());
    final Matcher matcher = pattern.matcher(this.getHintText(h));
    while (matcher.find()) {
      {
        final String hint = matcher.group(1);
        boolean _contains = this.ALL_HINTS.contains(hint.toUpperCase());
        boolean _not = (!_contains);
        if (_not) {
          StringConcatenation _builder_1 = new StringConcatenation();
          _builder_1.append("\"");
          _builder_1.append(hint);
          _builder_1.append("\" is unknown.");
          this.warning(9601, _builder_1.toString(), h, matcher.start(1), hint.length());
        }
      }
    }
  }

  private ArrayList<Pair<String, String>> _getTableReference(final QueryBlock stmt) {
    final ArrayList<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
    final List<TableReference> tabrefs = EcoreUtil2.<TableReference>getAllContentsOfType(stmt, TableReference.class);
    for (final TableReference tabref : tabrefs) {
      {
        final QueryBlock parent = EcoreUtil2.<QueryBlock>getContainerOfType(tabref, QueryBlock.class);
        if ((parent == stmt)) {
          QueryTableExpression _queryTableExpression = null;
          if (tabref!=null) {
            _queryTableExpression=tabref.getQueryTableExpression();
          }
          SimpleExpressionNameValue _qteName = null;
          if (_queryTableExpression!=null) {
            _qteName=_queryTableExpression.getQteName();
          }
          String _value = null;
          if (_qteName!=null) {
            _value=_qteName.getValue();
          }
          TableAlias _tableAlias = null;
          if (tabref!=null) {
            _tableAlias=tabref.getTableAlias();
          }
          SimpleExpressionNameValue _alias = null;
          if (_tableAlias!=null) {
            _alias=_tableAlias.getAlias();
          }
          String _value_1 = null;
          if (_alias!=null) {
            _value_1=_alias.getValue();
          }
          Pair<String, String> _pair = new Pair<String, String>(_value, _value_1);
          result.add(_pair);
        }
      }
    }
    return result;
  }

  private ArrayList<Pair<String, String>> _getTableReference(final InsertStatement stmt) {
    final ArrayList<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
    final List<InsertIntoClause> tabrefs = EcoreUtil2.<InsertIntoClause>getAllContentsOfType(stmt, InsertIntoClause.class);
    for (final InsertIntoClause tabref : tabrefs) {
      DmlTableExpressionClause _dmlExpressionClause = null;
      if (tabref!=null) {
        _dmlExpressionClause=tabref.getDmlExpressionClause();
      }
      SimpleExpressionNameValue _dmlName = null;
      if (_dmlExpressionClause!=null) {
        _dmlName=_dmlExpressionClause.getDmlName();
      }
      String _value = null;
      if (_dmlName!=null) {
        _value=_dmlName.getValue();
      }
      SimpleExpressionNameValue _alias = null;
      if (tabref!=null) {
        _alias=tabref.getAlias();
      }
      String _value_1 = null;
      if (_alias!=null) {
        _value_1=_alias.getValue();
      }
      Pair<String, String> _pair = new Pair<String, String>(_value, _value_1);
      result.add(_pair);
    }
    return result;
  }

  private ArrayList<Pair<String, String>> _getTableReference(final UpdateStatement stmt) {
    final ArrayList<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
    DmlTableExpressionClause _dmlTableExpressionClause = stmt.getDmlTableExpressionClause();
    SimpleExpressionNameValue _dmlName = null;
    if (_dmlTableExpressionClause!=null) {
      _dmlName=_dmlTableExpressionClause.getDmlName();
    }
    String _value = null;
    if (_dmlName!=null) {
      _value=_dmlName.getValue();
    }
    SimpleExpressionNameValue _alias = stmt.getAlias();
    String _value_1 = null;
    if (_alias!=null) {
      _value_1=_alias.getValue();
    }
    Pair<String, String> _pair = new Pair<String, String>(_value, _value_1);
    result.add(_pair);
    return result;
  }

  private ArrayList<Pair<String, String>> _getTableReference(final DeleteStatement stmt) {
    final ArrayList<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
    DmlTableExpressionClause _dmlTableExpressionClause = stmt.getDmlTableExpressionClause();
    SimpleExpressionNameValue _dmlName = null;
    if (_dmlTableExpressionClause!=null) {
      _dmlName=_dmlTableExpressionClause.getDmlName();
    }
    String _value = null;
    if (_dmlName!=null) {
      _value=_dmlName.getValue();
    }
    SimpleExpressionNameValue _alias = stmt.getAlias();
    String _value_1 = null;
    if (_alias!=null) {
      _value_1=_alias.getValue();
    }
    Pair<String, String> _pair = new Pair<String, String>(_value, _value_1);
    result.add(_pair);
    return result;
  }

  private ArrayList<Pair<String, String>> _getTableReference(final MergeStatement stmt) {
    final ArrayList<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
    MergeIntoClause _intoClause = stmt.getIntoClause();
    SimpleExpressionNameValue _table = null;
    if (_intoClause!=null) {
      _table=_intoClause.getTable();
    }
    String _value = null;
    if (_table!=null) {
      _value=_table.getValue();
    }
    MergeIntoClause _intoClause_1 = stmt.getIntoClause();
    SimpleExpressionNameValue _alias = null;
    if (_intoClause_1!=null) {
      _alias=_intoClause_1.getAlias();
    }
    String _value_1 = null;
    if (_alias!=null) {
      _value_1=_alias.getValue();
    }
    Pair<String, String> _pair = new Pair<String, String>(_value, _value_1);
    result.add(_pair);
    MergeUsingClause _usingClause = stmt.getUsingClause();
    SimpleExpressionNameValue _table_1 = null;
    if (_usingClause!=null) {
      _table_1=_usingClause.getTable();
    }
    String _value_2 = null;
    if (_table_1!=null) {
      _value_2=_table_1.getValue();
    }
    MergeUsingClause _usingClause_1 = stmt.getUsingClause();
    SimpleExpressionNameValue _alias_1 = null;
    if (_usingClause_1!=null) {
      _alias_1=_usingClause_1.getAlias();
    }
    String _value_3 = null;
    if (_alias_1!=null) {
      _value_3=_alias_1.getValue();
    }
    Pair<String, String> _pair_1 = new Pair<String, String>(_value_2, _value_3);
    result.add(_pair_1);
    return result;
  }

  private ArrayList<Pair<String, String>> getTableReferences(final HintOrComment h) {
    return this.getTableReference(h.eContainer());
  }

  private String getTableAliasOfReferencedTable(final String tabspec, final List<Pair<String, String>> tabrefs) {
    for (final Pair<String, String> tabref : tabrefs) {
      {
        final String tableName = tabref.getKey();
        final String alias = tabref.getValue();
        if (((alias != null) && (tableName != null))) {
          String _lowerCase = tableName.toLowerCase();
          String _lowerCase_1 = tabspec.toLowerCase();
          boolean _equals = Objects.equal(_lowerCase, _lowerCase_1);
          if (_equals) {
            return alias;
          }
        }
      }
    }
    return null;
  }

  private boolean isTableReference(final String tabspec, final List<Pair<String, String>> tabrefs) {
    for (final Pair<String, String> tabref : tabrefs) {
      {
        final String tableName = tabref.getKey();
        final String alias = tabref.getValue();
        String _lowerCase = null;
        if (alias!=null) {
          _lowerCase=alias.toLowerCase();
        }
        String _lowerCase_1 = tabspec.toLowerCase();
        boolean _equals = Objects.equal(_lowerCase, _lowerCase_1);
        if (_equals) {
          return true;
        }
        String _lowerCase_2 = null;
        if (tableName!=null) {
          _lowerCase_2=tableName.toLowerCase();
        }
        String _lowerCase_3 = tabspec.toLowerCase();
        boolean _equals_1 = Objects.equal(_lowerCase_2, _lowerCase_3);
        if (_equals_1) {
          return true;
        }
      }
    }
    return false;
  }

  private void checkTabspec(final HintOrComment h, final String hint, final String tabspec, final int tabspecOffset, final List<Pair<String, String>> tabrefs) {
    if ((tabspec.startsWith(">") || tabspec.contains("@"))) {
    } else {
      final String alias = this.getTableAliasOfReferencedTable(tabspec, tabrefs);
      if ((alias != null)) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Use ");
        _builder.append(alias);
        _builder.append(" instead of ");
        _builder.append(tabspec);
        _builder.append(" in ");
        _builder.append(hint);
        _builder.append(" hint.");
        this.warning(9602, _builder.toString(), h, tabspecOffset, tabspec.length());
      }
      boolean _isTableReference = this.isTableReference(tabspec, tabrefs);
      boolean _not = (!_isTableReference);
      if (_not) {
        StringConcatenation _builder_1 = new StringConcatenation();
        _builder_1.append("(");
        _builder_1.append(tabspec);
        _builder_1.append(" in ");
        _builder_1.append(hint);
        _builder_1.append(" hint).");
        this.warning(9603, _builder_1.toString(), h, tabspecOffset, tabspec.length());
      }
    }
  }

  private void checkLeadingHintSyntaxStyle(final HintOrComment h) {
    final List<String> hints = Arrays.<String>asList("CACHE", "CLUSTER", "DRIVING_SITE", "FACT", "FULL", "HASH", "INMEMORY", 
      "INMEMORY_PRUNING", "LEADING", "NOCACHE", "NO_FACT", "NO_INMEMORY", "NO_INMEMORY_PRUNING", "NOPARALLEL", 
      "NO_PARALLEL", "NO_PQ_SKEW", "NO_USE_BAND", "NO_USE_CUBE", "NO_USE_HASH", "NO_USE_MERGE", "NO_USE_NL", 
      "PQ_DISTRIBUTE", "PQ_SKEW", "PX_JOIN_FILTER", "USE_BAND", "USE_CUBE", "USE_HASH", "USE_MERGE", "USE_NL");
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("(?i)(\\s|\\+)(");
    {
      boolean _hasElements = false;
      for(final String hint : hints) {
        if (!_hasElements) {
          _hasElements = true;
        } else {
          _builder.appendImmediate("|", "");
        }
        _builder.append(hint);
      }
    }
    _builder.append(")\\s*\\((@[^\\s]+)?([^\\)]+)\\)");
    final Pattern pattern = Pattern.compile(_builder.toString());
    final Matcher matcher = pattern.matcher(this.getHintText(h));
    List<Pair<String, String>> tabrefs = null;
    while (matcher.find()) {
      {
        if ((tabrefs == null)) {
          tabrefs = this.getTableReferences(h);
        }
        final String hint_1 = matcher.group(2);
        StringConcatenation _builder_1 = new StringConcatenation();
        _builder_1.append("[^\\s,]+");
        final Pattern tabspecPattern = Pattern.compile(_builder_1.toString());
        final Matcher tabspecMatcher = tabspecPattern.matcher(matcher.group(4));
        while (tabspecMatcher.find()) {
          String _group = tabspecMatcher.group();
          int _start = matcher.start(4);
          int _start_1 = tabspecMatcher.start();
          int _plus = (_start + _start_1);
          this.checkTabspec(h, hint_1, _group, _plus, tabrefs);
        }
      }
    }
  }

  private void checkIgnoreRowOnDupkeyIndexSyntaxStyle(final HintOrComment h) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("(?i)(\\s|\\+)(CHANGE_DUPKEY_ERROR_INDEX|IGNORE_ROW_ON_DUPKEY_INDEX)\\s*\\(\\s*([^, \\(]+)\\s*((,\\s*([^ \\)]+)|(\\([^\\)]+\\))\\s*\\)))");
    final Pattern pattern = Pattern.compile(_builder.toString());
    final Matcher matcher = pattern.matcher(this.getHintText(h));
    List<Pair<String, String>> tabrefs = null;
    while (matcher.find()) {
      {
        if ((tabrefs == null)) {
          tabrefs = this.getTableReferences(h);
        }
        final String hint = matcher.group(2);
        final String tabspec = matcher.group(3);
        this.checkTabspec(h, hint, tabspec, matcher.start(3), tabrefs);
      }
    }
  }

  private void checkDynamicSamplingSyntaxStyle(final HintOrComment h) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("(?i)(\\s|\\+)(DYNAMIC_SAMPLING)\\s*\\(\\s*(@\\s*[^\\s]*)?\\s*([^\\s]*)?\\s*(\\d)+\\)");
    final Pattern pattern = Pattern.compile(_builder.toString());
    final Matcher matcher = pattern.matcher(this.getHintText(h));
    List<Pair<String, String>> tabrefs = null;
    while (matcher.find()) {
      {
        final String tabspec = matcher.group(4);
        if ((tabspec != null)) {
          final String hint = matcher.group(2);
          if ((tabrefs == null)) {
            tabrefs = this.getTableReferences(h);
          }
          this.checkTabspec(h, hint, tabspec, matcher.start(4), tabrefs);
        }
      }
    }
  }

  private void checkIndexSyntaxStyle(final HintOrComment h) {
    final List<String> hints = Arrays.<String>asList("INDEX", "INDEX_ASC", "INDEX_COMBINE", "INDEX_DESC", "INDEX_FFS", "INDEX_JOIN", 
      "INDEX_SS", "INDEX_SS_ASC", "INDEX_SS_DESC", "NO_INDEX", "NO_INDEX_FFS", "NO_INDEX_SS", "NOPARALLEL_INDEX", 
      "NO_PARALLEL_INDEX", "NO_ZONEMAP", "PARALLEL_INDEX", "USE_NL_WITH_INDEX");
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("(?i)(\\s|\\+)(");
    {
      boolean _hasElements = false;
      for(final String hint : hints) {
        if (!_hasElements) {
          _hasElements = true;
        } else {
          _builder.appendImmediate("|", "");
        }
        _builder.append(hint);
      }
    }
    _builder.append(")\\s*\\(\\s*(@\\s*[^\\s]*)?\\s*([^\\s]*)?\\s*([^\\s\\)]+\\s*)*\\)");
    final Pattern pattern = Pattern.compile(_builder.toString());
    final Matcher matcher = pattern.matcher(this.getHintText(h));
    List<Pair<String, String>> tabrefs = null;
    while (matcher.find()) {
      {
        if ((tabrefs == null)) {
          tabrefs = this.getTableReferences(h);
        }
        final String hint_1 = matcher.group(2);
        final String tabspec = matcher.group(4);
        this.checkTabspec(h, hint_1, tabspec, matcher.start(4), tabrefs);
      }
    }
  }

  private void checkMergeSyntaxStyle(final HintOrComment h) {
    final List<String> hints = Arrays.<String>asList("MERGE", "NO_MERGE", "PUSH_PRED", "NO_PUSH_PRED");
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("(?i)(\\s|\\+)(");
    {
      boolean _hasElements = false;
      for(final String hint : hints) {
        if (!_hasElements) {
          _hasElements = true;
        } else {
          _builder.appendImmediate("|", "");
        }
        _builder.append(hint);
      }
    }
    _builder.append(")\\s*(\\(\\s*(@\\s*[^\\s]*)?\\s*([^\\s\\)]*)?\\s*\\))?");
    final Pattern pattern = Pattern.compile(_builder.toString());
    final Matcher matcher = pattern.matcher(this.getHintText(h));
    List<Pair<String, String>> tabrefs = null;
    while (matcher.find()) {
      {
        final String tabspec = matcher.group(5);
        if ((tabspec != null)) {
          final String hint_1 = matcher.group(2);
          if ((tabrefs == null)) {
            tabrefs = this.getTableReferences(h);
          }
          this.checkTabspec(h, hint_1, tabspec, matcher.start(5), tabrefs);
        }
      }
    }
  }

  private boolean isInteger(final String value) {
    try {
      Integer.parseInt(value);
      return true;
    } catch (final Throwable _t) {
      if (_t instanceof NumberFormatException) {
        return false;
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }

  private void checkParallelSyntaxStyle(final HintOrComment h) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("(?i)(\\s|\\+)(PARALLEL)\\s*\\(\\s*(@\\s*[^\\s]*)?\\s*([^\\s,]*)?\\s*,?\\s*([^\\s\\)]+\\s*)?\\)");
    final Pattern pattern = Pattern.compile(_builder.toString());
    final Matcher matcher = pattern.matcher(this.getHintText(h));
    List<Pair<String, String>> tabrefs = null;
    while (matcher.find()) {
      {
        final String tabspec = matcher.group(4);
        if ((((tabspec.equalsIgnoreCase("DEFAULT") || tabspec.equalsIgnoreCase("AUTO")) || 
          tabspec.equalsIgnoreCase("MANUAL")) || this.isInteger(tabspec))) {
        } else {
          final String hint = matcher.group(2);
          if ((tabrefs == null)) {
            tabrefs = this.getTableReferences(h);
          }
          this.checkTabspec(h, hint, tabspec, matcher.start(4), tabrefs);
        }
      }
    }
  }

  private void checkTableStatsStyle(final HintOrComment h) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("(?i)(\\s|\\+)(table_stats)\\s*(\\s*\\((\"?[^\\s\\.\\\"]+\"?(\\s*\\.\\\"?[^\\s\\.\\\"]+\\\"?)?)[\\s,]+([^\\s,]+)([^\\)]*)\\))");
    final Pattern pattern = Pattern.compile(_builder.toString());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("(?i)(([^\\s]+)\\s*=)");
    final Pattern pattern2 = Pattern.compile(_builder_1.toString());
    final Matcher matcher = pattern.matcher(this.getHintText(h));
    while (matcher.find()) {
      {
        final String hint = matcher.group(2);
        final String method = matcher.group(6);
        boolean _not = (!((("DEFAULT".equalsIgnoreCase(method) || "SET".equalsIgnoreCase(method)) || 
          "SCALE".equalsIgnoreCase(method)) || "SAMPLE".equalsIgnoreCase(method)));
        if (_not) {
          StringConcatenation _builder_2 = new StringConcatenation();
          _builder_2.append("(");
          _builder_2.append(method);
          _builder_2.append(" in ");
          _builder_2.append(hint);
          _builder_2.append(" hint).");
          this.warning(9604, _builder_2.toString(), h, matcher.start(6), method.length());
        }
        final String settings = matcher.group(7);
        final Matcher matcher2 = pattern2.matcher(settings);
        while (matcher2.find()) {
          {
            final String keyword = matcher2.group(2);
            boolean _not_1 = (!(("BLOCKS".equalsIgnoreCase(keyword) || "ROWS".equalsIgnoreCase(keyword)) || 
              "ROW_LENGTH".equalsIgnoreCase(keyword)));
            if (_not_1) {
              StringConcatenation _builder_3 = new StringConcatenation();
              _builder_3.append("(");
              _builder_3.append(keyword);
              _builder_3.append(" in ");
              _builder_3.append(hint);
              _builder_3.append(" hint).");
              int _start = matcher.start(7);
              int _start_1 = matcher2.start(2);
              int _plus = (_start + _start_1);
              this.warning(9605, _builder_3.toString(), h, _plus, keyword.length());
            }
          }
        }
      }
    }
  }

  @Check
  public void checkHint(final HintOrComment h) {
    boolean _isHint = this.isHint(h);
    if (_isHint) {
      this.checkUnknownHint(h);
      this.checkLeadingHintSyntaxStyle(h);
      this.checkIgnoreRowOnDupkeyIndexSyntaxStyle(h);
      this.checkDynamicSamplingSyntaxStyle(h);
      this.checkIndexSyntaxStyle(h);
      this.checkMergeSyntaxStyle(h);
      this.checkParallelSyntaxStyle(h);
      this.checkTableStatsStyle(h);
    }
  }

  private EList<HintOrComment> getAllHints(final Notifier stmt) {
    if (stmt instanceof DeleteStatement) {
      return _getAllHints((DeleteStatement)stmt);
    } else if (stmt instanceof InsertStatement) {
      return _getAllHints((InsertStatement)stmt);
    } else if (stmt instanceof MergeStatement) {
      return _getAllHints((MergeStatement)stmt);
    } else if (stmt instanceof UpdateStatement) {
      return _getAllHints((UpdateStatement)stmt);
    } else if (stmt instanceof QueryBlock) {
      return _getAllHints((QueryBlock)stmt);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(stmt).toString());
    }
  }

  private ArrayList<Pair<String, String>> getTableReference(final Notifier stmt) {
    if (stmt instanceof DeleteStatement) {
      return _getTableReference((DeleteStatement)stmt);
    } else if (stmt instanceof InsertStatement) {
      return _getTableReference((InsertStatement)stmt);
    } else if (stmt instanceof MergeStatement) {
      return _getTableReference((MergeStatement)stmt);
    } else if (stmt instanceof UpdateStatement) {
      return _getTableReference((UpdateStatement)stmt);
    } else if (stmt instanceof QueryBlock) {
      return _getTableReference((QueryBlock)stmt);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(stmt).toString());
    }
  }
}
