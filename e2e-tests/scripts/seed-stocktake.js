/**
 * Seed the stocktake E2E fixture. Idempotent for the spec/attribute/batch rows;
 * the container is expected to be unique per run.
 *
 * What it creates (all via direct SQL unless noted):
 *   1. A CONTAINER-type container spec STK_SPEC with one slot (FACE1/SLOT1).
 *      The management "count by container" wizard only lists containers whose
 *      spec type is CONTAINER, so TOTE_SPEC (TRANSFER_CONTAINER) cannot be used.
 *   2. A stock container with one container-stock line for SKU001.
 *   3. Batch attribute / batch stock rows when the environment lacks them.
 *   4. Puts workstation 1 into STOCKTAKE mode and clears both station caches
 *      so the ONLINE event re-initializes from the new DB mode.
 *
 * Usage: node seed-stocktake.js <containerCode> [qty]
 * Prints a RESULT line the spec parses:
 *   RESULT {"containerCode":"...","skuCode":"SKU001","qty":10}
 */
const { execFileSync } = require('child_process');
const path = require('path');

const JDBC_JAR =
  process.env.MYSQL_JDBC_JAR ||
  'C:/Users/niekang/.gradle/caches/modules-2/files-2.1/com.mysql/mysql-connector-j/8.3.0/1cc7fa5d61f4bbc113531a4ba6d85d41cf3d57e1/mysql-connector-j-8.3.0.jar';

const WAREHOUSE = 'WH001';
const AREA_ID = 1;
const SKU = 'SKU001';
const OWNER = 'OWNER001';
const SPEC_CODE = 'STK_SPEC';

function sql(stmt) {
  return execFileSync('java', ['-cp', `.;${JDBC_JAR}`, 'SqlRunner', stmt], {
    cwd: __dirname,
    encoding: 'utf-8',
    stdio: ['ignore', 'pipe', 'pipe'],
  });
}

function rowCount(stmt) {
  const line = sql(stmt).split('\n').filter(Boolean).find((l) => /^\d+$/.test(l.trim()));
  return Number(line);
}

/** First value of the first data row, or null when the result is empty. */
function queryOne(stmt) {
  const lines = sql(stmt)
    .split('\n')
    .map((l) => l.replace(/\r$/, ''))
    .filter((l) => l.length > 0);
  if (lines.length < 2 || lines[1].startsWith('(')) return null;
  return lines[1].split('\t')[0];
}

function nextId() {
  return (BigInt(Date.now()) << 22n) + BigInt(Math.floor(Math.random() * 100));
}

function clearRedis(keys) {
  for (const k of keys) {
    try {
      execFileSync('node', [path.join(__dirname, 'redis-del.js'), k], {
        cwd: __dirname,
        encoding: 'utf-8',
        stdio: ['ignore', 'pipe', 'pipe'],
      });
      console.log(`[redis] cleared ${k}`);
    } catch (e) {
      console.log(`[redis] ${k}: (no key)`);
    }
  }
}

function requireBaseline() {
  const skuId = queryOne(`SELECT id FROM m_sku_main_data WHERE sku_code='${SKU}' AND warehouse_code='${WAREHOUSE}' LIMIT 1`);
  if (!skuId) {
    throw new Error(`${SKU} not found in ${WAREHOUSE} — run seed-server-baseline.js first`);
  }
  if (rowCount(`SELECT COUNT(*) FROM w_warehouse_area WHERE id=${AREA_ID}`) === 0) {
    throw new Error(`warehouse area id=${AREA_ID} not found — run seed-server-baseline.js first`);
  }
  return Number(skuId);
}

function ensureBatchAttribute(skuId) {
  const existing = queryOne(`SELECT id FROM w_sku_batch_attribute WHERE sku_id=${skuId} LIMIT 1`);
  if (existing) return Number(existing);

  const id = nextId();
  const now = Date.now();
  sql(
    `INSERT INTO w_sku_batch_attribute (id, create_time, create_user, update_time, update_user, ` +
      `batch_no, sku_id, sku_attributes) VALUES ` +
      `(${id}, ${now}, 'admin', ${now}, 'admin', 'BATCH001', ${skuId}, '{}')`
  );
  console.log(`[batch_attr] created w_sku_batch_attribute for sku_id=${skuId}`);
  return Number(id);
}

function ensureBatchStock(skuId, batchAttributeId) {
  const existing = queryOne(`SELECT id FROM w_sku_batch_stock WHERE sku_id=${skuId} LIMIT 1`);
  if (existing) {
    sql(`UPDATE w_sku_batch_stock SET sku_batch_attribute_id=${batchAttributeId} WHERE id=${existing}`);
    sql(`UPDATE w_sku_batch_stock SET version=0 WHERE version IS NULL`);
    return Number(existing);
  }

  const id = nextId();
  const now = Date.now();
  sql(
    `INSERT INTO w_sku_batch_stock (id, create_time, create_user, update_time, update_user, ` +
      `available_qty, frozen_qty, no_outbound_locked_qty, outbound_locked_qty, sku_batch_attribute_id, ` +
      `sku_id, total_qty, version, warehouse_area_id, warehouse_code) VALUES ` +
      `(${id}, ${now}, 'admin', ${now}, 'admin', 1000, 0, 0, 0, ${batchAttributeId}, ` +
      `${skuId}, 1000, 0, ${AREA_ID}, '${WAREHOUSE}')`
  );
  console.log(`[batch_stock] created w_sku_batch_stock for sku_id=${skuId}`);
  return Number(id);
}

function ensureContainerSpec() {
  if (rowCount(`SELECT COUNT(*) FROM w_container_spec WHERE container_spec_code='${SPEC_CODE}' AND warehouse_code='${WAREHOUSE}'`) > 0) {
    sql(
      `UPDATE w_container_spec SET container_slot_specs='${slotSpecsJson()}' ` +
        `WHERE container_spec_code='${SPEC_CODE}' AND warehouse_code='${WAREHOUSE}' AND container_slot_specs IS NULL`
    );
    console.log(`[spec] ${SPEC_CODE} already exists`);
    return;
  }
  const now = Date.now();
  const id = nextId();
  sql(
    `INSERT INTO w_container_spec (id, create_time, create_user, update_time, update_user, ` +
      `container_slot_num, container_spec_code, container_spec_name, container_type, ` +
      `height, length, volume, width, warehouse_code, container_slot_specs) VALUES ` +
      `(${id}, ${now}, 'admin', ${now}, 'admin', 1, '${SPEC_CODE}', 'Stocktake Container', 'CONTAINER', ` +
      `200, 300, 100000, 200, '${WAREHOUSE}', '${slotSpecsJson()}')`
  );
  console.log(`[spec] created ${SPEC_CODE} with slot FACE1/SLOT1`);
}

function slotSpecsJson() {
  return JSON.stringify([
    {
      containerSlotSpecCode: 'SLOT1',
      face: 'FACE1',
      length: 200, width: 100, height: 100, volume: 10000,
      level: 1, bay: 1, locLevel: 1, locBay: 1,
      children: [],
    },
  ]).replace(/'/g, "''");
}

function containerSlotsJson() {
  return JSON.stringify([
    {
      containerSlotCode: 'SLOT1',
      containerSlotSpecCode: 'SLOT1',
      occupationRatio: 1,
      emptySlot: false,
      face: 'FACE1',
      locationCode: 'LOC-STOCKTAKE-SLOT1',
      children: [],
    },
  ]).replace(/'/g, "''");
}

function seedContainer(containerCode, qty) {
  const now = Date.now();
  if (rowCount(`SELECT COUNT(*) FROM w_container WHERE container_code='${containerCode}' AND warehouse_code='${WAREHOUSE}'`) === 0) {
    const id = nextId();
    sql(
      `INSERT INTO w_container (id, create_time, create_user, update_time, update_user, ` +
        `container_code, container_slot_num, container_spec_code, container_status, empty_container, ` +
        `empty_slot_num, location_code, location_type, locked, opened, warehouse_area_id, ` +
        `warehouse_code, warehouse_logic_id, version, container_slots) VALUES ` +
        `(${id}, ${now}, 'admin', ${now}, 'admin', '${containerCode}', 1, '${SPEC_CODE}', 'IN_SIDE', 0, ` +
        `0, 'LOC-${containerCode}', 'RACK', 0, 0, ${AREA_ID}, '${WAREHOUSE}', 0, 0, '${containerSlotsJson()}')`
    );
    console.log(`[container] created ${containerCode}`);
  } else {
    console.log(`[container] ${containerCode} already exists`);
  }
}

function seedContainerStock(containerCode, qty, skuId, batchAttributeId, batchStockId) {
  if (rowCount(`SELECT COUNT(*) FROM w_container_stock WHERE container_code='${containerCode}' AND warehouse_code='${WAREHOUSE}'`) > 0) {
    console.log(`[container_stock] ${containerCode} already exists`);
    return;
  }
  const now = Date.now();
  const id = nextId();
  sql(
    `INSERT INTO w_container_stock (id, create_time, create_user, update_time, update_user, ` +
      `available_qty, box_no, box_stock, container_code, container_face, container_id, container_slot_code, ` +
      `frozen_qty, no_outbound_locked_qty, outbound_locked_qty, sku_batch_attribute_id, sku_batch_stock_id, ` +
      `sku_id, total_qty, version, warehouse_code) VALUES ` +
      `(${id}, ${now}, 'admin', ${now}, 'admin', ${qty}, 'BOX1', 1, '${containerCode}', 'FACE1', ` +
      `(SELECT id FROM w_container WHERE container_code='${containerCode}' AND warehouse_code='${WAREHOUSE}' LIMIT 1), 'SLOT1', ` +
      `0, 0, 0, ${batchAttributeId}, ${batchStockId}, ${skuId}, ${qty}, 0, '${WAREHOUSE}')`
  );
  console.log(`[container_stock] created ${containerCode} qty=${qty}`);
}

function setStationMode(mode) {
  sql(`UPDATE w_work_station SET work_station_mode='${mode}' WHERE id=1`);
  console.log(`[station] work_station_mode -> ${mode}`);
  clearRedis(['_wms:basic:work:station:cache::1', 'WorkStation:1']);
}

(async () => {
  const containerCode = process.argv[2] || `STK_${Date.now()}`;
  const qty = Number(process.argv[3] || 10);

  const skuId = requireBaseline();
  ensureContainerSpec();
  const batchAttributeId = ensureBatchAttribute(skuId);
  const batchStockId = ensureBatchStock(skuId, batchAttributeId);
  seedContainer(containerCode, qty);
  seedContainerStock(containerCode, qty, skuId, batchAttributeId, batchStockId);
  setStationMode('STOCKTAKE');

  console.log('\n[verify]');
  console.log(sql(`SELECT container_code, container_spec_code, container_status FROM w_container WHERE container_code='${containerCode}'`));
  console.log(sql(`SELECT container_code, sku_id, container_face, container_slot_code, available_qty FROM w_container_stock WHERE container_code='${containerCode}'`));
  console.log(sql(`SELECT id, work_station_mode FROM w_work_station WHERE id=1`));
  console.log(`\nRESULT ${JSON.stringify({ containerCode, skuCode: SKU, qty })}`);
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
