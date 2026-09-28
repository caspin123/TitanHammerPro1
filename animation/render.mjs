// Render claude-octopus.html frame by frame into an MP4.
// Usage: node render.mjs <ffmpeg> <audio.wav> <out.mp4> [fps]
// Needs the `playwright` package and a Chromium (set CHROME_PATH to override).
import { chromium } from 'playwright';
import { spawn } from 'child_process';
import path from 'path';
import { fileURLToPath } from 'url';

const [, , ffmpeg, audio, out, fpsArg] = process.argv;
const fps = Number(fpsArg || 30);
const here = path.dirname(fileURLToPath(import.meta.url));
const html = 'file://' + path.join(here, 'claude-octopus.html') + '?record=1';

const browser = await chromium.launch(process.env.CHROME_PATH ? { executablePath: process.env.CHROME_PATH } : {});
const page = await browser.newPage({ viewport: { width: 1280, height: 720 } });
await page.goto(html);
const duration = await page.evaluate(() => window.DURATION);

const ff = spawn(ffmpeg, [
  '-y', '-f', 'image2pipe', '-framerate', String(fps), '-c:v', 'png', '-i', '-',
  '-i', audio,
  '-c:v', 'libx264', '-preset', 'medium', '-crf', '20', '-pix_fmt', 'yuv420p',
  '-c:a', 'aac', '-b:a', '160k', '-shortest', '-movflags', '+faststart', out
], { stdio: ['pipe', 'inherit', 'inherit'] });

const total = Math.round(duration * fps);
for (let i = 0; i < total; i++) {
  const data = await page.evaluate(t => { renderAt(t); return document.getElementById('c').toDataURL('image/png'); }, i / fps);
  const buf = Buffer.from(data.split(',')[1], 'base64');
  if (!ff.stdin.write(buf)) await new Promise(r => ff.stdin.once('drain', r));
  if (i % 90 === 0) console.log(`frame ${i}/${total}`);
}
ff.stdin.end();
await new Promise(r => ff.on('close', r));
await browser.close();
console.log('done ->', out);
