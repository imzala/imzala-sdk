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
  // Contract term fields (API 1.9.1) are appended after the last published
  // parameter; the leading ", " keeps the remaining list well formed.
  termStartMode: { decl: ', TermStartModeEnum? termStartMode = default', prop: 'TermStartMode' },
  termStartDate: { decl: ', DateOnly? termStartDate = default', prop: 'TermStartDate' },
  termDurationMonths: { decl: ', int? termDurationMonths = default', prop: 'TermDurationMonths' },
  termFixedEndDate: { decl: ', DateOnly? termFixedEndDate = default', prop: 'TermFixedEndDate' },
  renewalType: { decl: ', RenewalTypeEnum? renewalType = default', prop: 'RenewalType' },
  renewalPeriodMonths: { decl: ', int? renewalPeriodMonths = default', prop: 'RenewalPeriodMonths' },
  noticeDays: { decl: ', int? noticeDays = default', prop: 'NoticeDays' },
  reminderOffsets: { decl: ', List<int> reminderOffsets = default', prop: 'ReminderOffsets' },
  notifyCounterparty: { decl: ', bool notifyCounterparty = default', prop: 'NotifyCounterparty' },
};
const TERM = [
  'termStartMode', 'termStartDate', 'termDurationMonths', 'termFixedEndDate', 'renewalType',
  'renewalPeriodMonths', 'noticeDays', 'reminderOffsets', 'notifyCounterparty',
];
// On create an unset term field must not reach the wire: the server fills a
// missing field from the template, and the generated model would otherwise
// send null for every unset field and false for notify_counterparty, which
// overrides a template that notifies the counterparty. Emit them only when set.
const TERM_WIRE = [
  'term_start_mode', 'term_start_date', 'term_duration_months', 'term_fixed_end_date', 'renewal_type',
  'renewal_period_months', 'notice_days', 'reminder_offsets', 'notify_counterparty',
];
for (const [model, expectedCount, strip] of [
  ['CreateDemandRequest', 19, ['allowedSignatureVariants', 'documentVariables', ...TERM]],
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
  if (model === 'CreateDemandRequest') {
    for (const wire of TERM_WIRE) {
      const attr = '[DataMember(Name = "' + wire + '", EmitDefaultValue = true)]';
      if (!source.includes(attr)) throw new Error('Term attribute not found: ' + wire);
      source = source.replace(attr, '[DataMember(Name = "' + wire + '", EmitDefaultValue = false)]');
    }
  }
  fs.writeFileSync(file, source);
}
