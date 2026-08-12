/**
 * Seed / repair the Open WES dev environment so the outbound picking pipeline
 * can run end-to-end. Idempotent — safe to run repeatedly.
 *
 * Fixes / creates (all via direct SQL unless noted):
 *   1. w_warehouse_area enum values: type=STORAGE_AREA, use=PICK, work_type=ROBOT
 *      (hand-seeded AREA001 used illegal enum strings that broke Hibernate reads)
 *   2. w_sku_batch_attribute row for SKU001 (was empty -> broke pre-allocate cache)
 *   3. w_sku_batch_stock.version = 0 (null version broke @Version optimistic locking)
 *   4. w_container + w_container_stock + w_location for the pick source CTN001
 *   5. Put-wall PW01 bound to workstation 1 with N IDLE slots (via real API)
 *
 * Usage: node seed-env.js [putWallSlotCount] [putWallCode] [containerCode]
 */
const { execFileSync } = require('child_process');
const JDBC_JAR =
  process.env.MYSQL_JDBC_JAR ||
  'C:/Users/niekang/.gradle/caches/modules-2/files-2.1/com.mysql/mysql-connector-j/8.3.0/1cc7fa5d61f4bbc113531a4ba6d85d41cf3d57e1/mysql-connector-j-8.3.0.jar';
const BASE = process.env.API_URL || 'http://localhost:8090';
const BATCH_ATTRIBUTE_ID = 7492941805684523009n; // fixed id used across scripts

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

function nextId() {
  return (BigInt(Date.now()) << 22n) + BigInt(Math.floor(Math.random() * 100));
}

async function login() {
  const resp = await fetch(`${BASE}/user/api/auth/signin`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: process.env.TEST_USERNAME || 'admin', password: process.env.TEST_PASSWORD || '123456' }),
  });
  if (!resp.ok) throw new Error(`Login failed: ${resp.status} ${await resp.text()}`);
  const body = await resp.json();
  return body.token || body.access_token || body.data?.token;
}

function fixWarehouseArea() {
  const row = sql(`SELECT COUNT(*) FROM w_warehouse_area WHERE id=1`).split('\n').filter(Boolean).find((l) => /^\d+$/.test(l.trim()));
  if (Number(row) === 0) {
    throw new Error('w_warehouse_area id=1 not found — seed the base warehouse/area first');
  }
  sql(`UPDATE w_warehouse_area SET warehouse_area_type='STORAGE_AREA', warehouse_area_use='PICK', warehouse_area_work_type='ROBOT', enable=1 WHERE id=1`);
  console.log('[area] fixed enum values on w_warehouse_area id=1');
}

function seedBatchAttribute() {
  if (rowCount(`SELECT COUNT(*) FROM w_sku_batch_attribute WHERE sku_id=1`) === 0) {
    const now = Date.now();
    sql(
      `INSERT INTO w_sku_batch_attribute (id, create_time, create_user, update_time, update_user, batch_no, sku_id, sku_attributes) ` +
        `VALUES (${BATCH_ATTRIBUTE_ID}, ${now}, 'admin', ${now}, 'admin', 'BATCH001', 1, '{}')`
    );
    console.log('[batch_attr] created w_sku_batch_attribute for sku_id=1');
  } else {
    console.log('[batch_attr] already exists');
  }
  // Link stock to the batch attribute and repair version for optimistic locking.
  sql(`UPDATE w_sku_batch_stock SET sku_batch_attribute_id=${BATCH_ATTRIBUTE_ID} WHERE sku_id=1`);
  sql(`UPDATE w_sku_batch_stock SET version=0 WHERE version IS NULL`);
  console.log('[stock] linked batch attribute + repaired version');
}

function seedContainerStock(containerCode, qty) {
  const now = Date.now();
  if (rowCount(`SELECT COUNT(*) FROM w_container WHERE container_code='${containerCode}'`) === 0) {
    const cid = nextId();
    sql(
      `INSERT INTO w_container (id, create_time, create_user, update_time, update_user, ` +
        `container_code, container_slot_num, container_spec_code, container_status, empty_container, ` +
        `empty_slot_num, location_code, location_type, locked, opened, warehouse_area_id, ` +
        `warehouse_code, warehouse_logic_id, version) VALUES ` +
        `(${cid}, ${now}, 'admin', ${now}, 'admin', '${containerCode}', 1, 'TOTE_SPEC', 'IN_SIDE', 0, ` +
        `1, 'LOC-${containerCode}', 'RACK', 0, 0, 1, 'WH001', 0, 0)`
    );
    console.log(`[container] created ${containerCode}`);
  }
  if (rowCount(`SELECT COUNT(*) FROM w_container_stock WHERE container_code='${containerCode}'`) === 0) {
    const csid = nextId();
    sql(
      `INSERT INTO w_container_stock (id, create_time, create_user, update_time, update_user, ` +
        `available_qty, box_no, box_stock, container_code, container_face, container_id, container_slot_code, ` +
        `frozen_qty, no_outbound_locked_qty, outbound_locked_qty, sku_batch_attribute_id, sku_batch_stock_id, ` +
        `sku_id, total_qty, version, warehouse_code) VALUES ` +
        `(${csid}, ${now}, 'admin', ${now}, 'admin', ${qty}, 'BOX1', 1, '${containerCode}', 'FACE1', ` +
        `(SELECT id FROM w_container WHERE container_code='${containerCode}'), 'SLOT1', ` +
        `0, 0, 0, ${BATCH_ATTRIBUTE_ID}, 1, 1, ${qty}, 0, 'WH001')`
    );
    console.log(`[container_stock] created ${containerCode} qty=${qty}`);
  }
  if (rowCount(`SELECT COUNT(*) FROM w_location WHERE shelf_code='${containerCode}'`) === 0) {
    const lid = nextId();
    sql(
      `INSERT INTO w_location (id, create_time, create_user, update_time, update_user, ` +
        `aisle_code, heat, location_code, location_status, location_type, occupied, position, shelf_code, ` +
        `version, warehouse_area_id, warehouse_code) VALUES ` +
        `(${lid}, ${now}, 'admin', ${now}, 'admin', 'AISLE1', 'A', 'LOC-${containerCode}', 'PUT_AWAY_PUT_DOWN', ` +
        `'RACK', 1, '{}', '${containerCode}', 0, 1, 'WH001')`
    );
    console.log(`[location] created LOC-${containerCode}`);
  }
}

async function seedPutWall(token, slotCount, putWallCode) {
  if (rowCount(`SELECT COUNT(*) FROM w_put_wall WHERE put_wall_code='${putWallCode}'`) > 0) {
    console.log(`[putwall] ${putWallCode} already exists`);
    return;
  }
  // Ensure container specs referenced by the put wall / tote exist.
  const specs = [
    { code: 'PWS_SPEC', type: 'PUT_WALL' },
    { code: 'TOTE_SPEC', type: 'TRANSFER_CONTAINER' },
  ];
  const now = Date.now();
  for (const s of specs) {
    if (rowCount(`SELECT COUNT(*) FROM w_container_spec WHERE container_spec_code='${s.code}'`) === 0) {
      const id = nextId();
      sql(
        `INSERT INTO w_container_spec (id, create_time, create_user, update_time, update_user, ` +
          `container_slot_num, container_spec_code, container_spec_name, container_type, ` +
          `height, length, volume, width, warehouse_code) VALUES ` +
          `(${id}, ${now}, 'admin', ${now}, 'admin', 1, '${s.code}', '${s.name}', '${s.type}', ` +
          `400, 300, 5000, 300, 'WH001')`
      );
    }
  }

  const slots = [];
  for (let i = 1; i <= slotCount; i++) {
    slots.push({
      putWallSlotCode: `${putWallCode}_S${i}`,
      ptlTag: `PTL_${i}`,
      face: 'LEFT',
      level: String(((i - 1) % 2) + 1),
      bay: String(Math.ceil(i / 2)),
      locLevel: ((i - 1) % 2) + 1,
      locBay: Math.ceil(i / 2),
      enable: true,
      putWallSlotStatus: 'IDLE',
    });
  }
  const dto = {
    warehouseCode: 'WH001',
    workStationId: 1,
    putWallCode,
    putWallName: 'Smoke Test Put Wall',
    containerSpecCode: 'PWS_SPEC',
    location: 'TEST-LOCATION',
    enable: true,
    putWallStatus: 'IDLE',
    displayOrder: 'LEFT_TO_RIGHT',
    putWallSlots: slots,
  };
  const resp = await fetch(`${BASE}/wms/basic/putWall/createOrUpdate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    body: JSON.stringify(dto),
  });
  const text = await resp.text();
  if (!resp.ok) throw new Error(`Put wall creation failed: ${resp.status} ${text}`);
  console.log(`[putwall] created ${putWallCode} with ${slotCount} slots`);
}

(async () => {
  const slotCount = Number(process.argv[2] || 4);
  const putWallCode = process.argv[3] || 'PW01';
  const containerCode = process.argv[4] || 'CTN001';
  const qty = Number(process.argv[5] || 100);

  fixWarehouseArea();
  seedBatchAttribute();
  seedContainerStock(containerCode, qty);
  const token = await login();
  await seedPutWall(token, slotCount, putWallCode);

  console.log('\n[verify]');
  console.log(sql(`SELECT id, warehouse_area_type, warehouse_area_use, warehouse_area_work_type FROM w_warehouse_area WHERE id=1`));
  console.log(sql(`SELECT container_code, available_qty FROM w_container_stock WHERE container_code='${containerCode}'`));
  console.log(sql(`SELECT put_wall_code, put_wall_status, work_station_id, enable FROM w_put_wall`));
  console.log(sql(`SELECT put_wall_slot_code, put_wall_slot_status FROM w_put_wall_slot ORDER BY put_wall_slot_code`));
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
