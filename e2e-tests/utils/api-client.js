const { getAuthToken } = require('./auth');

const BASE = process.env.API_URL || 'http://localhost:8090';

class ApiClient {
  constructor(request) {
    this.request = request;
    this.token = null;
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

  // -- Inbound --
  async createInboundPlanOrder(dto) {
    return this.post('/inbound/api/inbound-plan-order', dto);
  }

  async queryInboundPlanOrder(id) {
    return this.get(`/inbound/api/inbound-plan-order/${id}`);
  }

  // -- Outbound --
  async createOutboundPlanOrder(dto) {
    return this.post('/outbound/api/outbound-plan-order', dto);
  }

  async queryOutboundPlanOrder(customerOrderNo) {
    return this.get(`/outbound/api/outbound-plan-order/${customerOrderNo}`);
  }

  // -- Transfer Container --
  async bindContainer(dto) {
    return this.post('/basic/api/transfer-container/bind', dto);
  }

  async sealContainer(dto) {
    return this.post('/basic/api/transfer-container/seal', dto);
  }

  // -- Stock --
  async queryStock(skuCode) {
    return this.get(`/stock/api/container-stock?skuCode=${skuCode}`);
  }
}

module.exports = { ApiClient };
