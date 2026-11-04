/**
 * Station API helpers for the picking-flow smoke tests.
 *
 * These drive the real station server (via the gateway) exactly like the
 * station UI does: GET /station/api?stationCode=<id> for state, PUT
 * /station/api?apiCode=<EVENT> for operator actions. `stationCode` is the
 * query param / header the HttpStationFilter reads (not X-WorkStation-Id).
 */
const { ApiClient } = require('./api-client');

const STATION_ID = process.env.TEST_WORKSTATION_ID || '1';

class StationApiClient extends ApiClient {
  /** GET the full station view (WorkStationCache). */
  async getView(stationId = STATION_ID) {
    const resp = await this.get(`/station/api?stationCode=${stationId}`);
    if (!resp.ok) throw new Error(`station getView failed: ${resp.status}`);
    return resp.json();
  }

  /** PUT an operator action. body is a plain string (text/plain). */
  async action(apiCode, body, stationId = STATION_ID) {
    const headers = await this.headers();
    const resp = await this.request.put(`${process.env.API_URL || 'http://localhost:8090'}/station/api?apiCode=${apiCode}&stationCode=${stationId}`, {
      headers: { ...headers, 'Content-Type': 'text/plain' },
      data: typeof body === 'string' ? body : JSON.stringify(body),
    });
    if (!resp.ok) throw new Error(`station ${apiCode} failed: ${resp.status} ${await resp.text()}`);
    return resp;
  }

  /**
   * Bring the station online (re-initializes the Redis cache from DB).
   * @param {string} mode  WorkStationModeEnum: 'PICKING' (default), 'RECEIVE', ...
   * @param {boolean} hasOrder  Whether the mode operates with an order.
   */
  async online(mode = 'PICKING', hasOrder = true, stationId = STATION_ID) {
    return this.action('ONLINE', JSON.stringify({ workStationMode: mode, hasOrder }), stationId);
  }

  /** Bind a transfer container to a WAITING_BINDING slot (2 INPUT scans). */
  async bindContainer(slotCode, containerCode, stationId = STATION_ID) {
    await this.action('INPUT', slotCode, stationId);
    await this.action('INPUT', containerCode, stationId);
  }

  /** Simulate the robot delivering the source container. */
  async containerArrived(containerCode, stationId = STATION_ID) {
    return this.action(
      'CONTAINER_ARRIVED',
      JSON.stringify({
        containerDetails: [
          { containerCode, face: 'FACE1', locationCode: 'LOC-' + containerCode, groupCode: 'G1', robotCode: 'ROBOT1', robotType: 'R', level: 1, bay: 1 },
        ],
        workLocationCode: 'JH01',
        workStationId: Number(stationId),
        warehouseAreaId: 1,
      }),
      stationId
    );
  }

  /** Scan a SKU barcode (marks tasks PROCESSING + slots DISPATCH). */
  async scanSku(skuCode, stationId = STATION_ID) {
    return this.action('SCAN_BARCODE', skuCode, stationId);
  }

  /** Tap a put-wall slot (complete pick or seal depending on state). */
  async tapSlot(slotCode, stationId = STATION_ID) {
    return this.action('TAP_PUT_WALL_SLOT', JSON.stringify({ putWallSlotCode: slotCode }), stationId);
  }

  /** Find the first slot in the given status, else null. */
  async findSlot(status, putWallIndex = 0, stationId = STATION_ID) {
    const view = await this.getView(stationId);
    const slots = view?.putWallArea?.putWallViews?.[putWallIndex]?.putWallSlots || [];
    return slots.find((s) => s.putWallSlotStatus === status) || null;
  }

  /** Return the pickingOrderId currently bound to a slot. */
  async slotPickingOrderId(slotCode, stationId = STATION_ID) {
    const view = await this.getView(stationId);
    const slot = (view?.putWallArea?.putWallViews?.[0]?.putWallSlots || []).find((s) => s.putWallSlotCode === slotCode);
    return slot?.pickingOrderId || null;
  }

  /**
   * Ensure at least one put-wall slot is WAITING_BINDING. If all slots are
   * busy, create a new outbound order and wait for the WES wave+dispatch
   * schedulers to assign it to a slot.
   *
   * Returns the first WAITING_BINDING slot code.
   */
  async ensureWaitingBindingSlot(timeoutMs = 110_000) {
    const existing = await this.findSlot('WAITING_BINDING');
    if (existing) return existing.putWallSlotCode;

    // Create an outbound order so the pipeline has work.
    const orderNo = `STA_E2E_${Date.now()}`;
    const createResp = await this.post('/wms/outbound/order/create', [
      {
        warehouseCode: 'WH001',
        customerOrderNo: orderNo,
        customerOrderType: 'SALES',
        details: [{ ownerCode: 'OWNER001', skuCode: 'SKU001', qtyRequired: 1 }],
      },
    ]);
    if (!createResp.ok) throw new Error(`create outbound order failed: ${createResp.status}`);

    const deadline = Date.now() + timeoutMs;
    while (Date.now() < deadline) {
      const slot = await this.findSlot('WAITING_BINDING');
      if (slot) return slot.putWallSlotCode;
      await new Promise((r) => setTimeout(r, 3000));
    }
    throw new Error(`no WAITING_BINDING slot within ${timeoutMs}ms (order ${orderNo})`);
  }
}

module.exports = { StationApiClient, STATION_ID };
