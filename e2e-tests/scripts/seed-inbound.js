/**
 * Seed the inbound receiving pipeline so the inbound-receiving E2E can run
 * end-to-end. Idempotent — safe to run repeatedly.
 *
 * What it creates (all via direct SQL unless noted):
 *   1. An inbound plan order (status NEW) + one detail for SKU001.
 *      The detail's qty_restocked is the accepted qty — accepting more than
 *      that throws Preconditions ("restocked qty should be greater than
 *      accepted qty").
 *   2. A CONTAINER container spec RCV_SPEC with one slot spec (SLOT1) — the
 *      receive UI's container-spec picker (SearchContainerSpecCode) needs a
 *      CONTAINER-type spec to render the shelf.
 *   3. A target receiving container (unique code) with one slot (SLOT1) — the
 *      accept's ContainerValidator requires the target container + slot to
 *      exist.
 *   4. Puts workstation 1 into RECEIVE mode (the receive page only mounts when
 *      the station cache's workStationMode is RECEIVE) and clears the station
 *      caches so the ONLINE re-initialization reads the fresh DB mode.
 *
 * Usage: node seed-inbound.js [customerOrderNo] [containerCode]
 */
const { execFileSync } = require('child_process');
const path = require('path');
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
function nextId() {
  return (BigInt(Date.now()) << 22n) + BigInt(Math.floor(Math.random() * 100));
}
function clearRedis(keys) {
  for (const k of keys) {
    try {
      execFileSync('node', [path.join(__dirname, 'redis-del.js'), k], {
        cwd: __dirname, encoding: 'utf-8', stdio: ['ignore', 'pipe', 'pipe'],
      });
      console.log(`[redis] cleared ${k}`);
    } catch (e) {
      console.log(`[redis] ${k}: (no key)`);
    }
  }
}

const ACCEPT_QTY = 10;

function seedInboundOrder(customerOrderNo) {
  if (rowCount(`SELECT COUNT(*) FROM w_inbound_plan_order WHERE customer_order_no='${customerOrderNo}'`) > 0) {
    console.log(`[inbound] ${customerOrderNo} already exists`);
    return;
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
      `'LPN-${customerOrderNo}', 'IN-${customerOrderNo}', 'smoke test', 'SENDER1', 'SHIP1', 1, 'STORAGE', 1, ${ACCEPT_QTY}, ` +
      `'TRK-${customerOrderNo}', 0, 'WH001')`
  );
  sql(
    `INSERT INTO w_inbound_plan_order_detail (id, create_time, create_user, update_time, update_user, ` +
      `box_no, inbound_plan_order_id, owner_code, qty_abnormal, qty_accepted, qty_restocked, sku_code, sku_id, sku_name) VALUES ` +
      `(${detailId}, ${now}, 'admin', ${now}, 'admin', 'BOX1', ${orderId}, 'OWNER001', 0, 0, ${ACCEPT_QTY}, 'SKU001', 1, 'Test SKU')`
  );
  console.log(`[inbound] seeded ${customerOrderNo} (order ${orderId}, detail ${detailId}, qty ${ACCEPT_QTY})`);
}

function seedContainerSpec() {
  const SPEC_CODE = 'RCV_SPEC';
  if (rowCount(`SELECT COUNT(*) FROM w_container_spec WHERE container_spec_code='${SPEC_CODE}' AND warehouse_code='WH001'`) > 0) {
    console.log(`[spec] ${SPEC_CODE} already exists`);
    return;
  }
  const now = Date.now();
  const id = nextId();
  const slotSpecs = JSON.stringify([
    {
      containerSlotSpecCode: 'SLOT1',
      face: 'FACE1',
      length: 200, width: 100, height: 100, volume: 10000,
      level: 1, bay: 1, locLevel: 1, locBay: 1,
      children: [],
    },
  ]).replace(/'/g, "''");
  sql(
    `INSERT INTO w_container_spec (id, create_time, create_user, update_time, update_user, ` +
      `container_slot_num, container_spec_code, container_spec_name, container_type, ` +
      `height, length, volume, width, warehouse_code, container_slot_specs) VALUES ` +
      `(${id}, ${now}, 'admin', ${now}, 'admin', 1, '${SPEC_CODE}', 'Receive Container', 'CONTAINER', ` +
      `200, 300, 100000, 200, 'WH001', '${slotSpecs}')`
  );
  console.log(`[spec] created ${SPEC_CODE} with slot SLOT1`);
}

function seedContainer(containerCode) {
  if (rowCount(`SELECT COUNT(*) FROM w_container WHERE container_code='${containerCode}' AND warehouse_code='WH001'`) > 0) {
    console.log(`[container] ${containerCode} already exists`);
    return;
  }
  const now = Date.now();
  const id = nextId();
  const slots = JSON.stringify([
    {
      containerSlotCode: 'SLOT1',
      containerSlotSpecCode: 'SLOT1',
      occupationRatio: 0,
      emptySlot: true,
      face: 'FACE1',
      locationCode: `LOC-${containerCode}-SLOT1`,
      children: [],
    },
  ]).replace(/'/g, "''");
  sql(
    `INSERT INTO w_container (id, create_time, create_user, update_time, update_user, ` +
      `container_code, container_slot_num, container_spec_code, container_status, empty_container, ` +
      `empty_slot_num, location_code, location_type, locked, opened, warehouse_area_id, ` +
      `warehouse_code, warehouse_logic_id, version, container_slots) VALUES ` +
      `(${id}, ${now}, 'admin', ${now}, 'admin', '${containerCode}', 1, 'RCV_SPEC', 'IN_SIDE', 1, ` +
      `1, 'LOC-${containerCode}', 'RACK', 0, 0, 1, 'WH001', 0, 0, '${slots}')`
  );
  console.log(`[container] created ${containerCode} (id ${id})`);
}

function setStationMode(mode) {
  sql(`UPDATE w_work_station SET work_station_mode='${mode}' WHERE id=1`);
  console.log(`[station] work_station_mode -> ${mode}`);
  // Spring-cache on the workstation entity + the @RedisHash station cache must
  // both be refreshed so ONLINE re-initializes from the new DB mode.
  clearRedis(['_wms:basic:work:station:cache::1', 'WorkStation:1']);
}

(async () => {
  const customerOrderNo = process.argv[2] || `INB_SMOKE_${Date.now()}`;
  const containerCode = process.argv[3] || `RCV_${Date.now()}`;

  seedInboundOrder(customerOrderNo);
  seedContainerSpec();
  seedContainer(containerCode);
  setStationMode('RECEIVE');

  console.log('\n[verify]');
  console.log(sql(`SELECT id, customer_order_no, inbound_plan_order_status, total_qty FROM w_inbound_plan_order WHERE customer_order_no='${customerOrderNo}'`));
  console.log(sql(`SELECT id, sku_code, qty_restocked, qty_accepted FROM w_inbound_plan_order_detail WHERE inbound_plan_order_id=(SELECT id FROM w_inbound_plan_order WHERE customer_order_no='${customerOrderNo}')`));
  console.log(sql(`SELECT container_code, container_spec_code, container_status FROM w_container WHERE container_code='${containerCode}'`));
  console.log(sql(`SELECT id, work_station_mode FROM w_work_station WHERE id=1`));
  console.log(`\n[ready] containerCode=${containerCode} customerOrderNo=${customerOrderNo}`);
})().catch((e) => { console.error(e); process.exit(1); });
