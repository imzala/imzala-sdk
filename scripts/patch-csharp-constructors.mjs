import fs from 'node:fs';
import path from 'node:path';

// New optional properties must not alter already published constructor ABI.
// Set these through the generated property/object initializer instead.
const directory = process.argv[2];
if (!directory) throw new Error('Usage: patch-csharp-constructors.mjs <generated directory>');
const STRIP = {
  allowedSignatureVariants: { decl: 'List<AllowedSignatureVariantsEnum> allowedSignatureVariants = default, ', prop: 'AllowedSignatureVariants' },
  documentVariables: {
    decl: 'Dictionary<string, Dictionary<string, PartyMappingInputVariablesValue>> documentVariables = default, ',
    prop: 'DocumentVariables',
  },
};
for (const [model, expectedCount, strip] of [
  ['CreateDemandRequest', 19, ['allowedSignatureVariants', 'documentVariables']],
  ['ApiV1DemandsBulkPostRequestOptions', 9, ['allowedSignatureVariants']],
]) {
  const file = path.join(directory, 'src/ImzalaApiClient/Model', model + '.cs');
  let source = fs.readFileSync(file, 'utf8');
  const signature = new RegExp('public ' + model + '\\(([^\\n]*)\\)');
  if (!source.match(signature)) throw new Error('Constructor not found: ' + model);
  for (const name of strip) {
    const { decl, prop } = STRIP[name];
    const match = source.match(signature);
    if (match[1].includes(decl)) {
      source = source.replace(match[0], match[0].replace(decl, ''));
      source = source.replace(new RegExp('^.*<param name="' + name + '">.*\\n', 'm'), '');
      source = source.replace('            this.' + prop + ' = ' + name + ';\n', '');
    }
    if (!new RegExp('public [^\\n]* ' + prop + ' \\{ get; set; \\}').test(source)) {
      throw new Error(prop + ' property missing: ' + model);
    }
  }
  // Count defaults instead of commas: generic Dictionary parameters contain commas.
  const actual = source.match(signature)[1];
  if (strip.some((n) => actual.includes(n)) || (actual.match(/ = /g) ?? []).length !== expectedCount) {
    throw new Error('Unexpected constructor shape: ' + model);
  }
  fs.writeFileSync(file, source);
}
