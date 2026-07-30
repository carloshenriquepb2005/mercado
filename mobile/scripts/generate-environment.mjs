import { existsSync, readFileSync, writeFileSync } from 'node:fs';
import { resolve } from 'node:path';

const parseEnv = (file) => {
  if (!existsSync(file)) return {};
  return Object.fromEntries(
    readFileSync(file, 'utf8')
      .split(/\r?\n/)
      .filter((line) => line.trim() && !line.trim().startsWith('#') && line.includes('='))
      .map((line) => {
        const index = line.indexOf('=');
        return [line.slice(0, index).trim(), line.slice(index + 1).trim()];
      })
  );
};

const rootEnv = parseEnv(resolve('..', '.env'));
const localEnv = parseEnv(resolve('.env'));
const apiUrl = process.env.MOBILE_API_URL || localEnv.MOBILE_API_URL || rootEnv.MOBILE_API_URL || 'http://localhost:8080';
const safeApiUrl = apiUrl.replace(/['\\]/g, '');

writeFileSync(
  resolve('src', 'environments', 'environment.generated.ts'),
  `// Gerado por scripts/generate-environment.mjs. Não coloque segredos aqui.\nexport const environment = { production: true, apiUrl: '${safeApiUrl.replace(/\/$/, '')}' } as const;\n`,
  'utf8'
);

