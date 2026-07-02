# Selector Notes

## AMIS-specific patterns
- Dialogs: `.cxd-Dialog`, `.cxd-Modal`
- Forms: `.cxd-Form`, `.cxd-FormItem`
- Tables: `.cxd-Table`, `.cxd-Table-content`
- Buttons: `.cxd-Button`, `.cxd-Button--primary`
- Inputs: `.cxd-TextControl-input`, `.cxd-Number-input`
- Combo-box: `.cxd-Combo`, `.cxd-Select`

## Known quirks
- AMIS re-renders frequently — use `waitForSelector` before interactions
- Some inputs are generated dynamically — use `locator('input')` with `nth()`
- Page transitions need `waitForLoadState('networkidle')`
