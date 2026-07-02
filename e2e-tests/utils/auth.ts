import { APIRequestContext } from '@playwright/test';

const AUTH_TOKEN_KEY = 'token';
const BASE_API_URL = process.env.API_URL || 'http://localhost:8090';

/**
 * Obtain auth token. Reads from env or performs login.
 * For CI, set AUTH_TOKEN env var directly.
 */
export async function getAuthToken(request: APIRequestContext): Promise<string> {
  const envToken = process.env.AUTH_TOKEN;
  if (envToken) return envToken;

  // Fallback: perform login
  const resp = await request.post(`${BASE_API_URL}/login`, {
    data: {
      username: process.env.TEST_USERNAME || 'admin',
      password: process.env.TEST_PASSWORD || 'admin',
    },
  });
  if (!resp.ok()) {
    throw new Error(`Login failed: ${resp.status()} ${await resp.text()}`);
  }
  const body = await resp.json();
  return body.token || body.access_token || body.data?.token;
}
