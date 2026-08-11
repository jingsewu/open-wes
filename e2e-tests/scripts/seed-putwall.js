/**
 * Seed the put-wall (播种墙) for the picking-flow smoke test.
 *
 * Creates (idempotently):
 *   1. A PUT_WALL container spec + a TRANSFER_CONTAINER spec (direct SQL).
 *   2. A put-wall bound to workstation ST001 (id=1) with N IDLE slots (via the
 *      real gateway API POST /wms/basic/putWall/createOrUpdate).
 *
 * Usage: node seed-putwall.js [slotCount] [putWallCode]
 */
const { execFileSync } = require('child_process');
const BASE = process.env.API_URL || 'http://localhost:8090';
const JDBC_JAR =
  process.env.MYSQL_JDBC_JAR ||
  'C:/Users/niekang/.gradle/caches/modules-2/files-2.1/com.mysql/mysql-connector-j/8.3.0/1cc7fa5d61f4bbc113531a4ba6d85d41cf3d57e1/mysql-connector-j-8.3.0.jar';

async function login() {
  const resp = await fetch(`${BASE}/user/api/auth/signin`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      username: process.env.TEST_USERNAME || 'admin',
      password: process.env.TEST_PASSWORD || '123456',
    }),
  });
  if (!resp.ok) throw new Error(`Login failed: ${resp.status} ${await resp.text()}`);
  const body = await resp.json();
  return body.token || body.access_token || body.data?.token;
}

/** Run a single SQL statement through SqlRunner. */
function sql(stmt) {
  const cwd = __dirname;
  const out = execFileSync('java', ['-cp', `.;${JDBC_JAR}`, 'SqlRunner', stmt], {
    cwd,
    encoding: 'utf-8',
    stdio: ['ignore', 'pipe', 'pipe'],
  });
  return out;
}

async function seedContainerSpecs() {
  const specs = [
    { code: 'PWS_SPEC', name: 'Put Wall Spec', type: 'PUT_WALL' },
    { code: 'TOTE_SPEC', name: 'Transfer Tote', type: 'TRANSFER_CONTAINER' },
  ];
  const now = Date.now();
  for (const s of specs) {
    // The row-count line looks like "(N rows)" — extract the actual count.
    const countLine = sql(`SELECT COUNT(*) FROM w_container_spec WHERE container_spec_code='${s.code}'`)
      .split('\n').filter(Boolean).find((l) => /^\d+$/.test(l.trim()));
    if (Number(countLine) > 0) {
      console.log(`[spec] ${s.code} already exists`);
      continue;
    }
    const id = BigInt(now) << 22n | BigInt(specs.indexOf(s) + 1);
    sql(
      `INSERT INTO w_container_spec (id, create_time, create_user, update_time, update_user, ` +
        `container_slot_num, container_spec_code, container_spec_name, container_type, ` +
        `height, length, volume, width, warehouse_code) VALUES ` +
        `(${id}, ${now}, 'admin', ${now}, 'admin', 1, '${s.code}', '${s.name}', '${s.type}', ` +
        `400, 300, 5000, 300, 'WH001')`
    );
    console.log(`[spec] created ${s.code}`);
  }
}

async function createPutWall(token, slotCount, putWallCode) {
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
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(dto),
  });
  const text = await resp.text();
  console.log(`[putwall] createOrUpdate status=${resp.status} body=${text.slice(0, 300)}`);
  if (!resp.ok) throw new Error(`Put wall creation failed: ${text}`);
  return true;
}

(async () => {
  const slotCount = Number(process.argv[2] || 4);
  const putWallCode = process.argv[3] || 'PW01';
  await seedContainerSpecs();
  const token = await login();
  await createPutWall(token, slotCount, putWallCode);

  // Verify via DB
  console.log('\n[verify]');
  console.log(sql(`SELECT id, put_wall_code, put_wall_name, put_wall_status, work_station_id, enable FROM w_put_wall`));
  console.log(sql(`SELECT put_wall_code, put_wall_slot_code, put_wall_slot_status, enable FROM w_put_wall_slot ORDER BY put_wall_slot_code`));
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
