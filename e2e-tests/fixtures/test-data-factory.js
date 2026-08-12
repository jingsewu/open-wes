let seq = 0;
function uniqueId() {
  return `${Date.now()}_${seq++}_${Math.random().toString(36).slice(2, 8)}`;
}

class TestDataFactory {
  constructor(api) {
    this.api = api;
    this.createdSkus = [];
    this.createdContainers = [];
    this.stash = new Map();
  }

  /** Store a value for later retrieval in the same test */
  set(key, value) { this.stash.set(key, value); }
  get(key) { return this.stash.get(key); }

  async createTestSku(warehouseCode = 'WH001', ownerCode = 'OWNER001') {
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

  async createTestContainer(warehouseCode = 'WH001', type = 'TOTE') {
    const code = `TEST_CTN_${uniqueId()}`;
    // Container creation via API — adjust based on actual container management API
    this.createdContainers.push(code);
    return { code, type, warehouseCode };
  }

  /** Clean up all data created by this factory */
  async cleanup() {
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

module.exports = { TestDataFactory };
