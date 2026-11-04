/**
 * Navigate to an authenticated route by driving the real sidebar menu.
 *
 * Why not page.goto(path)? On a full page load the app's TabsLayout
 * (handleAppChange) forces a redirect to the first tab (dashboard), so deep
 * links are unreliable. Clicking sidebar menu items performs an in-SPA
 * navigation that does not trigger that redirect — the same path a human
 * operator takes.
 *
 * Submenu expansion is resolved from the DOM structure (a menu item is an
 * <li> whose child header precedes a nested submenu <ul>/<div>), so it works
 * regardless of the UI locale.
 */
async function navigateTo(page, path) {
  const target = page.locator(`a[href="${path}"]`);
  for (let i = 0; i < 6; i++) {
    if (await target.isVisible().catch(() => false)) {
      // AMIS parent menu items render an <a onClick> that overlays the leaf
      // hit area, so a normal pointer click is intercepted. Dispatch the click
      // directly on the leaf anchor to trigger the SPA navigation.
      await page.evaluate((p) => {
        const t = document.querySelector(`a[href="${p}"]`);
        if (t) t.click();
      }, path);
      await page.waitForURL((u) => u.pathname === path, { timeout: 15_000 }).catch(() => {});
      await page.waitForLoadState('networkidle').catch(() => {});
      return;
    }

    // Expand the innermost COLLAPSED ancestor menu item that contains the target.
    const clicked = await page.evaluate((p) => {
      const t = document.querySelector(`a[href="${p}"]`);
      if (!t) return false;
      let el = t.parentElement;
      while (el) {
        if (el.tagName === 'LI') {
          const sub = el.querySelector(':scope > ul, :scope > div');
          if (sub && sub.offsetParent === null) {
            const header = el.firstElementChild;
            if (header) {
              header.click();
              return true;
            }
          }
        }
        el = el.parentElement;
      }
      return false;
    }, path);

    if (!clicked) break;
    await page.waitForTimeout(800);
  }
}

module.exports = { navigateTo };
