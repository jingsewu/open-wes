/**
 * Create an outbound plan order via the real gateway API and poll its status.
 * Verifies the NEW -> ASSIGNED pre-allocation step of the picking pipeline.
 *
 * Usage: node create-outbound.js [skuCode] [qty] [customerOrderNo]
 */
const BASE = process.env.API_URL || 'http://localhost:8090';

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

async function createOrder(token, skuCode, qty, customerOrderNo) {
  const dto = {
    warehouseCode: 'WH001',
    customerOrderNo,
    customerOrderType: 'SALES',
    details: [
      {
        ownerCode: 'OWNER001',
        skuCode,
        qtyRequired: qty,
      },
    ],
  };
  const resp = await fetch(`${BASE}/wms/outbound/order/create`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify([dto]),
  });
  console.log(`[create] status=${resp.status}`);
  if (!resp.ok) {
    console.log(`[create] body=${await resp.text()}`);
    process.exit(1);
  }
}

async function pollStatus(token, customerOrderNo, timeoutMs = 15000) {
  const start = Date.now();
  while (Date.now() - start < timeoutMs) {
    const params = new URLSearchParams({
      page: '1',
      perPage: '10',
      'customerOrderNo-op': 'eq',
      customerOrderNo,
    });
    const resp = await fetch(`${BASE}/search/search?${params.toString()}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({
        searchIdentity: 'WOutboundPlanOrder',
        showColumns: [
          { name: 'customerOrderNo' },
          { name: 'orderNo' },
          { name: 'outboundPlanOrderStatus' },
          { name: 'waveNo' },
        ],
        searchObject: {},
      }),
    });
    if (resp.ok) {
      const data = await resp.json();
      const order = (data.items || []).find((o) => o.customerOrderNo === customerOrderNo);
      if (order) {
        console.log(`[poll] status=${order.outboundPlanOrderStatus} orderNo=${order.orderNo} waveNo=${order.waveNo || '-'}`);
        if (order.outboundPlanOrderStatus !== 'NEW') return order;
      }
    }
    await new Promise((r) => setTimeout(r, 1000));
  }
  console.log('[poll] timeout waiting for status change');
  return null;
}

(async () => {
  const sku = process.argv[2] || 'SKU001';
  const qty = Number(process.argv[3] || 1);
  const customerOrderNo = process.argv[4] || `API_TEST_${Date.now()}`;
  console.log(`[params] sku=${sku} qty=${qty} customerOrderNo=${customerOrderNo}`);
  const token = await login();
  await createOrder(token, sku, qty, customerOrderNo);
  await pollStatus(token, customerOrderNo);
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
