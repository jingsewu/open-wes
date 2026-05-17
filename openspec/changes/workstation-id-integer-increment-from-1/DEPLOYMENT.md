# WorkStation ID Sequential Generation - Deployment Guide

## Overview

This deployment configures the WorkStation table auto-increment sequence to start from 1 using Liquibase. This improves log readability and operator identification by ensuring new WorkStation records receive sequential, predictable IDs.

## Prerequisites

- Application with Liquibase integration
- Access to staging/production database
- Monitoring tools for application logs
- Maintenance window access (if needed)

## Deployment Steps

### 1. Verify Liquibase Files

**Status**: ✅ Complete

- [x] Created `db.changelog-20260317.xml` in `server/server/wes-server/src/main/resources/db/changelog/`
- [x] Added include statement to `db.changelog-master.xml`
- [x] Change set includes proper preconditions for table existence

### 2. Local Testing

**Status**: ⏳ Pending Application Startup

- [ ] Start application locally with development database
- [ ] Verify Liquibase change set executes without errors
- [ ] Check application logs for Liquibase execution confirmation
- [ ] Verify `DATABASECHANGELOG` table contains the new change set record

### 3. Staging Deployment

**Status**: ⏳ Pending

- [ ] Deploy application to staging environment
- [ ] Monitor application startup logs for Liquibase execution:
  ```
  Liquibase: Executing SQL: ALTER TABLE w_work_station AUTO_INCREMENT = 1;
  Liquibase: ChangeSet configure_workstation_auto_increment_start_from_1/kang.nie executed
  ```
- [ ] Verify change set recorded in `DATABASECHANGELOG` table
- [ ] Test WorkStation creation via API endpoints
- [ ] Verify new WorkStation IDs are sequential and readable

### 4. Production Deployment

**Status**: ⏳ Pending

- [ ] Schedule deployment during appropriate maintenance window
- [ ] Deploy application to production
- [ ] Monitor application startup logs for Liquibase execution
- [ ] Verify change set recorded in production `DATABASECHANGELOG` table
- [ ] Test WorkStation creation in production
- [ ] Monitor logs for new WorkStation ID format

## Rollback Procedure

### Scenario 1: Change Set Fails During Execution

If Liquibase change set fails during application startup:

1. **Identify the failure** from application logs
2. **Check error message** - common causes:
   - Table has active locks
   - Foreign key constraints
   - Insufficient database permissions
3. **Address the issue**:
   - Wait for locks to release
   - Temporarily disable foreign key checks (if safe)
   - Verify database user permissions
4. **Retry deployment**

### Scenario 2: Manual Sequence Reset Needed

If you need to manually reset the sequence after deployment:

```sql
-- Reset sequence to specific value
ALTER TABLE w_work_station AUTO_INCREMENT = <desired_value>;

-- Example: Reset to 1 (will start at MAX(existing_id) + 1)
ALTER TABLE w_work_station AUTO_INCREMENT = 1;
```

### Scenario 3: Complete Rollback Required

If you need to completely remove the Liquibase change set:

```sql
-- Remove the change set record from DATABASECHANGELOG
DELETE FROM DATABASECHANGELOG
WHERE ID = 'configure_workstation_auto_increment_start_from_1'
  AND AUTHOR = 'kang.nie'
  AND FILENAME = 'db.changelog-20260317.xml';
```

Then remove the include statement from `db.changelog-master.xml` and redeploy.

## Verification Steps

### Pre-Deployment

- [ ] Verify current WorkStation ID values in database
- [ ] Note highest existing WorkStation ID for comparison
- [ ] Backup database (recommended)

### Post-Deployment

- [ ] Verify Liquibase change set executed successfully
- [ ] Check `DATABASECHANGELOG` table contains new record
- [ ] Create test WorkStation via API
- [ ] Verify new WorkStation ID is sequential
- [ ] Confirm new ID > MAX(existing_id) + 1
- [ ] Check application logs show readable WorkStation IDs
- [ ] Verify existing WorkStation records unchanged
- [ ] Test WorkStation CRUD operations
- [ ] Verify API responses maintain Long type for ID field

## Monitoring

### Key Metrics to Monitor

1. **Liquibase Execution**: Check application logs for successful change set execution
2. **WorkStation Creation**: Monitor new WorkStation ID values
3. **Log Format**: Verify WorkStation IDs appear as sequential integers
4. **API Performance**: No degradation in WorkStation API performance
5. **Database Performance**: Monitor for any performance impact from sequence configuration

### Log Search Queries

```bash
# Check for Liquibase execution
grep "configure_workstation_auto_increment_start_from_1" application.log

# Check for WorkStation creation with IDs
grep "work station id:" application.log | tail -20

# Monitor for any database-related errors
grep -i "error.*workstation\|error.*liquibase" application.log
```

## Risk Mitigation

### Low Risk

- **Data Loss**: None - Liquibase change set only modifies sequence, not data
- **API Compatibility**: None - Long type maintained throughout system
- **Application Changes**: None - Only database configuration modified

### Medium Risk

- **Deployment Issues**: Change set may fail due to database locks or permissions
  - **Mitigation**: Test in staging first, schedule during maintenance window
- **Sequence Behavior**: MySQL may not start at exactly 1 if existing IDs > 1
  - **Mitigation**: Accept sequence starts at MAX(existing_id) + 1, still provides sequential behavior

### High Risk

- **None identified**: This is a low-risk, additive database change

## Support Contacts

- **Database Team**: [Contact information]
- **Application Team**: [Contact information]
- **DevOps Team**: [Contact information]

## Timeline Estimate

- **Local Testing**: 1-2 hours
- **Staging Testing**: 2-4 hours
- **Production Deployment**: 1-2 hours
- **Total Estimated Time**: 4-8 hours

## Success Criteria

Deployment is considered successful when:

1. ✅ Liquibase change set executes without errors
2. ✅ New WorkStation IDs are sequential and predictable
3. ✅ Log entries show readable WorkStation IDs
4. ✅ Existing WorkStation records remain unchanged
5. ✅ All WorkStation APIs function correctly
6. ✅ No performance degradation observed
7. ✅ No errors in application or database logs

---

**Document Version**: 1.0
**Last Updated**: 2026-03-17
**Author**: kang.nie
