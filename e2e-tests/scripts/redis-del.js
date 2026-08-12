/**
 * Minimal Redis client (raw RESP) to delete cache keys or scan keys in the
 * dev environment.
 *
 * Usage:
 *   node redis-del.js <key> [key2 ...]   -- delete keys
 *   node redis-del.js --scan <pattern>   -- list keys matching pattern
 */
const net = require('net');

const HOST = process.env.REDIS_HOST || '192.168.127.129';
const PORT = Number(process.env.REDIS_PORT || 6379);
// The Open WES Nacos config pins Redis to database 3 (redisson singleServerConfig.database).
const DB = Number(process.env.REDIS_DB || 3);

function encode(args) {
  let out = `*${args.length}\r\n`;
  for (const a of args) {
    const buf = Buffer.from(String(a), 'utf-8');
    out += `$${buf.length}\r\n${buf.toString('utf-8')}\r\n`;
  }
  return out;
}

function runCommand(command) {
  return new Promise((resolve, reject) => {
    const sock = net.connect(PORT, HOST, () => {
      sock.write(encode(['SELECT', DB]));
      sock.write(encode(command));
    });
    let data = '';
    const timer = setTimeout(() => {
      sock.destroy();
      resolve('(timeout)');
    }, 8000);
    sock.on('data', (c) => {
      data += c.toString();
      clearTimeout(timer);
      sock.destroy();
      resolve(data);
    });
    sock.on('error', (e) => {
      clearTimeout(timer);
      reject(e);
    });
  });
}

function parseArrayReply(data) {
  // RESP array reply: *N \r\n ($len \r\n value \r\n)*
  const lines = data.split('\r\n').filter((l) => l.length > 0);
  const values = [];
  let i = 0;
  while (i < lines.length) {
    const l = lines[i];
    if (l.startsWith('$')) {
      values.push(lines[i + 1]);
      i += 2;
    } else {
      i++;
    }
  }
  return values;
}

(async () => {
  const args = process.argv.slice(2);
  if (args[0] === '--scan') {
    const pattern = args[1] || '*';
    const reply = await runCommand(['SCAN', '0', 'MATCH', pattern, 'COUNT', '500']);
    const keys = parseArrayReply(reply);
    console.log(`[redis] keys matching "${pattern}":`);
    keys.forEach((k) => console.log('  ' + k));
    process.exit(0);
  }
  if (args.length === 0) {
    console.error('Usage: node redis-del.js <key> [key2 ...] | --scan <pattern>');
    process.exit(1);
  }
  const reply = await runCommand(['DEL', ...args]);
  console.log(`[redis] DEL ${args.length} key(s) -> ${reply.trim()}`);
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
