## Why

Current WorkStation IDs use database auto-increment but may not start from 1, making them less readable in logs and harder to identify which station/operator performed operations. Sequential integer IDs starting from 1 provide clear, predictable identifiers that are easier to read in log files and operator tracking.

## What Changes

- Keep WorkStation ID type as `Long` (no type change)
- Add Liquibase change set to configure auto-increment sequence to start from 1 for `w_work_station` table
- Historical data migration not required - focus only on new WorkStation records

## Capabilities

### New Capabilities
- `workstation-id-sequence`: Manage sequential integer ID generation for WorkStation entities starting from 1

### Modified Capabilities
- None (this is an implementation-level change, not a requirement change)

## Impact

- **Database**: Add Liquibase change set to configure `w_work_station` table auto-increment sequence starting from 1
- **Liquibase**: New XML configuration file required for table structure modification
- **APIs**: No impact - WorkStation-related CRUD APIs continue to return `Long` type IDs
- **Logging**: Log entries will show more readable WorkStation IDs for new records
- **Dependencies**: No code changes required since type remains `Long`
