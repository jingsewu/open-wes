/**
 * Seed an inbound plan order so the inbound pipeline smoke test has data.
 * The receive station backend (scan order -> accept) is not implemented yet,
 * so this only exercises the inbound data model + management list display.
 *
 * Idempotent. Usage: node seed-inbound.js [customerOrderNo]
 */
const { execFileSync } = require('child_process');
const JDBC_JAR =
  process.env.MYSQL_JDBC_JAR ||
  'C:/Users/niekang/.gradle/caches/modules-2/files-2.1/com.mysql/mysql-connector-j/8.3.0/1cc7fa5d61f4bbc113531a4ba6d85d41cf3d57e1/mysql-connector-j-8.3.0.jar';

function sql(stmt) {
  return execFileSync('java', ['-cp', `.;${JDBC_JAR}`, 'SqlRunner', stmt], {
    cwd: __dirname, encoding: 'utf-8', stdio: ['ignore', 'pipe', 'pipe'],
  });
}
function rowCount(stmt) {
  const l = sql(stmt).split('\n').filter(Boolean).find((x) => /^\d+$/.test(x.trim()));
  return Number(l);
}

(async () => {
  const customerOrderNo = process.argv[2] || `INB_SMOKE_${Date.now()}`;
  if (rowCount(`SELECT COUNT(*) FROM w_inbound_plan_order WHERE customer_order_no='${customerOrderNo}'`) > 0) {
    console.log(`[inbound] ${customerOrderNo} already exists`);
    process.exit(0);
  }
  const now = Date.now();
  const orderId = (BigInt(now) << 22n) + 1n;
  const detailId = (BigInt(now) << 22n) + 2n;
  sql(
    `INSERT INTO w_inbound_plan_order (id, create_time, create_user, update_time, update_user, audit_time, audit_user, ` +
      `abnormal, carrier, customer_order_no, customer_order_type, estimated_arrival_date, inbound_plan_order_status, ` +
      `lpn_code, order_no, remark, sender, shipping_method, sku_kind_num, storage_type, total_box, total_qty, ` +
      `tracking_number, version, warehouse_code) VALUES ` +
      `(${orderId}, ${now}, 'admin', ${now}, 'admin', ${now}, 'admin', 0, 'CARRIER1', '${customerOrderNo}', 'SALES', ${now}, 'NEW', ` +
      `'LPN-${customerOrderNo}', 'IN-${customerOrderNo}', 'smoke test', 'SENDER1', 'SHIP1', 1, 'STORAGE', 1, 10, ` +
      `'TRK-${customerOrderNo}', 0, 'WH001')`
  );
  sql(
    `INSERT INTO w_inbound_plan_order_detail (id, create_time, create_user, update_time, update_user, ` +
      `box_no, inbound_plan_order_id, owner_code, qty_abnormal, qty_accepted, qty_restocked, sku_code, sku_id, sku_name) VALUES ` +
      `(${detailId}, ${now}, 'admin', ${now}, 'admin', 'BOX1', ${orderId}, 'OWNER001', 0, 0, 0, 'SKU001', 1, 'Test SKU')`
  );
  console.log(`[inbound] seeded ${customerOrderNo} (order ${orderId}, detail ${detailId})`);
  console.log(sql(`SELECT id, customer_order_no, inbound_plan_order_status, total_qty FROM w_inbound_plan_order WHERE customer_order_no='${customerOrderNo}'`));
})().catch((e) => { console.error(e); process.exit(1); });
