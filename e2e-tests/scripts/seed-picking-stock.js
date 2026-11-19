/**
 * Seed the physical picking stock needed for the ROBOT-area outbound pipeline:
 *   - a container (w_container) holding SKU001 in warehouse area 1
 *   - a container-level stock row (w_container_stock) with available qty
 *   - a storage location (w_location) for the container (shelf = container code)
 *
 * Idempotent. Requires w_sku_batch_stock and w_sku_batch_attribute already present.
 *
 * Usage: node seed-picking-stock.js [containerCode] [qty]
 */
const { execFileSync } = require('child_process');
const JDBC_JAR =
  process.env.MYSQL_JDBC_JAR ||
  'C:/Users/niekang/.gradle/caches/modules-2/files-2.1/com.mysql/mysql-connector-j/8.3.0/1cc7fa5d61f4bbc113531a4ba6d85d41cf3d57e1/mysql-connector-j-8.3.0.jar';

function sql(stmt) {
  const out = execFileSync('java', ['-cp', `.;${JDBC_JAR}`, 'SqlRunner', stmt], {
    cwd: __dirname,
    encoding: 'utf-8',
    stdio: ['ignore', 'pipe', 'pipe'],
  });
  return out;
}

function rowCount(stmt) {
  const line = sql(stmt).split('\n').filter(Boolean).find((l) => /^\d+$/.test(l.trim()));
  return Number(line);
}

function nextId() {
  const now = Date.now();
  const seq = Math.floor(Math.random() * 100);
  return (BigInt(now) << 22n) + BigInt(seq);
}

(async () => {
  const containerCode = process.argv[2] || 'CTN001';
  const qty = Number(process.argv[3] || 100);
  const now = Date.now();

  // 1. w_container
  const hasContainer = rowCount(`SELECT COUNT(*) FROM w_container WHERE container_code='${containerCode}'`);
  if (hasContainer === 0) {
    const cid = nextId();
    sql(
      `INSERT INTO w_container (id, create_time, create_user, update_time, update_user, ` +
        `container_code, container_slot_num, container_spec_code, container_status, empty_container, ` +
        `empty_slot_num, location_code, location_type, locked, opened, warehouse_area_id, ` +
        `warehouse_code, warehouse_logic_id, version) VALUES ` +
        `(${cid}, ${now}, 'admin', ${now}, 'admin', '${containerCode}', 1, 'TOTE_SPEC', 'IN_SIDE', 0, ` +
        `1, 'LOC-${containerCode}', 'STORAGE', 0, 0, 1, 'WH001', 0, 0)`
    );
    console.log(`[container] created ${containerCode} id=${cid}`);
  } else {
    console.log(`[container] ${containerCode} already exists`);
  }

  // 2. w_container_stock
  const hasStock = rowCount(`SELECT COUNT(*) FROM w_container_stock WHERE container_code='${containerCode}'`);
  if (hasStock === 0) {
    const csid = nextId();
    sql(
      `INSERT INTO w_container_stock (id, create_time, create_user, update_time, update_user, ` +
        `available_qty, box_no, box_stock, container_code, container_face, container_id, container_slot_code, ` +
        `frozen_qty, no_outbound_locked_qty, outbound_locked_qty, sku_batch_attribute_id, sku_batch_stock_id, ` +
        `sku_id, total_qty, version, warehouse_code) VALUES ` +
        `(${csid}, ${now}, 'admin', ${now}, 'admin', ${qty}, 'BOX1', 1, '${containerCode}', 'FACE1', ` +
        `(SELECT id FROM w_container WHERE container_code='${containerCode}'), 'SLOT1', ` +
        `0, 0, 0, 7492941805684523009, 1, 1, ${qty}, 0, 'WH001')`
    );
    console.log(`[container_stock] created for ${containerCode} qty=${qty}`);
  } else {
    console.log(`[container_stock] ${containerCode} already exists`);
  }

  // 3. w_location (storage shelf for the container)
  const hasLocation = rowCount(`SELECT COUNT(*) FROM w_location WHERE shelf_code='${containerCode}'`);
  if (hasLocation === 0) {
    const lid = nextId();
    sql(
      `INSERT INTO w_location (id, create_time, create_user, update_time, update_user, ` +
        `aisle_code, heat, location_code, location_status, location_type, occupied, position, shelf_code, ` +
        `version, warehouse_area_id, warehouse_code) VALUES ` +
        `(${lid}, ${now}, 'admin', ${now}, 'admin', 'AISLE1', 'A', 'LOC-${containerCode}', 'PUT_AWAY_PUT_DOWN', ` +
        `'STORAGE', 1, '{}', '${containerCode}', 0, 1, 'WH001')`
    );
    console.log(`[location] created LOC-${containerCode}`);
  } else {
    console.log(`[location] LOC-${containerCode} already exists`);
  }

  console.log('\n[verify]');
  console.log(sql(`SELECT container_code, container_status, location_code, warehouse_area_id FROM w_container WHERE container_code='${containerCode}'`));
  console.log(sql(`SELECT container_code, sku_batch_stock_id, available_qty, total_qty, sku_id FROM w_container_stock WHERE container_code='${containerCode}'`));
  console.log(sql(`SELECT location_code, shelf_code, location_status FROM w_location WHERE shelf_code='${containerCode}'`));
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
