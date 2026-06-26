import { expect, test } from '@playwright/test';

test.describe('Greeting cards management', () => {
  test('loads one server-side page of the seeded catalogue', async ({ page }) => {
    await page.goto('/');
    // Default page size is 20: server-side pagination, not all 1,200 rows.
    await expect(page.locator('table tbody tr')).toHaveCount(20);
  });

  test('creates, finds and deletes a card', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('table tbody tr').first()).toBeVisible();

    const title = `Playwright Card ${Date.now()}`;

    await page.getByRole('button', { name: /Add card/i }).click();
    const dialog = page.locator('mat-dialog-container');
    await dialog.getByLabel('Title').fill(title);
    await dialog.getByLabel('Category').click();
    await page.getByRole('option').first().click();
    await dialog.getByLabel('Artist').fill('Playwright Artist');
    await dialog.getByLabel(/Price/).fill('6.66');
    await dialog.getByLabel('Stock status').click();
    await page.getByRole('option', { name: 'In stock' }).click();
    await dialog.getByRole('button', { name: 'Add card' }).click();
    await expect(dialog).toBeHidden();

    await page.getByLabel('Search by title').fill(title);
    const row = page.locator('table tbody tr', { hasText: title });
    await expect(row).toHaveCount(1);

    await row.getByRole('button').click();
    const confirm = page.locator('mat-dialog-container');
    await confirm.getByRole('button', { name: 'Delete', exact: true }).click();
    await expect(confirm).toBeHidden();
    await expect(page.locator('table tbody tr', { hasText: title })).toHaveCount(0);
  });

  test('shows validation errors for an empty add form', async ({ page }) => {
    await page.goto('/');
    await page.getByRole('button', { name: /Add card/i }).click();

    const dialog = page.locator('mat-dialog-container');
    await dialog.getByRole('button', { name: 'Add card' }).click();

    await expect(dialog.getByText('Title is required')).toBeVisible();
    await expect(dialog).toBeVisible();
  });
});
