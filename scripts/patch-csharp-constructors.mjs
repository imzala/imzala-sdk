import fs from 'node:fs';
import path from 'node:path';

// New optional properties must not alter already published constructor ABI.
// Set AllowedSignatureVariants through the generated property/object initializer.
const directory = process.argv[2];
if (!directory) throw new Error('Usage: patch-csharp-constructors.mjs <generated directory>');
for (const [model, expectedCount] of [['CreateDemandRequest', 19], ['ApiV1DemandsBulkPostRequestOptions', 9]]) {
  const file = path.join(directory, 'src/ImzalaApiClient/Model', model + '.cs');
  let source = fs.readFileSync(file, 'utf8');
  const signature = new RegExp('public ' + model + '\\(([^\\n]*)\\)');
  const match = source.match(signature);
  if (!match) throw new Error('Constructor not found: ' + model);
  const insertion = 'List<AllowedSignatureVariantsEnum> allowedSignatureVariants = default, ';
  if (match[1].includes(insertion)) {
    source = source.replace(match[0], match[0].replace(insertion, ''));
    source = source.replace(/^.*<param name="allowedSignatureVariants">.*\n/m, '');
    source = source.replace('            this.AllowedSignatureVariants = allowedSignatureVariants;\n', '');
  }
  // Count defaults instead of commas: generic Dictionary parameters contain commas.
  const actual = source.match(signature)[1];
  if (actual.includes('allowedSignatureVariants') || (actual.match(/ = /g) ?? []).length !== expectedCount) {
    throw new Error('Unexpected constructor shape: ' + model);
  }
  if (!source.includes('public List<' + model + '.AllowedSignatureVariantsEnum> AllowedSignatureVariants { get; set; }')) {
    throw new Error('Signature variant property missing: ' + model);
  }
  fs.writeFileSync(file, source);
}
