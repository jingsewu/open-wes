/**
 * Reset the WES outbound-pipeline operational state to a clean baseline so the
 * station picking E2E is deterministic. Keeps the base seed data (warehouse,
 * SKU, batch attribute, physical container CTN001, put-wall PW01) intact.
 *
 * What it clears / resets:
 *   - operation tasks, picking orders, waves, pre-allocated records
 *   - outbound plan orders (test-created ones)
 *   - transfer containers + records + container-stock transactions
 *   - TOTE container-stock rows (keeps CTN001), resets CTN001 qty
 *   - put-wall slots back to IDLE
 *   - sku_batch_stock locks (outbound_locked_qty=0)
 *   - Redis caches (station cache, put-wall, search class cache)
 *
 * Usage: node reset-pipeline.js
 */
const { execFileSync } = require('child_process');
const JDBC_JAR =
  process.env.MYSQL_JDBC_JAR ||
  'C:/Users/niekang/.gradle/caches/modules-2/files-2.1/com.mysql/mysql-connector-j/8.3.0/1cc7fa5d61f4bbc113531a4ba6d85d41cf3d57e1/mysql-connector-j-8.3.0.jar';

function sql(stmt) {
  return execFileSync('java', ['-cp', `.;${JDBC_JAR}`, 'SqlRunner', stmt], {
    cwd: __dirname,
    encoding: 'utf-8',
    stdio: ['ignore', 'pipe', 'pipe'],
  });
}

function run(stmt) {
  const out = sql(stmt).split('\n').filter(Boolean).pop() || '';
  console.log(`  ${stmt.slice(0, 80)} -> ${out}`);
}

function clearRedis(keys) {
  const redisScript = require('path').join(__dirname, 'redis-del.js');
  for (const k of keys) {
    try {
      const out = execFileSync('node', [redisScript, k], { encoding: 'utf-8', stdio: ['ignore', 'pipe', 'pipe'] });
      console.log(`  redis ${k}: ${out.trim().split('\n').pop()}`);
    } catch (e) {
      console.log(`  redis ${k}: (no key)`);
    }
  }
}

(async () => {
  const tables = [
    'w_operation_task',
    'w_picking_order_detail',
    'w_picking_order',
    'w_outbound_wave',
    'w_outbound_pre_allocated_record',
    'w_outbound_plan_order_detail',
    'w_outbound_plan_order',
    'w_transfer_container_record',
    'w_transfer_container',
    'w_container_stock_transaction',
  ];
  for (const t of tables) {
    try { run(`DELETE FROM ${t}`); } catch (e) { console.log(`  ${t}: (empty/none)`); }
  }

  // Remove TOTE/transfer-container stock rows, reset the physical CTN001 stock.
  try { run(`DELETE FROM w_container_stock WHERE container_code <> 'CTN001'`); } catch (e) {}
  try { run(`UPDATE w_container_stock SET available_qty=100, total_qty=100, outbound_locked_qty=0, no_outbound_locked_qty=0, frozen_qty=0 WHERE container_code='CTN001'`); } catch (e) {}
  try { run(`UPDATE w_sku_batch_stock SET available_qty=100, total_qty=100, outbound_locked_qty=0, no_outbound_locked_qty=0, frozen_qty=0 WHERE sku_id=1`); } catch (e) {}

  // Put-wall slots back to IDLE.
  try { run(`UPDATE w_put_wall_slot SET put_wall_slot_status='IDLE', picking_order_id=0, transfer_container_code=NULL, transfer_container_record_id=NULL`); } catch (e) {}

  // Clear Redis caches so the station + put-wall + search reload from DB.
  clearRedis([
    'WorkStation:1',
    '_station:work:put:wall:cache::1',
    '_station:work:put:wall:slot:cache::workStation:1',
  ]);

  console.log('[reset] done — run seed-env.js next if base data was removed');
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
