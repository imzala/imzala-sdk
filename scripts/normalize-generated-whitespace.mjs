import { readdirSync, readFileSync, writeFileSync } from 'node:fs';
import { extname, basename, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

// Text formats emitted by the five supported generators. Unknown formats and
// binary data stay byte-for-byte intact; generated Markdown hard breaks stay.
const TEXT_EXTENSIONS = new Set(['.md', '.ts', '.cs', '.java', '.py', '.php', '.json', '.yaml', '.yml', '.xml', '.sln', '.csproj', '.props', '.config', '.txt', '.rst', '.properties', '.sh', '.bat', '.ps1', '.toml', '.ini', '.cfg', '.gradle', '.sbt', '.dist', '.typed', '.gitignore']);
const TEXT_NAMES = new Set(['LICENSE', 'NOTICE', 'README', 'VERSION', 'FILES', 'Dockerfile', '.openapi-generator-ignore', '.gitignore', 'git_push.sh', 'gradlew', 'gradlew.bat']);
export function normalizeText(text, markdown = false) {
  const normalized = text.replace(/[ \t]+(?=\r?$)/gm, (tail, offset) => {
    const start = text.lastIndexOf('\n', offset - 1) + 1;
    const nonblank = text.slice(start, offset).trim().length > 0;
    return markdown && nonblank && tail.replace(/\t/g, ' ').length >= 2 ? '  ' : '';
  });
  // Generated Markdown tables can add surplus empty lines at EOF. Keep the
  // original terminal EOL style and hard-break spaces; never add an EOL.
  return markdown ? normalized.replace(/(\r?\n)(?:\r?\n)+$/, '$1') : normalized;
}
export function normalizeDirectory(directory) {
  let changed = 0;
  function visit(dir) {
    for (const entry of readdirSync(dir, { withFileTypes: true })) {
      const file = join(dir, entry.name);
      if (entry.isDirectory()) { visit(file); continue; }
      if (!entry.isFile() || (!TEXT_EXTENSIONS.has(extname(file)) && !TEXT_NAMES.has(basename(file)))) continue;
      const bytes = readFileSync(file);
      if (bytes.includes(0)) continue;
      let text; try { text = new TextDecoder('utf-8', { fatal: true, ignoreBOM: true }).decode(bytes); } catch { continue; }
      const normalized = normalizeText(text, extname(file) === '.md');
      if (text !== normalized) { writeFileSync(file, normalized); changed++; }
    }
  }
  visit(resolve(directory)); return changed;
}
if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  if (!process.argv[2]) throw new Error('Usage: normalize-generated-whitespace.mjs <generated directory>');
  console.log(`[generate] normalized whitespace in ${normalizeDirectory(process.argv[2])} text files`);
}
