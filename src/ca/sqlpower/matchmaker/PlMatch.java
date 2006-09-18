package ca.sqlpower.matchmaker;
// Generated Sep 18, 2006 4:34:38 PM by Hibernate Tools 3.2.0.beta7


import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * PlMatch generated by hbm2java
 */
public class PlMatch  implements java.io.Serializable {

    // Fields    

     private String matchId;
     private String matchDesc;
     private String tableOwner;
     private String matchTable;
     private String pkColumn;
     private String filter;
     private String resultsTable;
     private Date createDate;
     private Date lastUpdateDate;
     private String lastUpdateUser;
     private String sequenceName;
     private boolean compileFlag;
     private String mergeScriptFileName;
     private Short autoMatchThreshold;
     private Date mergeCompletionDate;
     private String mergeLastUser;
     private String mergeRunStatus;
     private String mergeDesc;
     private String matchLogFileName;
     private boolean matchAppendToLogInd;
     private BigDecimal matchProcessCnt;
     private BigDecimal matchShowProgressFreq;
     private boolean matchDebugModeInd;
     private String matchRollbackSegmentName;
     private String mergeLogFileName;
     private boolean mergeAppendToLogInd;
     private BigDecimal mergeProcessCnt;
     private BigDecimal mergeShowProgressFreq;
     private boolean mergeDebugModeInd;
     private String mergeRollbackSegmentName;
     private boolean mergeAugmentNullInd;
     private String matchRunStatus;
     private String matchScriptFileName;
     private BigDecimal matchTotalSteps;
     private BigDecimal matchStepsCompleted;
     private BigDecimal matchRowsInserted;
     private Date matchLastRunDate;
     private String matchLastRunUser;
     private BigDecimal mergeTotalSteps;
     private BigDecimal mergeStepsCompleted;
     private Date mergeLastRunDate;
     private String mergeLastRunUser;
     private String matchPackageName;
     private String matchProcedureNameAll;
     private String matchProcedureNameOne;
     private String mergePackageName;
     private String mergeProcedureName;
     private String matchTablePkColumnFormat;
     private BigDecimal mergeRowsInserted;
     private String batchFileName;
     private String selectClause;
     private String fromClause;
     private String whereClause;
     private String resultsTableOwner;
     private boolean matchBreakInd;
     private String filterCriteria;
     private String matchType;
     private String lastUpdateOsUser;
     private String matchStepDesc;
     private String mergeStepDesc;
     private boolean mergeTablesBackupInd;
     private String matchStatus;
     private BigDecimal lastBackupNo;
     private boolean checkedOutInd;
     private Date checkedOutDate;
     private String checkedOutUser;
     private String checkedOutOsUser;
     private String indexColumnName0;
     private String indexColumnName1;
     private String indexColumnName2;
     private String indexColumnName3;
     private String indexColumnName4;
     private String indexColumnName5;
     private String indexColumnName6;
     private String indexColumnName7;
     private String indexColumnName8;
     private String indexColumnName9;
     private String tempSourceTableName;
     private String tempCandDupTableName;
     private String fromClauseDb;
     private String indexColumnType0;
     private String indexColumnType1;
     private String indexColumnType2;
     private String indexColumnType3;
     private String indexColumnType4;
     private String indexColumnType5;
     private String indexColumnType6;
     private String indexColumnType7;
     private String indexColumnType8;
     private String indexColumnType9;
     private boolean truncateCandDupInd;
     private boolean matchSendEmailInd;
     private boolean mergeSendEmailInd;
     private String xrefOwner;
     private String xrefTableName;
     private boolean autoMatchActiveInd;
     private Set plMergeConsolidateCriterias = new HashSet(0);
     private Set plMatchXrefMaps = new HashSet(0);
     private Set plMergeCriterias = new HashSet(0);
     private Set plMatchGroups = new HashSet(0);

     // Constructors

    /** default constructor */
    public PlMatch() {
    }

	/** minimal constructor */
    public PlMatch(String matchId, String matchType) {
        this.matchId = matchId;
        this.matchType = matchType;
    }
    /** full constructor */
    public PlMatch(String matchId, String matchDesc, String tableOwner, String matchTable, String pkColumn, String filter, String resultsTable, Date createDate, Date lastUpdateDate, String lastUpdateUser, String sequenceName, boolean compileFlag, String mergeScriptFileName, Short autoMatchThreshold, Date mergeCompletionDate, String mergeLastUser, String mergeRunStatus, String mergeDesc, String matchLogFileName, boolean matchAppendToLogInd, BigDecimal matchProcessCnt, BigDecimal matchShowProgressFreq, boolean matchDebugModeInd, String matchRollbackSegmentName, String mergeLogFileName, boolean mergeAppendToLogInd, BigDecimal mergeProcessCnt, BigDecimal mergeShowProgressFreq, boolean mergeDebugModeInd, String mergeRollbackSegmentName, boolean mergeAugmentNullInd, String matchRunStatus, String matchScriptFileName, BigDecimal matchTotalSteps, BigDecimal matchStepsCompleted, BigDecimal matchRowsInserted, Date matchLastRunDate, String matchLastRunUser, BigDecimal mergeTotalSteps, BigDecimal mergeStepsCompleted, Date mergeLastRunDate, String mergeLastRunUser, String matchPackageName, String matchProcedureNameAll, String matchProcedureNameOne, String mergePackageName, String mergeProcedureName, String matchTablePkColumnFormat, BigDecimal mergeRowsInserted, String batchFileName, String selectClause, String fromClause, String whereClause, String resultsTableOwner, boolean matchBreakInd, String filterCriteria, String matchType, String lastUpdateOsUser, String matchStepDesc, String mergeStepDesc, boolean mergeTablesBackupInd, String matchStatus, BigDecimal lastBackupNo, boolean checkedOutInd, Date checkedOutDate, String checkedOutUser, String checkedOutOsUser, String indexColumnName0, String indexColumnName1, String indexColumnName2, String indexColumnName3, String indexColumnName4, String indexColumnName5, String indexColumnName6, String indexColumnName7, String indexColumnName8, String indexColumnName9, String tempSourceTableName, String tempCandDupTableName, String fromClauseDb, String indexColumnType0, String indexColumnType1, String indexColumnType2, String indexColumnType3, String indexColumnType4, String indexColumnType5, String indexColumnType6, String indexColumnType7, String indexColumnType8, String indexColumnType9, boolean truncateCandDupInd, boolean matchSendEmailInd, boolean mergeSendEmailInd, String xrefOwner, String xrefTableName, boolean autoMatchActiveInd, Set plMergeConsolidateCriterias, Set plMatchXrefMaps, Set plMergeCriterias, Set plMatchGroups) {
       this.matchId = matchId;
       this.matchDesc = matchDesc;
       this.tableOwner = tableOwner;
       this.matchTable = matchTable;
       this.pkColumn = pkColumn;
       this.filter = filter;
       this.resultsTable = resultsTable;
       this.createDate = createDate;
       this.lastUpdateDate = lastUpdateDate;
       this.lastUpdateUser = lastUpdateUser;
       this.sequenceName = sequenceName;
       this.compileFlag = compileFlag;
       this.mergeScriptFileName = mergeScriptFileName;
       this.autoMatchThreshold = autoMatchThreshold;
       this.mergeCompletionDate = mergeCompletionDate;
       this.mergeLastUser = mergeLastUser;
       this.mergeRunStatus = mergeRunStatus;
       this.mergeDesc = mergeDesc;
       this.matchLogFileName = matchLogFileName;
       this.matchAppendToLogInd = matchAppendToLogInd;
       this.matchProcessCnt = matchProcessCnt;
       this.matchShowProgressFreq = matchShowProgressFreq;
       this.matchDebugModeInd = matchDebugModeInd;
       this.matchRollbackSegmentName = matchRollbackSegmentName;
       this.mergeLogFileName = mergeLogFileName;
       this.mergeAppendToLogInd = mergeAppendToLogInd;
       this.mergeProcessCnt = mergeProcessCnt;
       this.mergeShowProgressFreq = mergeShowProgressFreq;
       this.mergeDebugModeInd = mergeDebugModeInd;
       this.mergeRollbackSegmentName = mergeRollbackSegmentName;
       this.mergeAugmentNullInd = mergeAugmentNullInd;
       this.matchRunStatus = matchRunStatus;
       this.matchScriptFileName = matchScriptFileName;
       this.matchTotalSteps = matchTotalSteps;
       this.matchStepsCompleted = matchStepsCompleted;
       this.matchRowsInserted = matchRowsInserted;
       this.matchLastRunDate = matchLastRunDate;
       this.matchLastRunUser = matchLastRunUser;
       this.mergeTotalSteps = mergeTotalSteps;
       this.mergeStepsCompleted = mergeStepsCompleted;
       this.mergeLastRunDate = mergeLastRunDate;
       this.mergeLastRunUser = mergeLastRunUser;
       this.matchPackageName = matchPackageName;
       this.matchProcedureNameAll = matchProcedureNameAll;
       this.matchProcedureNameOne = matchProcedureNameOne;
       this.mergePackageName = mergePackageName;
       this.mergeProcedureName = mergeProcedureName;
       this.matchTablePkColumnFormat = matchTablePkColumnFormat;
       this.mergeRowsInserted = mergeRowsInserted;
       this.batchFileName = batchFileName;
       this.selectClause = selectClause;
       this.fromClause = fromClause;
       this.whereClause = whereClause;
       this.resultsTableOwner = resultsTableOwner;
       this.matchBreakInd = matchBreakInd;
       this.filterCriteria = filterCriteria;
       this.matchType = matchType;
       this.lastUpdateOsUser = lastUpdateOsUser;
       this.matchStepDesc = matchStepDesc;
       this.mergeStepDesc = mergeStepDesc;
       this.mergeTablesBackupInd = mergeTablesBackupInd;
       this.matchStatus = matchStatus;
       this.lastBackupNo = lastBackupNo;
       this.checkedOutInd = checkedOutInd;
       this.checkedOutDate = checkedOutDate;
       this.checkedOutUser = checkedOutUser;
       this.checkedOutOsUser = checkedOutOsUser;
       this.indexColumnName0 = indexColumnName0;
       this.indexColumnName1 = indexColumnName1;
       this.indexColumnName2 = indexColumnName2;
       this.indexColumnName3 = indexColumnName3;
       this.indexColumnName4 = indexColumnName4;
       this.indexColumnName5 = indexColumnName5;
       this.indexColumnName6 = indexColumnName6;
       this.indexColumnName7 = indexColumnName7;
       this.indexColumnName8 = indexColumnName8;
       this.indexColumnName9 = indexColumnName9;
       this.tempSourceTableName = tempSourceTableName;
       this.tempCandDupTableName = tempCandDupTableName;
       this.fromClauseDb = fromClauseDb;
       this.indexColumnType0 = indexColumnType0;
       this.indexColumnType1 = indexColumnType1;
       this.indexColumnType2 = indexColumnType2;
       this.indexColumnType3 = indexColumnType3;
       this.indexColumnType4 = indexColumnType4;
       this.indexColumnType5 = indexColumnType5;
       this.indexColumnType6 = indexColumnType6;
       this.indexColumnType7 = indexColumnType7;
       this.indexColumnType8 = indexColumnType8;
       this.indexColumnType9 = indexColumnType9;
       this.truncateCandDupInd = truncateCandDupInd;
       this.matchSendEmailInd = matchSendEmailInd;
       this.mergeSendEmailInd = mergeSendEmailInd;
       this.xrefOwner = xrefOwner;
       this.xrefTableName = xrefTableName;
       this.autoMatchActiveInd = autoMatchActiveInd;
       this.plMergeConsolidateCriterias = plMergeConsolidateCriterias;
       this.plMatchXrefMaps = plMatchXrefMaps;
       this.plMergeCriterias = plMergeCriterias;
       this.plMatchGroups = plMatchGroups;
    }
   
    // Property accessors
    public String getMatchId() {
        return this.matchId;
    }
    
    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }
    public String getMatchDesc() {
        return this.matchDesc;
    }
    
    public void setMatchDesc(String matchDesc) {
        this.matchDesc = matchDesc;
    }
    public String getTableOwner() {
        return this.tableOwner;
    }
    
    public void setTableOwner(String tableOwner) {
        this.tableOwner = tableOwner;
    }
    public String getMatchTable() {
        return this.matchTable;
    }
    
    public void setMatchTable(String matchTable) {
        this.matchTable = matchTable;
    }
    public String getPkColumn() {
        return this.pkColumn;
    }
    
    public void setPkColumn(String pkColumn) {
        this.pkColumn = pkColumn;
    }
    public String getFilter() {
        return this.filter;
    }
    
    public void setFilter(String filter) {
        this.filter = filter;
    }
    public String getResultsTable() {
        return this.resultsTable;
    }
    
    public void setResultsTable(String resultsTable) {
        this.resultsTable = resultsTable;
    }
    public Date getCreateDate() {
        return this.createDate;
    }
    
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
    public Date getLastUpdateDate() {
        return this.lastUpdateDate;
    }
    
    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }
    public String getLastUpdateUser() {
        return this.lastUpdateUser;
    }
    
    public void setLastUpdateUser(String lastUpdateUser) {
        this.lastUpdateUser = lastUpdateUser;
    }
    public String getSequenceName() {
        return this.sequenceName;
    }
    
    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }
    public boolean isCompileFlag() {
        return this.compileFlag;
    }
    
    public void setCompileFlag(boolean compileFlag) {
        this.compileFlag = compileFlag;
    }
    public String getMergeScriptFileName() {
        return this.mergeScriptFileName;
    }
    
    public void setMergeScriptFileName(String mergeScriptFileName) {
        this.mergeScriptFileName = mergeScriptFileName;
    }
    public Short getAutoMatchThreshold() {
        return this.autoMatchThreshold;
    }
    
    public void setAutoMatchThreshold(Short autoMatchThreshold) {
        this.autoMatchThreshold = autoMatchThreshold;
    }
    public Date getMergeCompletionDate() {
        return this.mergeCompletionDate;
    }
    
    public void setMergeCompletionDate(Date mergeCompletionDate) {
        this.mergeCompletionDate = mergeCompletionDate;
    }
    public String getMergeLastUser() {
        return this.mergeLastUser;
    }
    
    public void setMergeLastUser(String mergeLastUser) {
        this.mergeLastUser = mergeLastUser;
    }
    public String getMergeRunStatus() {
        return this.mergeRunStatus;
    }
    
    public void setMergeRunStatus(String mergeRunStatus) {
        this.mergeRunStatus = mergeRunStatus;
    }
    public String getMergeDesc() {
        return this.mergeDesc;
    }
    
    public void setMergeDesc(String mergeDesc) {
        this.mergeDesc = mergeDesc;
    }
    public String getMatchLogFileName() {
        return this.matchLogFileName;
    }
    
    public void setMatchLogFileName(String matchLogFileName) {
        this.matchLogFileName = matchLogFileName;
    }
    public boolean isMatchAppendToLogInd() {
        return this.matchAppendToLogInd;
    }
    
    public void setMatchAppendToLogInd(boolean matchAppendToLogInd) {
        this.matchAppendToLogInd = matchAppendToLogInd;
    }
    public BigDecimal getMatchProcessCnt() {
        return this.matchProcessCnt;
    }
    
    public void setMatchProcessCnt(BigDecimal matchProcessCnt) {
        this.matchProcessCnt = matchProcessCnt;
    }
    public BigDecimal getMatchShowProgressFreq() {
        return this.matchShowProgressFreq;
    }
    
    public void setMatchShowProgressFreq(BigDecimal matchShowProgressFreq) {
        this.matchShowProgressFreq = matchShowProgressFreq;
    }
    public boolean isMatchDebugModeInd() {
        return this.matchDebugModeInd;
    }
    
    public void setMatchDebugModeInd(boolean matchDebugModeInd) {
        this.matchDebugModeInd = matchDebugModeInd;
    }
    public String getMatchRollbackSegmentName() {
        return this.matchRollbackSegmentName;
    }
    
    public void setMatchRollbackSegmentName(String matchRollbackSegmentName) {
        this.matchRollbackSegmentName = matchRollbackSegmentName;
    }
    public String getMergeLogFileName() {
        return this.mergeLogFileName;
    }
    
    public void setMergeLogFileName(String mergeLogFileName) {
        this.mergeLogFileName = mergeLogFileName;
    }
    public boolean isMergeAppendToLogInd() {
        return this.mergeAppendToLogInd;
    }
    
    public void setMergeAppendToLogInd(boolean mergeAppendToLogInd) {
        this.mergeAppendToLogInd = mergeAppendToLogInd;
    }
    public BigDecimal getMergeProcessCnt() {
        return this.mergeProcessCnt;
    }
    
    public void setMergeProcessCnt(BigDecimal mergeProcessCnt) {
        this.mergeProcessCnt = mergeProcessCnt;
    }
    public BigDecimal getMergeShowProgressFreq() {
        return this.mergeShowProgressFreq;
    }
    
    public void setMergeShowProgressFreq(BigDecimal mergeShowProgressFreq) {
        this.mergeShowProgressFreq = mergeShowProgressFreq;
    }
    public boolean isMergeDebugModeInd() {
        return this.mergeDebugModeInd;
    }
    
    public void setMergeDebugModeInd(boolean mergeDebugModeInd) {
        this.mergeDebugModeInd = mergeDebugModeInd;
    }
    public String getMergeRollbackSegmentName() {
        return this.mergeRollbackSegmentName;
    }
    
    public void setMergeRollbackSegmentName(String mergeRollbackSegmentName) {
        this.mergeRollbackSegmentName = mergeRollbackSegmentName;
    }
    public boolean isMergeAugmentNullInd() {
        return this.mergeAugmentNullInd;
    }
    
    public void setMergeAugmentNullInd(boolean mergeAugmentNullInd) {
        this.mergeAugmentNullInd = mergeAugmentNullInd;
    }
    public String getMatchRunStatus() {
        return this.matchRunStatus;
    }
    
    public void setMatchRunStatus(String matchRunStatus) {
        this.matchRunStatus = matchRunStatus;
    }
    public String getMatchScriptFileName() {
        return this.matchScriptFileName;
    }
    
    public void setMatchScriptFileName(String matchScriptFileName) {
        this.matchScriptFileName = matchScriptFileName;
    }
    public BigDecimal getMatchTotalSteps() {
        return this.matchTotalSteps;
    }
    
    public void setMatchTotalSteps(BigDecimal matchTotalSteps) {
        this.matchTotalSteps = matchTotalSteps;
    }
    public BigDecimal getMatchStepsCompleted() {
        return this.matchStepsCompleted;
    }
    
    public void setMatchStepsCompleted(BigDecimal matchStepsCompleted) {
        this.matchStepsCompleted = matchStepsCompleted;
    }
    public BigDecimal getMatchRowsInserted() {
        return this.matchRowsInserted;
    }
    
    public void setMatchRowsInserted(BigDecimal matchRowsInserted) {
        this.matchRowsInserted = matchRowsInserted;
    }
    public Date getMatchLastRunDate() {
        return this.matchLastRunDate;
    }
    
    public void setMatchLastRunDate(Date matchLastRunDate) {
        this.matchLastRunDate = matchLastRunDate;
    }
    public String getMatchLastRunUser() {
        return this.matchLastRunUser;
    }
    
    public void setMatchLastRunUser(String matchLastRunUser) {
        this.matchLastRunUser = matchLastRunUser;
    }
    public BigDecimal getMergeTotalSteps() {
        return this.mergeTotalSteps;
    }
    
    public void setMergeTotalSteps(BigDecimal mergeTotalSteps) {
        this.mergeTotalSteps = mergeTotalSteps;
    }
    public BigDecimal getMergeStepsCompleted() {
        return this.mergeStepsCompleted;
    }
    
    public void setMergeStepsCompleted(BigDecimal mergeStepsCompleted) {
        this.mergeStepsCompleted = mergeStepsCompleted;
    }
    public Date getMergeLastRunDate() {
        return this.mergeLastRunDate;
    }
    
    public void setMergeLastRunDate(Date mergeLastRunDate) {
        this.mergeLastRunDate = mergeLastRunDate;
    }
    public String getMergeLastRunUser() {
        return this.mergeLastRunUser;
    }
    
    public void setMergeLastRunUser(String mergeLastRunUser) {
        this.mergeLastRunUser = mergeLastRunUser;
    }
    public String getMatchPackageName() {
        return this.matchPackageName;
    }
    
    public void setMatchPackageName(String matchPackageName) {
        this.matchPackageName = matchPackageName;
    }
    public String getMatchProcedureNameAll() {
        return this.matchProcedureNameAll;
    }
    
    public void setMatchProcedureNameAll(String matchProcedureNameAll) {
        this.matchProcedureNameAll = matchProcedureNameAll;
    }
    public String getMatchProcedureNameOne() {
        return this.matchProcedureNameOne;
    }
    
    public void setMatchProcedureNameOne(String matchProcedureNameOne) {
        this.matchProcedureNameOne = matchProcedureNameOne;
    }
    public String getMergePackageName() {
        return this.mergePackageName;
    }
    
    public void setMergePackageName(String mergePackageName) {
        this.mergePackageName = mergePackageName;
    }
    public String getMergeProcedureName() {
        return this.mergeProcedureName;
    }
    
    public void setMergeProcedureName(String mergeProcedureName) {
        this.mergeProcedureName = mergeProcedureName;
    }
    public String getMatchTablePkColumnFormat() {
        return this.matchTablePkColumnFormat;
    }
    
    public void setMatchTablePkColumnFormat(String matchTablePkColumnFormat) {
        this.matchTablePkColumnFormat = matchTablePkColumnFormat;
    }
    public BigDecimal getMergeRowsInserted() {
        return this.mergeRowsInserted;
    }
    
    public void setMergeRowsInserted(BigDecimal mergeRowsInserted) {
        this.mergeRowsInserted = mergeRowsInserted;
    }
    public String getBatchFileName() {
        return this.batchFileName;
    }
    
    public void setBatchFileName(String batchFileName) {
        this.batchFileName = batchFileName;
    }
    public String getSelectClause() {
        return this.selectClause;
    }
    
    public void setSelectClause(String selectClause) {
        this.selectClause = selectClause;
    }
    public String getFromClause() {
        return this.fromClause;
    }
    
    public void setFromClause(String fromClause) {
        this.fromClause = fromClause;
    }
    public String getWhereClause() {
        return this.whereClause;
    }
    
    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }
    public String getResultsTableOwner() {
        return this.resultsTableOwner;
    }
    
    public void setResultsTableOwner(String resultsTableOwner) {
        this.resultsTableOwner = resultsTableOwner;
    }
    public boolean isMatchBreakInd() {
        return this.matchBreakInd;
    }
    
    public void setMatchBreakInd(boolean matchBreakInd) {
        this.matchBreakInd = matchBreakInd;
    }
    public String getFilterCriteria() {
        return this.filterCriteria;
    }
    
    public void setFilterCriteria(String filterCriteria) {
        this.filterCriteria = filterCriteria;
    }
    public String getMatchType() {
        return this.matchType;
    }
    
    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }
    public String getLastUpdateOsUser() {
        return this.lastUpdateOsUser;
    }
    
    public void setLastUpdateOsUser(String lastUpdateOsUser) {
        this.lastUpdateOsUser = lastUpdateOsUser;
    }
    public String getMatchStepDesc() {
        return this.matchStepDesc;
    }
    
    public void setMatchStepDesc(String matchStepDesc) {
        this.matchStepDesc = matchStepDesc;
    }
    public String getMergeStepDesc() {
        return this.mergeStepDesc;
    }
    
    public void setMergeStepDesc(String mergeStepDesc) {
        this.mergeStepDesc = mergeStepDesc;
    }
    public boolean isMergeTablesBackupInd() {
        return this.mergeTablesBackupInd;
    }
    
    public void setMergeTablesBackupInd(boolean mergeTablesBackupInd) {
        this.mergeTablesBackupInd = mergeTablesBackupInd;
    }
    public String getMatchStatus() {
        return this.matchStatus;
    }
    
    public void setMatchStatus(String matchStatus) {
        this.matchStatus = matchStatus;
    }
    public BigDecimal getLastBackupNo() {
        return this.lastBackupNo;
    }
    
    public void setLastBackupNo(BigDecimal lastBackupNo) {
        this.lastBackupNo = lastBackupNo;
    }
    public boolean isCheckedOutInd() {
        return this.checkedOutInd;
    }
    
    public void setCheckedOutInd(boolean checkedOutInd) {
        this.checkedOutInd = checkedOutInd;
    }
    public Date getCheckedOutDate() {
        return this.checkedOutDate;
    }
    
    public void setCheckedOutDate(Date checkedOutDate) {
        this.checkedOutDate = checkedOutDate;
    }
    public String getCheckedOutUser() {
        return this.checkedOutUser;
    }
    
    public void setCheckedOutUser(String checkedOutUser) {
        this.checkedOutUser = checkedOutUser;
    }
    public String getCheckedOutOsUser() {
        return this.checkedOutOsUser;
    }
    
    public void setCheckedOutOsUser(String checkedOutOsUser) {
        this.checkedOutOsUser = checkedOutOsUser;
    }
    public String getIndexColumnName0() {
        return this.indexColumnName0;
    }
    
    public void setIndexColumnName0(String indexColumnName0) {
        this.indexColumnName0 = indexColumnName0;
    }
    public String getIndexColumnName1() {
        return this.indexColumnName1;
    }
    
    public void setIndexColumnName1(String indexColumnName1) {
        this.indexColumnName1 = indexColumnName1;
    }
    public String getIndexColumnName2() {
        return this.indexColumnName2;
    }
    
    public void setIndexColumnName2(String indexColumnName2) {
        this.indexColumnName2 = indexColumnName2;
    }
    public String getIndexColumnName3() {
        return this.indexColumnName3;
    }
    
    public void setIndexColumnName3(String indexColumnName3) {
        this.indexColumnName3 = indexColumnName3;
    }
    public String getIndexColumnName4() {
        return this.indexColumnName4;
    }
    
    public void setIndexColumnName4(String indexColumnName4) {
        this.indexColumnName4 = indexColumnName4;
    }
    public String getIndexColumnName5() {
        return this.indexColumnName5;
    }
    
    public void setIndexColumnName5(String indexColumnName5) {
        this.indexColumnName5 = indexColumnName5;
    }
    public String getIndexColumnName6() {
        return this.indexColumnName6;
    }
    
    public void setIndexColumnName6(String indexColumnName6) {
        this.indexColumnName6 = indexColumnName6;
    }
    public String getIndexColumnName7() {
        return this.indexColumnName7;
    }
    
    public void setIndexColumnName7(String indexColumnName7) {
        this.indexColumnName7 = indexColumnName7;
    }
    public String getIndexColumnName8() {
        return this.indexColumnName8;
    }
    
    public void setIndexColumnName8(String indexColumnName8) {
        this.indexColumnName8 = indexColumnName8;
    }
    public String getIndexColumnName9() {
        return this.indexColumnName9;
    }
    
    public void setIndexColumnName9(String indexColumnName9) {
        this.indexColumnName9 = indexColumnName9;
    }
    public String getTempSourceTableName() {
        return this.tempSourceTableName;
    }
    
    public void setTempSourceTableName(String tempSourceTableName) {
        this.tempSourceTableName = tempSourceTableName;
    }
    public String getTempCandDupTableName() {
        return this.tempCandDupTableName;
    }
    
    public void setTempCandDupTableName(String tempCandDupTableName) {
        this.tempCandDupTableName = tempCandDupTableName;
    }
    public String getFromClauseDb() {
        return this.fromClauseDb;
    }
    
    public void setFromClauseDb(String fromClauseDb) {
        this.fromClauseDb = fromClauseDb;
    }
    public String getIndexColumnType0() {
        return this.indexColumnType0;
    }
    
    public void setIndexColumnType0(String indexColumnType0) {
        this.indexColumnType0 = indexColumnType0;
    }
    public String getIndexColumnType1() {
        return this.indexColumnType1;
    }
    
    public void setIndexColumnType1(String indexColumnType1) {
        this.indexColumnType1 = indexColumnType1;
    }
    public String getIndexColumnType2() {
        return this.indexColumnType2;
    }
    
    public void setIndexColumnType2(String indexColumnType2) {
        this.indexColumnType2 = indexColumnType2;
    }
    public String getIndexColumnType3() {
        return this.indexColumnType3;
    }
    
    public void setIndexColumnType3(String indexColumnType3) {
        this.indexColumnType3 = indexColumnType3;
    }
    public String getIndexColumnType4() {
        return this.indexColumnType4;
    }
    
    public void setIndexColumnType4(String indexColumnType4) {
        this.indexColumnType4 = indexColumnType4;
    }
    public String getIndexColumnType5() {
        return this.indexColumnType5;
    }
    
    public void setIndexColumnType5(String indexColumnType5) {
        this.indexColumnType5 = indexColumnType5;
    }
    public String getIndexColumnType6() {
        return this.indexColumnType6;
    }
    
    public void setIndexColumnType6(String indexColumnType6) {
        this.indexColumnType6 = indexColumnType6;
    }
    public String getIndexColumnType7() {
        return this.indexColumnType7;
    }
    
    public void setIndexColumnType7(String indexColumnType7) {
        this.indexColumnType7 = indexColumnType7;
    }
    public String getIndexColumnType8() {
        return this.indexColumnType8;
    }
    
    public void setIndexColumnType8(String indexColumnType8) {
        this.indexColumnType8 = indexColumnType8;
    }
    public String getIndexColumnType9() {
        return this.indexColumnType9;
    }
    
    public void setIndexColumnType9(String indexColumnType9) {
        this.indexColumnType9 = indexColumnType9;
    }
    public boolean isTruncateCandDupInd() {
        return this.truncateCandDupInd;
    }
    
    public void setTruncateCandDupInd(boolean truncateCandDupInd) {
        this.truncateCandDupInd = truncateCandDupInd;
    }
    public boolean isMatchSendEmailInd() {
        return this.matchSendEmailInd;
    }
    
    public void setMatchSendEmailInd(boolean matchSendEmailInd) {
        this.matchSendEmailInd = matchSendEmailInd;
    }
    public boolean isMergeSendEmailInd() {
        return this.mergeSendEmailInd;
    }
    
    public void setMergeSendEmailInd(boolean mergeSendEmailInd) {
        this.mergeSendEmailInd = mergeSendEmailInd;
    }
    public String getXrefOwner() {
        return this.xrefOwner;
    }
    
    public void setXrefOwner(String xrefOwner) {
        this.xrefOwner = xrefOwner;
    }
    public String getXrefTableName() {
        return this.xrefTableName;
    }
    
    public void setXrefTableName(String xrefTableName) {
        this.xrefTableName = xrefTableName;
    }
    public boolean isAutoMatchActiveInd() {
        return this.autoMatchActiveInd;
    }
    
    public void setAutoMatchActiveInd(boolean autoMatchActiveInd) {
        this.autoMatchActiveInd = autoMatchActiveInd;
    }
    public Set getPlMergeConsolidateCriterias() {
        return this.plMergeConsolidateCriterias;
    }
    
    public void setPlMergeConsolidateCriterias(Set plMergeConsolidateCriterias) {
        this.plMergeConsolidateCriterias = plMergeConsolidateCriterias;
    }
    public Set getPlMatchXrefMaps() {
        return this.plMatchXrefMaps;
    }
    
    public void setPlMatchXrefMaps(Set plMatchXrefMaps) {
        this.plMatchXrefMaps = plMatchXrefMaps;
    }
    public Set getPlMergeCriterias() {
        return this.plMergeCriterias;
    }
    
    public void setPlMergeCriterias(Set plMergeCriterias) {
        this.plMergeCriterias = plMergeCriterias;
    }
    public Set getPlMatchGroups() {
        return this.plMatchGroups;
    }
    
    public void setPlMatchGroups(Set plMatchGroups) {
        this.plMatchGroups = plMatchGroups;
    }




}


