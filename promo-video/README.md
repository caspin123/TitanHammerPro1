# Prime Host — SaaS Promo Video

![poster](poster.jpg)

**`prime-host-promo.mp4`** — 30s · 1920×1080 · 30fps · H.264 + AAC stereo soundtrack.

Style: **Liquid Glass** panels + **Claymorphism** icons on a warm black / metallic-gold palette taken from the logo.
Full storyboard and design decisions: [DESIGN_PLAN.md](DESIGN_PLAN.md).

| File | Purpose |
|---|---|
| `index.html` | The whole animation (HTML/CSS + GSAP timeline + canvas FX). Open it in a browser for a live, looping preview. All texts/prices live in the `CONFIG` object at the top. |
| `render.mjs` | Deterministic frame renderer (Playwright/Chromium, seeks the timeline frame by frame, parallel workers). |
| `audio.py` | Procedural, royalty-free cinematic soundtrack (106.67 BPM, locked to scene cuts): braam hits, bells, side-chained pad, arp, kick/clap/hats, convolution reverb, mastered to ~-15 LUFS. |
| `build.sh` | One command: frames → audio → MP4 + poster. |
| `assets/` | Logo (upscaled, split into crown/body for animation), world land-dot map for the globe. |

## Preview / edit
```bash
cd promo-video && python3 -m http.server 8080   # open http://localhost:8080
```
Edit `CONFIG` in `index.html` (headlines, features, plans, prices, CTA), then rebuild:
```bash
./build.sh           # needs node + playwright (chromium) and python3
```
Single stills: `node render.mjs --stills 3.6,19.5 --out stills`.

> Prices and stats (99.99%, +15 data centers, etc.) are placeholders — replace them with your real offers.
