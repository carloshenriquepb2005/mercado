import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'br.com.mercado.precificacao',
  appName: 'SisBac',
  webDir: 'www/browser',
  server: {
    androidScheme: 'http',
    cleartext: true
  }
};

export default config;
