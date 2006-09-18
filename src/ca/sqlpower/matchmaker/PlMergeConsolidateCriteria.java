package ca.sqlpower.matchmaker;
// Generated Sep 18, 2006 4:34:38 PM by Hibernate Tools 3.2.0.beta7


import java.math.BigDecimal;
import java.util.Date;

/**
 * PlMergeConsolidateCriteria generated by hbm2java
 */
public class PlMergeConsolidateCriteria  implements java.io.Serializable {

    // Fields    

     private PlMergeConsolidateCriteriaId id;
     private PlMatch plMatch;
     private String columnFormat;
     private BigDecimal columnLength;
     private boolean canUpdateActionInd;
     private String actionType;
     private Date lastUpdateDate;
     private String lastUpdateUser;
     private String lastUpdateOsUser;

     // Constructors

    /** default constructor */
    public PlMergeConsolidateCriteria() {
    }

	/** minimal constructor */
    public PlMergeConsolidateCriteria(PlMergeConsolidateCriteriaId id, PlMatch plMatch) {
        this.id = id;
        this.plMatch = plMatch;
    }
    /** full constructor */
    public PlMergeConsolidateCriteria(PlMergeConsolidateCriteriaId id, PlMatch plMatch, String columnFormat, BigDecimal columnLength, boolean canUpdateActionInd, String actionType, Date lastUpdateDate, String lastUpdateUser, String lastUpdateOsUser) {
       this.id = id;
       this.plMatch = plMatch;
       this.columnFormat = columnFormat;
       this.columnLength = columnLength;
       this.canUpdateActionInd = canUpdateActionInd;
       this.actionType = actionType;
       this.lastUpdateDate = lastUpdateDate;
       this.lastUpdateUser = lastUpdateUser;
       this.lastUpdateOsUser = lastUpdateOsUser;
    }
   
    // Property accessors
    public PlMergeConsolidateCriteriaId getId() {
        return this.id;
    }
    
    public void setId(PlMergeConsolidateCriteriaId id) {
        this.id = id;
    }
    public PlMatch getPlMatch() {
        return this.plMatch;
    }
    
    public void setPlMatch(PlMatch plMatch) {
        this.plMatch = plMatch;
    }
    public String getColumnFormat() {
        return this.columnFormat;
    }
    
    public void setColumnFormat(String columnFormat) {
        this.columnFormat = columnFormat;
    }
    public BigDecimal getColumnLength() {
        return this.columnLength;
    }
    
    public void setColumnLength(BigDecimal columnLength) {
        this.columnLength = columnLength;
    }
    public boolean isCanUpdateActionInd() {
        return this.canUpdateActionInd;
    }
    
    public void setCanUpdateActionInd(boolean canUpdateActionInd) {
        this.canUpdateActionInd = canUpdateActionInd;
    }
    public String getActionType() {
        return this.actionType;
    }
    
    public void setActionType(String actionType) {
        this.actionType = actionType;
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
    public String getLastUpdateOsUser() {
        return this.lastUpdateOsUser;
    }
    
    public void setLastUpdateOsUser(String lastUpdateOsUser) {
        this.lastUpdateOsUser = lastUpdateOsUser;
    }




}


