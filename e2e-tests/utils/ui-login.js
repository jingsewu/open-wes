/**
 * Drive the real frontend login page (AMIS/React LoginForm) to authenticate
 * the browser session before exercising UI flows.
 *
 * The LoginForm posts to /user/api/auth/signin (prefixed with /gw by the
 * request interceptor and proxied to the gateway), so this drives the same
 * real login path a human operator uses.
 */
async function uiLogin(page) {
  await page.goto('/login');
  // This Ant Design version renders form fields as id="<form>_<field>" without a `name` attribute.
  await page.waitForSelector('input#basic_username', { timeout: 20_000 });
  await page.fill('input#basic_username', process.env.TEST_USERNAME || 'admin');
  await page.fill('input#basic_password', process.env.TEST_PASSWORD || '123456');
  await page.click('button[type="submit"]');
  // After a successful login the app redirects away from /login.
  await page.waitForURL((url) => !url.pathname.includes('/login'), { timeout: 20_000 });
  await page.waitForLoadState('networkidle').catch(() => {});
}

module.exports = { uiLogin };
