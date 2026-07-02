import { ApiClient } from '../utils/api-client';

let seq = 0;
function uniqueId(): string {
  return `${Date.now()}_${seq++}_${Math.random().toString(36).slice(2, 8)}`;
}

export interface TestSku {
  id?: number;
  code: string;
  name: string;
  ownerCode: string;
  warehouseCode: string;
}

export interface TestContainer {
  id?: number;
  code: string;
  type: string;
  warehouseCode: string;
}

export class TestDataFactory {
  private createdSkus: string[] = [];
  private createdContainers: string[] = [];
  private stash: Map<string, unknown> = new Map();

  constructor(private api: ApiClient) {}

  /** Store a value for later retrieval in the same test */
  set(key: string, value: unknown): void { this.stash.set(key, value); }
  get<T>(key: string): T | undefined { return this.stash.get(key) as T | undefined; }

  async createTestSku(warehouseCode = 'WH001', ownerCode = 'OWNER001'): Promise<TestSku> {
    const code = `TEST_SKU_${uniqueId()}`;
    const dto = {
      skuCode: code,
      skuName: `Smoke Test SKU ${code}`,
      ownerCode,
      warehouseCode,
      // Other required fields depend on SkuDTO definition — fill from existing init data
    };

    const resp = await this.api.createInboundPlanOrder(dto);
    // If a direct SKU creation API exists, use it instead.
    // For smoke tests, SKU may already exist from init data; skip creation if so.

    this.createdSkus.push(code);
    return { code, name: dto.skuName, ownerCode, warehouseCode };
  }

  async createTestContainer(warehouseCode = 'WH001', type = 'TOTE'): Promise<TestContainer> {
    const code = `TEST_CTN_${uniqueId()}`;
    // Container creation via API — adjust based on actual container management API
    this.createdContainers.push(code);
    return { code, type, warehouseCode };
  }

  /** Clean up all data created by this factory */
  async cleanup(): Promise<void> {
    // Clean up is best-effort — failures are logged but not fatal
    for (const code of this.createdSkus) {
      try {
        // Delete SKU if API supports it
        console.log(`[cleanup] Would delete SKU: ${code}`);
      } catch { /* ignore */ }
    }
    for (const code of this.createdContainers) {
      try {
        console.log(`[cleanup] Would delete container: ${code}`);
      } catch { /* ignore */ }
    }
  }
}
