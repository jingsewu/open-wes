import { APIRequestContext, APIResponse } from '@playwright/test';
import { getAuthToken } from './auth';

const BASE = process.env.API_URL || 'http://localhost:8090';

export class ApiClient {
  private token: string | null = null;

  constructor(private request: APIRequestContext) {}

  private async headers(): Promise<Record<string, string>> {
    if (!this.token) {
      this.token = await getAuthToken(this.request);
    }
    return {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${this.token}`,
      'X-WorkStation-Id': process.env.TEST_WORKSTATION_ID || '1',
    };
  }

  private async get(path: string): Promise<APIResponse> {
    return this.request.get(`${BASE}${path}`, { headers: await this.headers() });
  }

  private async post(path: string, data?: unknown): Promise<APIResponse> {
    return this.request.post(`${BASE}${path}`, {
      headers: await this.headers(),
      data,
    });
  }

  private async put(path: string, data?: unknown): Promise<APIResponse> {
    return this.request.put(`${BASE}${path}`, {
      headers: await this.headers(),
      data,
    });
  }

  // -- Inbound --
  async createInboundPlanOrder(dto: object): Promise<APIResponse> {
    return this.post('/inbound/api/inbound-plan-order', dto);
  }

  async queryInboundPlanOrder(id: number): Promise<APIResponse> {
    return this.get(`/inbound/api/inbound-plan-order/${id}`);
  }

  // -- Outbound --
  async createOutboundPlanOrder(dto: object): Promise<APIResponse> {
    return this.post('/outbound/api/outbound-plan-order', dto);
  }

  async queryOutboundPlanOrder(customerOrderNo: string): Promise<APIResponse> {
    return this.get(`/outbound/api/outbound-plan-order/${customerOrderNo}`);
  }

  // -- Transfer Container --
  async bindContainer(dto: object): Promise<APIResponse> {
    return this.post('/basic/api/transfer-container/bind', dto);
  }

  async sealContainer(dto: object): Promise<APIResponse> {
    return this.post('/basic/api/transfer-container/seal', dto);
  }

  // -- Stock --
  async queryStock(skuCode: string): Promise<APIResponse> {
    return this.get(`/stock/api/container-stock?skuCode=${skuCode}`);
  }
}
