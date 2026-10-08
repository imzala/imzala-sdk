import { test } from 'vitest';
import assert from 'node:assert/strict';
import { mkdtempSync, mkdirSync, readFileSync, writeFileSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join, resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { spawnSync } from 'node:child_process';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '../..');
const models = [['CreateDemandRequest', 19], ['ApiV1DemandsBulkPostRequestOptions', 9]];
const directory = 'packages/dotnet/generated/src/ImzalaApiClient/Model';
const collection = ['DocumentCollectionEnabled', 'DocumentCollectionTemplateId', 'DocumentCollectionTiming', 'DocumentCollectionBlockSigning'];
function run(shape, mutate = (s) => s) {
  const dir = mkdtempSync(join(tmpdir(), 'imzala-csharp-contract-'));
  const path = join(dir, 'src/ImzalaApiClient/Model'); mkdirSync(path, { recursive: true });
  try {
    for (const [model] of models) {
      const file = `${directory}/${model}.cs`;
      let source = readFileSync(join(root, file), 'utf8');
      // Reconstruct raw pre-patch input from the checked generated properties.
      source = source.replace(/(DataMember\(Name = "(?:term_start_mode|term_start_date|term_duration_months|term_fixed_end_date|renewal_type|renewal_period_months|notice_days|reminder_offsets|notify_counterparty)", EmitDefaultValue = )false/g, '$1true');
      if (shape === 'old') {
        for (const prop of collection) source = source.replace(new RegExp('public [^\\n]* ' + prop + ' \\{ get; set; \\}', 'g'), '');
        source = source.replace(/(?:bool\?|Guid\?|DocumentCollectionTimingEnum\?) documentCollection\w+ = default, /g, '');
      } else if (!source.match(new RegExp('public ' + model + '\\(([^\\n]*)\\)'))[1].includes('documentCollectionEnabled')) {
        source = source.replace('public ' + model + '(', 'public ' + model + '(bool? documentCollectionEnabled = default, Guid? documentCollectionTemplateId = default, DocumentCollectionTimingEnum? documentCollectionTiming = default, bool? documentCollectionBlockSigning = default, ');
      }
      writeFileSync(join(path, `${model}.cs`), mutate(source));
    }
    const result = spawnSync(process.execPath, [join(root, 'scripts/patch-csharp-constructors.mjs'), dir], { encoding: 'utf8' });
    return { result, sources: models.map(([model]) => readFileSync(join(path, `${model}.cs`), 'utf8')) };
  } finally { rmSync(dir, { recursive: true, force: true }); }
}
for (const shape of ['old', 'new']) test(`preserves published constructor ABI for ${shape} generated shape`, () => {
  const { result, sources } = run(shape); assert.equal(result.status, 0, result.stderr);
  sources.forEach((s, index) => {
    const [model, count] = models[index]; const signature = s.match(new RegExp('public ' + model + '\\(([^\\n]*)\\)'))[1];
    assert.equal((signature.match(/ = /g) ?? []).length, count);
    for (const prop of collection) {
      assert.equal(signature.toLowerCase().includes(prop.toLowerCase()), false);
      if (shape === 'new') assert.match(s, new RegExp('public [^\\n]* ' + prop + ' \\{ get; set; \\}'));
    }
  });
});
test('unknown constructor addition is rejected rather than waived', () => {
  const { result } = run('new', s => s.replace(/(public (?:CreateDemandRequest|ApiV1DemandsBulkPostRequestOptions)\()/, '$1int futureField = default, '));
  assert.notEqual(result.status, 0); assert.match(result.stderr, /Unexpected constructor shape/);
});
test('incomplete collection property set is rejected', () => {
  const { result } = run('new', s => s.replace('DocumentCollectionEnabled { get; set; }', 'UnexpectedName { get; set; }'));
  assert.notEqual(result.status, 0); assert.match(result.stderr, /Incomplete collection shape/);
});
