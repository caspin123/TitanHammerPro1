"""Procedural soundtrack for the Prime Host promo (royalty-free, synced to the timeline).
Usage: python3 audio.py out.wav
"""
import sys
import wave
import numpy as np

SR, DUR = 48000, 30.0
N = int(SR * DUR)
t = np.arange(N) / SR
rng = np.random.default_rng(7)
L = np.zeros(N)
R = np.zeros(N)


def env(start, attack, hold, release):
    e = np.zeros(N)
    a0, a1 = int(start * SR), int((start + attack) * SR)
    h1, r1 = int((start + attack + hold) * SR), int((start + attack + hold + release) * SR)
    e[a0:a1] = np.linspace(0, 1, max(1, a1 - a0))[: len(e[a0:a1])]
    e[a1:h1] = 1
    e[h1:r1] = np.linspace(1, 0, max(1, r1 - h1))[: len(e[h1:r1])] ** 2
    return e


def lowpass(x, cutoff):
    """FFT brick-ish lowpass with soft knee; cutoff may be scalar."""
    X = np.fft.rfft(x)
    f = np.fft.rfftfreq(len(x), 1 / SR)
    X *= 1 / (1 + (f / cutoff) ** 4)
    return np.fft.irfft(X, len(x))


def add(sig, pan=0.0, gain=1.0):
    global L, R
    L += sig * gain * np.sqrt(0.5 * (1 - pan))
    R += sig * gain * np.sqrt(0.5 * (1 + pan))


def hz(n):
    return 440 * 2 ** ((n - 69) / 12)


# ---- cinematic pad: Am -> F -> C -> G progression, slow swells ----
chords = [(0, [45, 52, 57, 60, 64]), (8, [41, 48, 53, 57, 60]), (15.5, [48, 55, 60, 64, 67]), (21.5, [43, 50, 55, 59, 62]), (26, [45, 52, 57, 60, 64, 69])]
for i, (st, notes) in enumerate(chords):
    en = chords[i + 1][0] if i + 1 < len(chords) else DUR
    e = env(max(0, st - 0.4), 1.6, en - st - 0.8, 1.6)
    for k, n in enumerate(notes):
        f = hz(n)
        det = 1.003 + 0.001 * k
        s = (np.sin(2 * np.pi * f * t) + 0.5 * np.sin(2 * np.pi * f * det * t + k) + 0.25 * np.sin(2 * np.pi * 2 * f * t)) * e
        s *= 0.55 + 0.45 * np.sin(2 * np.pi * (0.13 + 0.03 * k) * t + k)
        add(s, pan=(k / (len(notes) - 1) - 0.5) * 0.8, gain=0.018)

# master pad fade in
intro = np.clip(t / 2.5, 0, 1)
L *= intro
R *= intro

# ---- sub drone ----
add(np.sin(2 * np.pi * 55 * t) * env(0, 2, 26.5, 1.5) * 0.9, gain=0.05)

# ---- risers before each transition ----
for at in [2.4, 4.4, 7.9, 12.4, 16.9, 21.4, 25.9]:
    length = 1.1
    e = env(at - length, length, 0.0, 0.12) ** 1.6
    noise = rng.standard_normal(N) * e
    # rising brightness: mix low- and high-passed versions over time
    lo = lowpass(noise, 900)
    hi = noise - lowpass(noise, 3500)
    k = np.clip((t - (at - length)) / length, 0, 1)
    add(lo * (1 - k) * 0.6 + hi * k, pan=-0.3, gain=0.05)
    add(lo * (1 - k) * 0.6 + hi * k, pan=0.3, gain=0.05)

# ---- whooshes on transitions ----
for at in [4.5, 7.6, 12.3, 16.8, 21.3, 25.8]:
    e = env(at - 0.25, 0.25, 0.0, 0.55)
    w = lowpass(rng.standard_normal(N) * e, 2200)
    add(w, pan=-0.6, gain=0.14)
    add(np.roll(w, 900), pan=0.6, gain=0.14)

# ---- impacts (crown landing, outro) ----
for at, g in [(2.5, 1.0), (27.35, 1.0), (8.05, 0.45), (12.8, 0.4), (17.1, 0.45), (21.8, 0.4)]:
    i0 = int(at * SR)
    tt = t[i0:] - at
    boom = np.sin(2 * np.pi * (38 + 60 * np.exp(-tt * 18)) * tt) * np.exp(-tt * 3.2)
    click = rng.standard_normal(len(tt)) * np.exp(-tt * 40)
    s = np.zeros(N)
    s[i0:] = boom + 0.25 * lowpass(np.pad(click, (i0, 0)), 5000)[i0:]
    add(s, gain=0.42 * g)

# ---- shimmer bells on logo shines / key reveals ----
bell_notes = [81, 84, 88, 93]
for at in [2.95, 6.45, 27.55, 28.7]:
    for j, n in enumerate(bell_notes):
        st = at + j * 0.07
        i0 = int(st * SR)
        tt = t[i0:] - st
        f = hz(n)
        b = (np.sin(2 * np.pi * f * tt) + 0.4 * np.sin(2 * np.pi * f * 2.76 * tt)) * np.exp(-tt * 2.2)
        s = np.zeros(N)
        s[i0:] = b
        add(s, pan=(j - 1.5) * 0.4, gain=0.035)

# ---- soft pulse (100 BPM) during feature scenes ----
beat = 60 / 100
bt = 8.05
while bt < 25.8:
    i0 = int(bt * SR)
    tt = t[i0:] - bt
    k = np.sin(2 * np.pi * (48 + 40 * np.exp(-tt * 30)) * tt) * np.exp(-tt * 9)
    hat = rng.standard_normal(len(tt)) * np.exp(-tt * 60)
    s = np.zeros(N)
    s[i0:] = k
    add(s, gain=0.16)
    h = np.zeros(N)
    ih = int((bt + beat / 2) * SR)
    if ih < N:
        th = t[ih:] - (bt + beat / 2)
        h[ih:] = rng.standard_normal(len(th)) * np.exp(-th * 70)
        h = h - lowpass(h, 7000)
        add(h, pan=0.25, gain=0.03)
    bt += beat

# ---- counter ticks ----
for st, en in [(8.8, 10.3), (18.2, 19.7)]:
    x = st
    while x < en:
        i0 = int(x * SR)
        tt = t[i0:i0 + 2000] - x
        s = np.zeros(N)
        s[i0:i0 + len(tt)] = np.sin(2 * np.pi * 2600 * tt) * np.exp(-tt * 300)
        add(s, pan=0.2, gain=0.02)
        x += 0.05 + 0.1 * ((x - st) / (en - st)) ** 2

# ---- master: fade out, simple stereo width, soft clip, normalise ----
fade = np.clip((DUR - t) / 0.9, 0, 1)
L *= fade
R *= fade
mix = np.stack([L, R], 1)
mix = np.tanh(mix * 1.6) / np.tanh(1.6)
mix /= np.max(np.abs(mix)) + 1e-9
mix *= 0.89
pcm = (mix * 32767).astype(np.int16)

with wave.open(sys.argv[1] if len(sys.argv) > 1 else 'soundtrack.wav', 'wb') as w:
    w.setnchannels(2)
    w.setsampwidth(2)
    w.setframerate(SR)
    w.writeframes(pcm.tobytes())
print('ok')
