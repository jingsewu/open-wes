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
  // The Ant Design form is named "login", so fields render as id="login_<field>".
  await page.waitForSelector('input#login_username', { timeout: 20_000 });
  await page.fill('input#login_username', process.env.TEST_USERNAME || 'admin');
  await page.fill('input#login_password', process.env.TEST_PASSWORD || '123456');
  await page.click('button[type="submit"]');
  // After a successful login the app redirects away from /login.
  await page.waitForURL((url) => !url.pathname.includes('/login'), { timeout: 20_000 });
  await page.waitForLoadState('networkidle').catch(() => {});
}

module.exports = { uiLogin };
