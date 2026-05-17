## Context

The WES (Warehouse Execution System) uses WorkStation entities to manage warehouse workstations. Currently, WorkStation IDs are generated using database auto-increment but may not start from 1, making log analysis and operator identification difficult. The project uses Liquibase for database schema management.

Current implementation uses `@GeneratedValue(strategy = GenerationType.IDENTITY)` in `WorkStationPO.java` for ID generation.

## Goals / Non-Goals

**Goals:**
- Configure WorkStation table auto-increment sequence to start from 1
- Improve log readability with sequential, predictable WorkStation IDs
- Maintain existing code structure and type system (Long IDs)
- Use Liquibase for database changes

**Non-Goals:**
- No historical WorkStation data migration
- No changes to WorkStation ID type (stays as Long)
- No application code changes
- No changes to existing WorkStation records

## Decisions

**Liquibase for Database Configuration**
- Use Liquibase XML change set to modify auto-increment sequence
- Liquibase is already integrated in the project for schema management
- Provides version control and rollback capability for database changes
- Change set will execute on application startup

**Auto-Increment Sequence Configuration**
- Target: MySQL database `w_work_station` table
- Set AUTO_INCREMENT value to 1
- Only affects new WorkStation insertions after deployment
- Historical records with higher ID values remain unchanged

**No Code Changes Required**
- JPA `@GeneratedValue(strategy = GenerationType.IDENTITY)` annotation remains unchanged
- All existing APIs, DTOs, and service layers continue working without modification
- Type remains `Long` throughout the codebase
- Only database-level configuration changes

**Alternative Considered: Database Manual Reset**
- Could reset sequence directly via SQL: `ALTER TABLE w_work_station AUTO_INCREMENT = 1;`
- Rejected: Liquibase provides better versioning and team coordination
- Liquibase change set is tracked in source control and applied consistently

## Risks / Trade-offs

**Risk: Sequence Reset May Not Work as Expected in MySQL**
- In MySQL, AUTO_INCREMENT only sets to the next value greater than existing rows
- If table has existing rows with IDs > 1, sequence may not actually start at 1
- **Mitigation**: Accept that sequence will start at MAX(existing_id) + 1, which still provides sequential behavior for new records
- The core benefit of sequential, predictable IDs is achieved regardless of exact starting point

**Risk: Liquibase Change Set May Fail in Production**
- If table has foreign key constraints or active locks, change set may fail
- **Mitigation**: Test in staging environment first
- Include rollback strategy in Liquibase change set
- Schedule deployment during maintenance window if necessary

**Trade-off: No Historical Data Standardization**
- Existing WorkStation records will have non-sequential IDs
- Benefits: No risk of data corruption or duplicate ID issues
  - Downside: Log analysis may need to handle both old and new ID patterns
- **Acceptable**: The improvement applies to all future logs, which is the primary use case

## Migration Plan

**Deployment Steps:**
1. Create Liquibase change set XML file in appropriate directory
2. Add change set with SQL: `ALTER TABLE w_work_station AUTO_INCREMENT = 1;`
3. Verify change set syntax and path
4. Deploy application with new change set
5. Monitor application logs for Liquibase execution
6. Verify new WorkStation creation starts from expected sequence value

**Rollback Strategy:**
- Liquibase change set is additive (ALTER TABLE)
- Rollback can be achieved by manually resetting sequence if needed
- No data loss or corruption risk from this change

**Testing:**
1. Unit test: Verify Liquibase change set file format
2. Integration test: Create new WorkStation and verify ID assignment
3. Log verification: Confirm new IDs appear in expected log format
4. Staging environment: Full deployment test before production

## Open Questions

None. This is a straightforward database configuration change with well-defined scope and implementation approach.
