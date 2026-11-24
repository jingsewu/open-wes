/**
 * Print the source container code of an unfinished picking task assigned to a
 * put-wall slot, one per line (unique).
 *
 * The picking E2E simulates the robot delivering the source container; which
 * container the allocator chose depends on available stock (another scenario,
 * e.g. inbound receiving, may add stock for the same SKU). Resolving it from
 * the task keeps the test independent of allocation order.
 *
 * Usage: node get-pick-source.js <putWallSlotCode>
 */
const { execFileSync } = require('child_process');
const JDBC_JAR =
  process.env.MYSQL_JDBC_JAR ||
  'C:/Users/niekang/.gradle/caches/modules-2/files-2.1/com.mysql/mysql-connector-j/8.3.0/1cc7fa5d61f4bbc113531a4ba6d85d41cf3d57e1/mysql-connector-j-8.3.0.jar';

const slotCode = process.argv[2];
if (!slotCode) {
  console.error('Usage: node get-pick-source.js <putWallSlotCode>');
  process.exit(1);
}

const escaped = slotCode.replace(/'/g, "''");
const out = execFileSync(
  'java',
  ['-cp', `.;${JDBC_JAR}`, 'SqlRunner',
    `SELECT DISTINCT source_container_code FROM w_operation_task ` +
    `WHERE task_type='PICKING' AND task_status IN ('NEW','PROCESSING') ` +
    `AND assigned_station_slot LIKE '%"${escaped}"%'`],
  { cwd: __dirname, encoding: 'utf-8', stdio: ['ignore', 'pipe', 'pipe'] }
);

const codes = out
  .split('\n')
  .map((l) => l.trim())
  .filter((l) => l && l !== 'source_container_code' && !/^\(\d+ rows?\)$/.test(l));
if (codes.length === 0) {
  throw new Error(`no unfinished picking task assigned to slot ${slotCode}`);
}
console.log(codes[0]);
