"""Synthesize the soundtrack for claude-octopus.html (32 s, 44.1 kHz mono WAV).

Usage: python3 make_audio.py out.wav
"""
import sys
import wave

import numpy as np

SR = 44100
DUR = 32.0
N = int(SR * DUR)
out = np.zeros(N)
rng = np.random.default_rng(7)


def add(sig, start):
    i = int(start * SR)
    j = min(N, i + len(sig))
    if i < N:
        out[i:j] += sig[: j - i]


def env(n, a=0.005, r=0.1):
    t = np.arange(n) / SR
    e = np.minimum(1, t / max(a, 1e-4))
    return e * np.exp(-t / r)


def tone(freq, dur, kind="sine", a=0.005, r=0.3, vol=0.3):
    n = int(dur * SR)
    t = np.arange(n) / SR
    ph = 2 * np.pi * freq * t
    if kind == "sine":
        w = np.sin(ph)
    elif kind == "square":
        w = np.sign(np.sin(ph))
    elif kind == "tri":
        w = 2 / np.pi * np.arcsin(np.sin(ph))
    else:
        w = np.sin(ph) + 0.5 * np.sin(2 * ph) + 0.25 * np.sin(3 * ph)
    return w * env(n, a, r) * vol


def note(name):
    names = {"C": -9, "D": -7, "E": -5, "F": -4, "G": -2, "A": 0, "B": 2}
    base = names[name[0]]
    octave = int(name[-1])
    if "#" in name:
        base += 1
    return 440 * 2 ** ((base + (octave - 4) * 12) / 12)


# soft night pad for the whole piece (changes chord with the story)
def pad(chord, start, dur, vol=0.05):
    n = int(dur * SR)
    t = np.arange(n) / SR
    s = sum(np.sin(2 * np.pi * note(c) * t) for c in chord)
    fade = np.minimum(1, np.minimum(t / 0.8, (dur - t) / 0.8))
    add(s * fade * vol, start)


pad(["A3", "C4", "E4"], 0, 6.2)
pad(["D3", "F3", "A3"], 10.5, 2.2, 0.04)
pad(["F3", "A3", "C4", "E4"], 12.5, 4.8, 0.06)
pad(["C4", "E4", "G4"], 17, 5.6)
pad(["F3", "A3", "C4"], 22.5, 3)
pad(["C4", "E4", "G4", "C5"], 25.4, 5.2)

# keyboard clicks while typing
t = 0.8
while t < 5.7:
    n = int(0.03 * SR)
    click = rng.normal(0, 1, n) * env(n, 0.001, 0.006) * 0.25
    add(click, t)
    t += 0.07 + rng.random() * 0.09

# error alarm: harsh buzzer beeps + glitch noise
for k in range(8):
    add(tone(180 if k % 2 else 140, 0.25, "square", 0.002, 0.2, 0.09), 6.05 + k * 0.5)
for t0 in [6.5, 6.9, 7.3, 7.7, 8.1, 8.5, 8.9]:  # each popup
    add(tone(520, 0.12, "square", 0.001, 0.05, 0.06), t0)

# light rising: shimmering upward sweep
n = int(2.2 * SR)
tt = np.arange(n) / SR
sweep = np.sin(2 * np.pi * (200 * tt + 300 * tt ** 2)) * np.minimum(1, tt / 1.5) * 0.12
add(sweep, 10.4)
noise = rng.normal(0, 1, n) * np.minimum(1, tt / 2) * 0.03
add(noise, 10.4)

# magical arrival chime (arpeggio) + sparkles
for i, nm in enumerate(["C5", "E5", "G5", "C6", "E6", "G6"]):
    add(tone(note(nm), 1.5, "sine", 0.005, 0.6, 0.12), 12.6 + i * 0.12)
for i in range(18):
    add(tone(1800 + rng.random() * 2200, 0.3, "sine", 0.001, 0.08, 0.04), 13 + rng.random() * 4)
# beam whoosh at 15.2
n = int(1.4 * SR)
tt = np.arange(n) / SR
add(rng.normal(0, 1, n) * np.sin(np.pi * tt / 1.4) * 0.05, 15.2)

# fixing: friendly blips for each terminal line, then progress ticks
for i in range(6):
    add(tone(note(["E5", "G5", "A5", "C6", "D6", "E6"][i]), 0.2, "tri", 0.002, 0.08, 0.12), 17.5 + i * 0.5)
for k in range(12):
    add(tone(900 + k * 60, 0.05, "sine", 0.001, 0.03, 0.05), 20.4 + k * 0.15)

# success fanfare
for i, nm in enumerate(["C5", "E5", "G5"]):
    add(tone(note(nm), 0.25, "rich", 0.005, 0.2, 0.12), 22.5 + i * 0.13)
for nm in ["C5", "E5", "G5", "C6"]:
    add(tone(note(nm), 1.6, "rich", 0.01, 0.7, 0.08), 22.9)

# waving: gentle music-box melody
melody = ["G5", "E5", "C6", "G5", "A5", "G5", "E5", "D5", "C5"]
for i, nm in enumerate(melody):
    add(tone(note(nm), 0.8, "sine", 0.003, 0.35, 0.1), 25.8 + i * 0.45)

# ending chord
for nm in ["C4", "G4", "C5", "E5"]:
    add(tone(note(nm), 2.0, "sine", 0.05, 0.9, 0.07), 30.6)

# master fade and normalize
t = np.arange(N) / SR
out *= np.minimum(1, t / 0.5) * np.minimum(1, (DUR - t) / 0.8)
out = out / (np.max(np.abs(out)) + 1e-9) * 0.85
pcm = (out * 32767).astype(np.int16)

with wave.open(sys.argv[1], "wb") as w:
    w.setnchannels(1)
    w.setsampwidth(2)
    w.setframerate(SR)
    w.writeframes(pcm.tobytes())
