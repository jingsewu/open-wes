/**
 * Seed the standard E2E baseline (WH001 set) into a fresh environment.
 *
 * The scenario suites and seed-env/seed-inbound assume a dev baseline that the
 * Tencent demo server does not have: warehouse WH001, pick area id=1, station
 * id=1 / ST001, owner OWNER001, SKU001 and its batch stock. This script creates
 * only those missing base rows (idempotent, additive). It never touches other
 * warehouses (e.g. the demo warehouse w1).
 *
 * Usage: DB_HOST=... DB_PORT=... node seed-server-baseline.js
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

function count(stmt) {
  const line = sql(stmt).split('\n').filter(Boolean).find((l) => /^\d+$/.test(l.trim()));
  return Number(line);
}

const WAREHOUSE = 'WH001';
const OWNER = 'OWNER001';
const SKU = 'SKU001';
const STATION_CODE = 'ST001';

const WORK_LOCATIONS = JSON.stringify([
  { enable: true, stationCode: null, workLocationCode: 'JH01', workLocationType: 'ROBOT', workLocationSlots: null },
]);
const ALLOWED_MODES = JSON.stringify(['PICKING', 'RECEIVE']);

function seedWarehouse() {
  if (count(`SELECT COUNT(*) FROM m_warehouse_main_data WHERE warehouse_code='${WAREHOUSE}'`) > 0) {
    console.log(`[warehouse] ${WAREHOUSE} already exists`);
    return;
  }
  const now = Date.now();
  sql(
    `INSERT INTO m_warehouse_main_data (id, create_time, create_user, update_time, update_user, ` +
      `address, area, business_type, city, country, district, fax, height, mail, name, province, ` +
      `structure_type, tel, version, virtual_warehouse, warehouse_attr_type, warehouse_code, ` +
      `warehouse_label, warehouse_level, warehouse_name, warehouse_type) VALUES ` +
      `(1, ${now}, 'admin', ${now}, 'admin', NULL, 0, 'TOB', NULL, NULL, NULL, NULL, 0, NULL, ` +
      `'${WAREHOUSE}', NULL, 'FLOOR', NULL, 0, 0, 'NORMAL', '${WAREHOUSE}', NULL, 'A', ` +
      `'E2E Test Warehouse', 'DELIVERY_WAREHOUSE')`
  );
  console.log(`[warehouse] created ${WAREHOUSE} (id 1)`);
}

function seedArea() {
  if (count(`SELECT COUNT(*) FROM w_warehouse_area WHERE id=1`) > 0) {
    console.log('[area] id=1 already exists');
    return;
  }
  const now = Date.now();
  sql(
    `INSERT INTO w_warehouse_area (id, create_time, create_user, update_time, update_user, ` +
      `delete_time, deleted, enable, level, remark, temperature_limit, version, ` +
      `warehouse_area_code, warehouse_area_name, warehouse_area_type, warehouse_area_use, ` +
      `warehouse_area_work_type, warehouse_code, warehouse_group_code, wet_limit) VALUES ` +
      `(1, ${now}, 'admin', ${now}, 'admin', 0, 0, 1, 0, 'E2E test pick area', 0, 0, ` +
      `'AREA001', 'E2E Pick Area', 'STORAGE_AREA', 'PICK', 'ROBOT', '${WAREHOUSE}', '1', 0)`
  );
  console.log('[area] created AREA001 id=1 (WH001, PICK/ROBOT)');
}

function seedStation() {
  if (count(`SELECT COUNT(*) FROM w_work_station WHERE id=1`) > 0) {
    console.log('[station] id=1 already exists');
    return;
  }
  const now = Date.now();
  sql(
    `INSERT INTO w_work_station (id, create_time, create_user, update_time, update_user, ` +
      `allow_work_station_modes, delete_time, deleted, enable, position, station_code, station_name, ` +
      `version, warehouse_area_id, warehouse_code, work_locations, work_station_mode, work_station_status) VALUES ` +
      `(1, ${now}, 'admin', ${now}, 'admin', '${ALLOWED_MODES}', 0, 0, 1, ` +
      `'{"x":null,"y":null,"z":null}', '${STATION_CODE}', 'E2E Station 1', 0, 1, '${WAREHOUSE}', ` +
      `'${WORK_LOCATIONS}', 'PICKING', 'OFFLINE')`
  );
  console.log(`[station] created ${STATION_CODE} id=1 (area 1, PICKING/OFFLINE)`);
}

function seedOwner() {
  if (count(`SELECT COUNT(*) FROM m_owner_main_data WHERE owner_code='${OWNER}'`) > 0) {
    console.log(`[owner] ${OWNER} already exists`);
    return;
  }
  const now = Date.now();
  sql(
    `INSERT INTO m_owner_main_data (id, create_time, create_user, update_time, update_user, ` +
      `address, city, country, district, fax, mail, name, owner_code, owner_name, owner_type, ` +
      `province, tel, version, warehouse_code) VALUES ` +
      `(1, ${now}, 'admin', ${now}, 'admin', NULL, NULL, NULL, NULL, NULL, NULL, NULL, ` +
      `'${OWNER}', 'E2E Owner', 'SELF', NULL, NULL, 0, '${WAREHOUSE}')`
  );
  console.log(`[owner] created ${OWNER} (id 1)`);
}

function seedSku() {
  if (count(`SELECT COUNT(*) FROM m_sku_main_data WHERE sku_code='${SKU}'`) > 0) {
    console.log(`[sku] ${SKU} already exists`);
    return;
  }
  const now = Date.now();
  sql(
    `INSERT INTO m_sku_main_data (id, create_time, create_user, update_time, update_user, ` +
      `barcode_rule_code, brand, calculate_heat, color, effective_days, enable_effective, enable_sn, ` +
      `gross_weight, heat, height, image_url, length, max_stock, min_stock, net_weight, no_barcode, ` +
      `owner_code, shelf_life, size, sku_attribute_category, sku_attribute_sub_category, sku_code, ` +
      `sku_first_category, sku_name, sku_second_category, sku_third_category, style, unit, version, ` +
      `volume, warehouse_code, width) VALUES ` +
      `(1, ${now}, 'admin', ${now}, 'admin', NULL, NULL, 0, NULL, 0, 0, 0, 0, NULL, 0, NULL, 0, ` +
      `NULL, NULL, 0, 0, '${OWNER}', 0, NULL, NULL, NULL, '${SKU}', NULL, 'E2E Test SKU', NULL, ` +
      `NULL, NULL, 'EA', 0, 0, '${WAREHOUSE}', 0)`
  );
  console.log(`[sku] created ${SKU} (id 1, owner ${OWNER})`);
}

function seedBatchStock() {
  if (count(`SELECT COUNT(*) FROM w_sku_batch_stock WHERE sku_id=1`) > 0) {
    console.log('[batch_stock] sku_id=1 already exists');
    return;
  }
  const now = Date.now();
  sql(
    `INSERT INTO w_sku_batch_stock (id, create_time, create_user, update_time, update_user, ` +
      `available_qty, frozen_qty, no_outbound_locked_qty, outbound_locked_qty, sku_batch_attribute_id, ` +
      `sku_id, total_qty, version, warehouse_area_id, warehouse_code) VALUES ` +
      `(1, ${now}, 'admin', ${now}, 'admin', 100, 0, 0, 0, 0, 1, 100, 0, 1, '${WAREHOUSE}')`
  );
  console.log('[batch_stock] created id=1 sku_id=1 area=1 qty=100');
}

(async () => {
  seedWarehouse();
  seedArea();
  seedStation();
  seedOwner();
  seedSku();
  seedBatchStock();

  console.log('\n[verify]');
  console.log(sql(`SELECT id, warehouse_code, warehouse_name FROM m_warehouse_main_data WHERE warehouse_code='${WAREHOUSE}'`));
  console.log(sql(`SELECT id, warehouse_area_code, warehouse_area_use, warehouse_area_work_type FROM w_warehouse_area WHERE id=1`));
  console.log(sql(`SELECT id, station_code, work_station_mode, work_station_status, warehouse_area_id FROM w_work_station WHERE id=1`));
  console.log(sql(`SELECT id, owner_code FROM m_owner_main_data WHERE owner_code='${OWNER}'`));
  console.log(sql(`SELECT id, sku_code, owner_code FROM m_sku_main_data WHERE sku_code='${SKU}'`));
  console.log(sql(`SELECT id, sku_id, available_qty, warehouse_area_id FROM w_sku_batch_stock WHERE sku_id=1`));
})().catch((e) => { console.error(e); process.exit(1); });
