// Deterministic frame renderer: seeks the GSAP timeline frame-by-frame in headless Chromium.
// Usage: node render.mjs [--fps 30] [--workers 4] [--from 0] [--to 30] [--out frames] [--stills 2,6,10]
import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { createRequire } from 'node:module';
import { fileURLToPath } from 'node:url';

const require = createRequire(import.meta.url);
let playwright;
try { playwright = require('playwright'); } catch { playwright = require('/opt/node22/lib/node_modules/playwright'); }

const ROOT = path.dirname(fileURLToPath(import.meta.url));
const arg = (k, d) => { const i = process.argv.indexOf('--' + k); return i > -1 ? process.argv[i + 1] : d; };
const FPS = +arg('fps', 30), WORKERS = +arg('workers', 4), OUT = path.resolve(ROOT, arg('out', 'frames'));
const STILLS = arg('stills', null);

const MIME = { '.html': 'text/html', '.js': 'text/javascript', '.css': 'text/css', '.png': 'image/png', '.woff2': 'font/woff2' };
const server = http.createServer((req, res) => {
  const f = path.join(ROOT, decodeURIComponent(req.url.split('?')[0]));
  if (!f.startsWith(ROOT) || !fs.existsSync(f) || fs.statSync(f).isDirectory()) { res.writeHead(404); return res.end(); }
  res.writeHead(200, { 'Content-Type': MIME[path.extname(f)] || 'application/octet-stream' });
  fs.createReadStream(f).pipe(res);
}).listen(0);
const PORT = server.address().port;

const browser = await playwright.chromium.launch({ args: ['--disable-gpu-vsync', '--force-color-profile=srgb', '--font-render-hinting=none'] });
async function newPage() {
  const page = await browser.newPage({ viewport: { width: 1920, height: 1080 }, deviceScaleFactor: 1 });
  page.on('pageerror', e => console.error('PAGE ERROR', e.message));
  await page.goto(`http://127.0.0.1:${PORT}/index.html?render=1`);
  await page.evaluate(() => window.ready);
  return page;
}

fs.mkdirSync(OUT, { recursive: true });

if (STILLS) {
  const page = await newPage();
  for (const t of STILLS.split(',').map(Number)) {
    await page.evaluate(t => renderAt(t), t);
    await page.screenshot({ path: path.join(OUT, `still_${String(t).padStart(5, '0')}.jpg`), type: 'jpeg', quality: 90 });
  }
} else {
  const dur = await (await newPage()).evaluate(() => window.DUR);
  const from = +arg('from', 0), to = +arg('to', dur);
  const f0 = Math.round(from * FPS), f1 = Math.round(to * FPS);
  const total = f1 - f0, per = Math.ceil(total / WORKERS);
  let done = 0; const t0 = Date.now();
  await Promise.all(Array.from({ length: WORKERS }, async (_, w) => {
    const page = await newPage();
    const a = f0 + w * per, b = Math.min(f1, a + per);
    for (let f = a; f < b; f++) {
      await page.evaluate(t => renderAt(t), f / FPS);
      await page.screenshot({ path: path.join(OUT, `f_${String(f).padStart(5, '0')}.jpg`), type: 'jpeg', quality: 94 });
      if (++done % 30 === 0) console.log(`${done}/${total} frames  ${((Date.now() - t0) / 1000).toFixed(0)}s`);
    }
  }));
}
await browser.close();
server.close();
