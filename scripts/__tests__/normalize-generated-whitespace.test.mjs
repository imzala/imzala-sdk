import { test, expect } from 'vitest';
import { mkdtempSync, writeFileSync, readFileSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { normalizeText, normalizeDirectory } from '../normalize-generated-whitespace.mjs';
test('code/config text loses only trailing horizontal whitespace, preserving CRLF', () => {
  expect(normalizeText('const s = "a  b"; \t\r\n\t \r\nnext\n')).toBe('const s = "a  b";\r\n\r\nnext\n');
});
test('Markdown retains intentional hard breaks and cleans accidental one-space/blank tails', () => {
  expect(normalizeText('hard    \naccidental \n  \nnext\t\t\n', true)).toBe('hard  \naccidental\n\nnext  \n');
});
test('only supported UTF-8 text changes; binary/unknown formats remain byte-identical; rerun is idempotent', () => {
  const dir = mkdtempSync(join(tmpdir(), 'imzala-generated-format-'));
  try {
    const binary = Buffer.from([0, 255, 32, 10]);
    writeFileSync(join(dir, 'example.cs'), 'text \n');
    writeFileSync(join(dir, 'example.md'), 'hard   \n');
    writeFileSync(join(dir, 'binary.json'), binary);
    writeFileSync(join(dir, 'unknown.bin'), 'text \n');
    expect(normalizeDirectory(dir)).toBe(2);
    expect(readFileSync(join(dir, 'binary.json'))).toEqual(binary);
    expect(readFileSync(join(dir, 'unknown.bin'), 'utf8')).toBe('text \n');
    expect(normalizeDirectory(dir)).toBe(0);
  } finally { rmSync(dir, { recursive: true, force: true }); }
});

test('text normalization preserves a UTF-8 BOM', () => {
  const dir = mkdtempSync(join(tmpdir(), 'imzala-generated-bom-'));
  try {
    const file = join(dir, 'example.sln'); writeFileSync(file, '\uFEFFheader \r\n');
    expect(normalizeDirectory(dir)).toBe(1);
    expect(readFileSync(file, 'utf8')).toBe('\uFEFFheader\r\n');
  } finally { rmSync(dir, { recursive: true, force: true }); }
});

test('Markdown removes surplus terminal empty lines, preserving interior blanks and CRLF', () => {
  expect(normalizeText('first\n\nlast  \n\n', true)).toBe('first\n\nlast  \n');
  expect(normalizeText('last\r\n\r\n', true)).toBe('last\r\n');
  expect(normalizeText('code\n\n')).toBe('code\n\n');
  expect(normalizeText('', true)).toBe('');
  expect(normalizeText('\uFEFFlast\r\n\r\n', true)).toBe('\uFEFFlast\r\n');
  const once = normalizeText('last  \n\n', true);
  expect(normalizeText(once, true)).toBe(once);
});
