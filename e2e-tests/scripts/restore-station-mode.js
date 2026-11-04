/**
 * Restore workstation 1 to PICKING mode after the inbound-receiving E2E, so the
 * outbound-picking E2E (which needs workStationMode=PICKING) still works.
 *
 * The inbound test flips the DB mode to RECEIVE; the receive page only mounts
 * when the station cache's workStationMode is RECEIVE. This script flips it
 * back and clears both the Spring-cache and the @RedisHash station cache.
 *
 * Idempotent. Usage: node restore-station-mode.js
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
function clearRedis(keys) {
  for (const k of keys) {
    try {
      execFileSync('node', [path.join(__dirname, 'redis-del.js'), k], {
        cwd: __dirname, encoding: 'utf-8', stdio: ['ignore', 'pipe', 'pipe'],
      });
    } catch (e) {
      /* no key — fine */
    }
  }
}

(async () => {
  sql(`UPDATE w_work_station SET work_station_mode='PICKING' WHERE id=1`);
  clearRedis(['_wms:basic:work:station:cache::1', 'WorkStation:1']);
  console.log('[restore] workstation 1 -> PICKING, station caches cleared');
})().catch((e) => { console.error(e); process.exit(1); });
