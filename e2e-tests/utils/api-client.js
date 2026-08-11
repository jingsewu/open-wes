const { getAuthToken } = require('./auth');

const BASE = process.env.API_URL || 'http://localhost:8090';

class ApiClient {
  constructor(request) {
    this.request = request;
    this.token = null;
  }

  /**
   * Inject an existing token (e.g. the browser session token from
   * localStorage.ws_token) so API calls reuse it instead of performing a second
   * signin — a fresh signin overwrites the per-user token in Redis and would
   * invalidate the browser session mid-test.
   */
  setToken(token) {
    this.token = token;
  }

  async headers() {
    if (!this.token) {
      this.token = await getAuthToken(this.request);
    }
    return {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${this.token}`,
      'X-WorkStation-Id': process.env.TEST_WORKSTATION_ID || '1',
    };
  }

  async get(path) {
    return this.request.get(`${BASE}${path}`, { headers: await this.headers() });
  }

  async post(path, data) {
    return this.request.post(`${BASE}${path}`, {
      headers: await this.headers(),
      data,
    });
  }

  async put(path, data) {
    return this.request.put(`${BASE}${path}`, {
      headers: await this.headers(),
      data,
    });
  }

  /**
   * Bean-searcher generic query used by all AMIS list pages.
   * Endpoint: POST /search/search (gateway route /search/** -> wes)
   * Example body: { searchIdentity, showColumns, searchObject }
   */
  async search(searchIdentity, filters = {}, showColumns = []) {
    const params = new URLSearchParams({ page: '1', perPage: '10' });
    for (const [key, value] of Object.entries(filters)) {
      params.append(`${key}-op`, 'eq');
      params.append(key, value);
    }
    return this.request.post(`${BASE}/search/search?${params.toString()}`, {
      headers: await this.headers(),
      data: {
        searchIdentity,
        showColumns,
        searchObject: {},
      },
    });
  }

  // -- Inbound --
  async createInboundPlanOrder(dto) {
    // NOTE: inbound plan order is created via the management UI in the smoke test;
    // a dedicated create API is not exercised here.
    return this.post('/wms/inbound/plan/create', dto);
  }

  async queryInboundPlanOrder(id) {
    return this.get(`/wms/inbound/plan/${id}`);
  }

  // -- Outbound --
  async createOutboundPlanOrder(dto) {
    return this.post('/wms/outbound/order/create', dto);
  }

  async queryOutboundPlanOrder(customerOrderNo) {
    return this.search('WOutboundPlanOrder', { customerOrderNo });
  }

  // -- Transfer Container --
  async bindContainer(dto) {
    // Station container bind flows through the station API (apiCode=INPUT);
    // not exercised via a plain REST call in the smoke test.
    return this.post('/station/api/transfer-container/bind', dto);
  }

  async sealContainer(dto) {
    return this.post('/station/api/transfer-container/seal', dto);
  }

  // -- Stock --
  async queryStock(skuCode) {
    return this.search('WContainerStock', { skuCode });
  }

  /**
   * Clear the bean-searcher dynamic-class cache. Calling search() with bad
   * showColumns permanently poisons a W{Entity} identity until this is run, so
   * smoke tests clear it before querying.
   */
  async clearSearchCache() {
    return this.post('/search/search/clearSearchMetaDataCache', {});
  }
}

module.exports = { ApiClient };
