#!/usr/bin/env bash
# Rebuild the Prime Host promo: frames -> soundtrack -> MP4
set -euo pipefail
cd "$(dirname "$0")"
pip install -q imageio-ffmpeg numpy scipy >/dev/null 2>&1 || true
FF=$(python3 -c "import imageio_ffmpeg;print(imageio_ffmpeg.get_ffmpeg_exe())")
rm -rf frames
node render.mjs --workers "${WORKERS:-4}" --fps 30
python3 audio.py soundtrack.wav
"$FF" -y -hide_banner -loglevel error -framerate 30 -i frames/f_%05d.jpg -i soundtrack.wav \
  -c:v libx264 -preset slow -crf 19 -pix_fmt yuv420p -profile:v high -movflags +faststart \
  -c:a aac -b:a 256k -shortest prime-host-promo.mp4
"$FF" -y -hide_banner -loglevel error -ss 3.6 -i prime-host-promo.mp4 -frames:v 1 -q:v 3 poster.jpg
echo "Done -> prime-host-promo.mp4"
