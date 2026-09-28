"""Procedural cinematic soundtrack for the Prime Host promo (royalty-free, synced to the timeline).

Tempo is 106.67 BPM so every scene cut (8.0 / 12.5 / 17 / 21.5 / 26 s) lands exactly on a bar line.
Layers: dark intro swell -> cinematic hit (crown) -> warm pad + soft plucks -> groove (kick, sub,
arp, soft clap/hats, side-chained pad) -> breakdown -> final hit + resolve, plus a motion-SFX layer
synced to the on-screen animation (whooshes, soft UI pops, clay bloops, impacts). Everything goes
through a stereo convolution reverb and a gentle master chain (quiet, ~-18 LUFS, soft top end).

Usage: python3 audio.py out.wav
"""
import sys
import wave

import numpy as np
from scipy import signal

SR = 48000
DUR = 30.0
N = int(SR * DUR)
T = np.arange(N) / SR
rng = np.random.default_rng(11)

MASTER_GAIN = 10 ** (-4.0 / 20)
BEAT = 60 / 106.6667          # 0.5625 s
GROOVE_START, GROOVE_END = 8.0, 26.0
HIT1, HIT2 = 2.5, 27.35

dry = np.zeros((N, 2))        # direct signal
send = np.zeros((N, 2))       # reverb send


def hz(midi):
    return 440.0 * 2 ** ((midi - 69) / 12)


def place(buf, start, sig, pan=0.0, gain=1.0):
    """Add mono or stereo `sig` into `buf` at time `start` (s)."""
    i0 = int(round(start * SR))
    if i0 >= N:
        return
    if sig.ndim == 1:
        l, r = np.cos((pan + 1) * np.pi / 4), np.sin((pan + 1) * np.pi / 4)
        sig = np.stack([sig * l, sig * r], 1)
    if i0 < 0:
        sig, i0 = sig[-i0:], 0
    n = min(len(sig), N - i0)
    buf[i0:i0 + n] += sig[:n] * gain


def out(start, sig, pan=0.0, gain=1.0, rev=0.0):
    place(dry, start, sig, pan, gain)
    if rev:
        place(send, start, sig, pan, gain * rev)


def sos(kind, freq, order=2):
    return signal.butter(order, freq, btype=kind, fs=SR, output='sos')


def filt(x, kind, freq, order=2):
    return signal.sosfilt(sos(kind, freq, order), x, axis=0)


def adsr(n, a, d, s, r, sus_len=None):
    a, d, r = int(a * SR), int(d * SR), int(r * SR)
    sus = max(0, n - a - d - r) if sus_len is None else int(sus_len * SR)
    e = np.concatenate([np.linspace(0, 1, a, endpoint=False) ** 2 if a else [],
                        1 - (1 - s) * (np.linspace(0, 1, d, endpoint=False) ** .7) if d else [],
                        np.full(sus, s), s * (1 - np.linspace(0, 1, r)) ** 2])
    return np.pad(e, (0, max(0, n - len(e))))[:n]


def saw(freq, n, phase0=0.0):
    """Band-limited sawtooth (polyBLEP)."""
    dt = freq / SR
    ph = (phase0 + dt * np.arange(n)) % 1.0
    y = 2 * ph - 1
    m = ph < dt
    x = ph[m] / dt
    y[m] -= x + x - x * x - 1
    m = ph > 1 - dt
    x = (ph[m] - 1) / dt
    y[m] -= x * x + x + x + 1
    return y


def supersaw(notes, dur, voices=5, spread=0.012, cutoff=1400):
    n = int(dur * SR)
    L = np.zeros(n)
    R = np.zeros(n)
    for note in notes:
        f = hz(note)
        for v in range(voices):
            det = 1 + spread * (v / (voices - 1) - .5)
            s = saw(f * det, n, rng.random())
            if v % 2:
                L += s
            else:
                R += s
            if v == voices // 2:
                L += s * .5
                R += s * .5
    st = np.stack([L, R], 1) / (len(notes) * voices)
    return filt(st, 'lowpass', cutoff, 2)


def pluck(note, dur=1.2, bright=1.0):
    """Clean additive pluck/bell: higher partials decay faster (natural filter envelope)."""
    n = int(dur * SR)
    t = np.arange(n) / SR
    f = hz(note)
    y = np.zeros(n)
    for k, (ratio, amp) in enumerate([(1, 1), (2, .5), (3, .28 * bright), (4, .16 * bright), (5.01, .08 * bright), (6.02, .05 * bright)]):
        if f * ratio > 16000:
            break
        y += amp * np.sin(2 * np.pi * f * ratio * t) * np.exp(-t * (2.5 + 5 * k))
    y *= np.minimum(1, t / .003)
    return y * .5


def kick(dur=.55, punch=1.0):
    n = int(dur * SR)
    t = np.arange(n) / SR
    fr = 44 + 110 * np.exp(-t * 32)
    ph = 2 * np.pi * np.cumsum(fr) / SR
    y = np.sin(ph) * np.exp(-t * 6.5)
    click = filt(rng.standard_normal(n) * np.exp(-t * 400), 'bandpass', [1500, 5000])
    return np.tanh((y + .15 * click * punch) * 1.4)


def clap(dur=.4):
    n = int(dur * SR)
    t = np.arange(n) / SR
    env = np.exp(-t * 22)
    for d in (.0, .011, .022):
        env += np.where(t > d, np.exp(-(t - d) * 90), 0) * .6
    return filt(rng.standard_normal(n) * env, 'bandpass', [900, 4200]) * .5


def hat(dur=.09):
    n = int(dur * SR)
    t = np.arange(n) / SR
    return filt(rng.standard_normal(n) * np.exp(-t * 55), 'highpass', 7500) * .35


def swept_noise(dur, f0, f1, q=1.2, shape='rise'):
    """Band-pass filtered noise with a sweeping centre frequency (processed in blocks)."""
    n = int(dur * SR)
    x = rng.standard_normal(n)
    y = np.zeros(n)
    blk = 256
    zi = None
    for b in range(0, n, blk):
        k = b / n
        if shape == 'swell':
            k = np.sin(np.pi * k)
        fc = f0 * (f1 / f0) ** k
        lo, hi = fc / (1 + 1 / q), min(fc * (1 + 1 / q), SR / 2 - 100)
        s = signal.butter(2, [lo, hi], btype='bandpass', fs=SR, output='sos')
        if zi is None:
            zi = np.zeros((s.shape[0], 2))
        y[b:b + blk], zi = signal.sosfilt(s, x[b:b + blk], zi=zi)
    return y


def braam(notes, dur=3.5):
    """Cinematic brass-like hit: saw stack with an opening/closing filter (crossfade)."""
    n = int(dur * SR)
    t = np.arange(n) / SR
    raw = np.zeros(n)
    for note in notes:
        for det in (0.996, 1.0, 1.004):
            raw += saw(hz(note) * det, n, rng.random())
    raw /= len(notes) * 3
    dark = filt(raw, 'lowpass', 260, 4)
    bright = filt(raw, 'lowpass', 1100, 2)
    open_k = np.exp(-t * 2.2)
    y = dark * (1 - open_k) + bright * open_k
    return np.tanh(y * 1.3) * adsr(n, .03, .5, .5, 2.2)


def sub_drop(dur=2.5):
    n = int(dur * SR)
    t = np.arange(n) / SR
    fr = 30 + 55 * np.exp(-t * 3)
    return np.sin(2 * np.pi * np.cumsum(fr) / SR) * np.exp(-t * 1.4) * np.minimum(1, t / .004)


# ============================ ARRANGEMENT ============================
# Harmony (A minor): sections line up with scene cuts.
CHORDS = [
    (0.0, 4.5, [33], [57, 60, 64, 71]),          # intro drone (A)
    (4.5, 8.0, [33], [57, 60, 64, 71]),          # Am(add9)
    (8.0, 12.5, [29], [53, 57, 60, 64]),         # Fmaj7
    (12.5, 17.0, [36], [55, 60, 64, 62]),        # C(add9)
    (17.0, 21.5, [31], [55, 59, 62, 64]),        # G6
    (21.5, 23.75, [29], [53, 57, 60, 65]),       # F
    (23.75, 26.0, [31], [55, 59, 62, 67]),       # G
    (26.0, 30.0, [33], [57, 60, 64, 69, 71]),    # Am(add9) resolve
]


def chord_at(t):
    for c in CHORDS:
        if c[0] <= t < c[1]:
            return c
    return CHORDS[-1]


# --- intro: dark swell into the crown hit ---
sw = swept_noise(2.6, 200, 2400, q=1.5)
sw *= np.linspace(0, 1, len(sw)) ** 2.5
out(HIT1 - 2.6, sw, gain=.05, rev=.9)
drone = supersaw([33, 45, 52], 4.6, cutoff=500) * adsr(int(4.6 * SR), 1.2, .1, 1, 1.2)[:, None]
out(0, drone, gain=.35, rev=.4)

# --- cinematic hits ---
for at, g in [(HIT1, 1.0), (HIT2, 1.1)]:
    out(at, sub_drop(), gain=.55 * g)
    out(at, braam([33, 40, 45, 52], 3.8), gain=.22 * g, rev=.5)
    out(at, kick(.8, .4), gain=.45 * g)
    out(at, filt(rng.standard_normal(int(1.6 * SR)) * np.exp(-np.arange(int(1.6 * SR)) / SR * 5), 'lowpass', 1800),
        gain=.03 * g, rev=1.0)
# smaller accents on scene cuts
for at in (8.0, 12.5, 17.0, 21.5):
    out(at, sub_drop(1.2), gain=.22)

# --- warm "glow" swells on logo shine moments (soft sine chord, no metallic partials) ---
def glow(notes, dur=2.4):
    n = int(dur * SR)
    t = np.arange(n) / SR
    y = sum(np.sin(2 * np.pi * hz(nt) * t * (1 + .0015 * np.sin(2 * np.pi * 4.5 * t))) for nt in notes) / len(notes)
    return filt(y * adsr(n, .35, .4, .5, 1.4), 'lowpass', 2500)


for at, notes in [(2.75, [69, 72, 76]), (6.25, [64, 69, 72]), (27.4, [69, 72, 76, 81])]:
    out(at, glow(notes), gain=.07, rev=.9)

# --- pads (side-chained during the groove) ---
pad = np.zeros((N, 2))
for st, en, bass, notes in CHORDS[1:]:
    d = en - st + 1.2
    s = supersaw(notes, d, cutoff=1600 if st >= GROOVE_START else 1100)
    s *= adsr(len(s), .6 if st > 4.6 else 1.2, .2, .9, 1.2)[:, None]
    place(pad, st, s)
duck = np.ones(N)
b = GROOVE_START
while b < GROOVE_END - 1e-6:
    i0 = int(b * SR)
    n = int(BEAT * SR)
    t = np.arange(n) / SR
    duck[i0:i0 + n] = 1 - .6 * np.exp(-t * 9)
    b += BEAT
pad *= duck[:, None]
dry += pad * .28
send += pad * .28 * .5

# --- bell-pluck arpeggio before the groove (4.5 – 8) ---
arp_intro = [57, 60, 64, 67, 69, 67, 64, 60]
for i in range(12):
    at = 4.5 + i * BEAT / 2 + (.0 if i < 6 else .0)
    out(at, pluck(arp_intro[i % 8], 1.4, .4), pan=((i % 4) / 3 - .5) * .6, gain=.10 + .01 * i, rev=.7)

# --- groove (8 – 26) ---
b, beat_i = GROOVE_START, 0
while b < GROOVE_END - 1e-6:
    st, en, bass, notes = chord_at(b + 1e-3)
    bar_pos = beat_i % 4
    section2 = b >= 12.5
    # kick: four-on-the-floor from 12.5, halftime before
    if section2 or bar_pos in (0, 2):
        out(b, kick(), gain=.42)
    # soft clap on 2 & 4 from 12.5
    if section2 and bar_pos in (1, 3):
        out(b, clap(), gain=.12, rev=.6)
    # offbeat hats
    if section2:
        out(b + BEAT / 2, hat(), pan=.25, gain=.06)
        if b >= 17.0:
            out(b + BEAT * .75, hat(.05), pan=-.2, gain=.04)
    # sub bass: root on the offbeat 8th (classic pumping pattern)
    for off in (0.5,):
        bn = int(BEAT / 2 * SR)
        t = np.arange(bn) / SR
        f = hz(bass[0] + 12)
        sb = np.sin(2 * np.pi * f * t) + .25 * np.sin(4 * np.pi * f * t)
        sb *= np.minimum(1, t / .01) * np.minimum(1, (BEAT / 2 - t) / .03)
        out(b + BEAT * off, sb, gain=.20)
    # arp: 8th notes on chord tones, 2 octaves
    tones = sorted(notes)
    seq = [tones[0], tones[1], tones[2], tones[3], tones[2] + 12, tones[3], tones[1], tones[2]]
    for h in range(2):
        idx = (beat_i * 2 + h) % len(seq)
        vel = .085 if h == 0 else .06
        out(b + h * BEAT / 2, pluck(seq[idx], .9, .5), pan=(.35 if idx % 2 else -.35), gain=vel, rev=.55)
    b += BEAT
    beat_i += 1

# ============================ MOTION SFX ============================
def whoosh(dur=1.0, f0=180, f1=1400, peak=.55):
    """Airy pass-by: swept band-pass noise + a low 'air' body, asymmetric envelope, L->R pan."""
    n = int(dur * SR)
    k = np.linspace(0, 1, n)
    env = np.where(k < peak, (k / peak) ** 2.2, ((1 - k) / (1 - peak)) ** 1.6)
    hi = swept_noise(dur, f0, f1, q=1.6, shape='swell')
    lo = filt(rng.standard_normal(n), 'lowpass', 260)
    y = (hi * .8 + lo * .6) * env
    y = filt(y, 'lowpass', 3000)
    pan = np.linspace(-.7, .7, n)
    return np.stack([y * np.cos((pan + 1) * np.pi / 4), y * np.sin((pan + 1) * np.pi / 4)], 1)


def soft_pop(f=620, dur=.12):
    """Rounded UI 'pop' — pitch-dropping sine with a tiny muted transient."""
    n = int(dur * SR)
    t = np.arange(n) / SR
    fr = f * (1 + .6 * np.exp(-t * 60))
    y = np.sin(2 * np.pi * np.cumsum(fr) / SR) * np.exp(-t * 38) * np.minimum(1, t / .002)
    y += filt(rng.standard_normal(n) * np.exp(-t * 300), 'lowpass', 1500) * .15
    return y


def bloop(f0=280, f1=620, dur=.18):
    """Clay 'bloop' — upward pitch glide, soft and round."""
    n = int(dur * SR)
    t = np.arange(n) / SR
    fr = f0 + (f1 - f0) * (1 - np.exp(-t * 30))
    y = np.sin(2 * np.pi * np.cumsum(fr) / SR) * np.exp(-t * 22) * np.minimum(1, t / .004)
    return y + .3 * np.sin(2 * np.pi * np.cumsum(fr * 2) / SR) * np.exp(-t * 40)


def thump(dur=.6):
    """Soft low impact for panels landing."""
    n = int(dur * SR)
    t = np.arange(n) / SR
    fr = 45 + 70 * np.exp(-t * 25)
    return np.sin(2 * np.pi * np.cumsum(fr) / SR) * np.exp(-t * 7) * np.minimum(1, t / .003)


SFX = .9  # overall SFX level

# intro: energy gathering into the logo, then the crown falling
out(0.1, whoosh(2.3, 120, 900, peak=.85), gain=.10 * SFX, rev=.6)
out(1.95, whoosh(.6, 900, 250, peak=.8), gain=.10 * SFX, rev=.3)          # crown drop (pitch-down)
# wordmark
out(4.4, whoosh(1.1, 150, 1100), gain=.10 * SFX, rev=.4)                   # logo rises
for i in range(9):                                                         # letters flip in
    out(4.97 + i * .055, soft_pop(520 + i * 25, .09), pan=(i / 8 - .5) * .8, gain=.05 * SFX, rev=.3)
out(5.5, whoosh(1.0, 300, 1600, peak=.5), gain=.05 * SFX, rev=.5)          # gold line draws
# scene transitions (scene exit + gold light streak)
for at in (7.5, 12.2, 16.75, 21.25, 25.7):
    out(at - .1, whoosh(1.1, 160, 1500), gain=.13 * SFX, rev=.45)
# performance panel
out(8.05, thump(), gain=.18 * SFX)
for i in range(3):
    out(8.62 + i * .1, soft_pop(560 + i * 60), pan=-.3 + i * .15, gain=.07 * SFX, rev=.3)
for i in range(3):
    out(9.42 + i * .12, soft_pop(440 + i * 50, .1), pan=-.4, gain=.05 * SFX, rev=.3)
out(9.0, whoosh(1.8, 200, 900, peak=.7), gain=.04 * SFX, rev=.6)          # chart drawing
# feature cards + clay icons
for i in range(6):
    out(12.87 + i * .09, whoosh(.45, 300, 1200, peak=.4), gain=.035 * SFX, rev=.3)
    out(13.32 + i * .09, bloop(260 + i * 20, 600 + i * 30), pan=(i % 3 - 1) * .5, gain=.07 * SFX, rev=.35)
# globe + stats
out(17.1, whoosh(1.6, 100, 700, peak=.4), gain=.10 * SFX, rev=.6)
out(17.15, thump(.8), gain=.14 * SFX)
for i in range(3):
    out(18.02 + i * .12, soft_pop(600 - i * 50), pan=.2 + i * .15, gain=.07 * SFX, rev=.3)
# pricing cards + badge
out(21.87, thump(), gain=.15 * SFX)
out(22.1, whoosh(.7, 250, 1200, peak=.4), gain=.06 * SFX, rev=.4)
out(22.22, whoosh(.7, 250, 1200, peak=.4)[:, ::-1], gain=.06 * SFX, rev=.4)
out(23.2, bloop(320, 760, .22), gain=.09 * SFX, rev=.4)
# outro: logo returns, CTA pops, ripples
out(25.95, whoosh(1.3, 120, 1000, peak=.65), gain=.10 * SFX, rev=.5)
out(27.82, bloop(240, 540, .25), gain=.11 * SFX, rev=.5)
for at in (28.5, 29.1):
    out(at, filt(whoosh(.9, 600, 250, peak=.15)[:, 0], 'lowpass', 1200), gain=.04 * SFX, rev=.8)

# --- riser into the finale (26 -> 27.35) ---
r_len = HIT2 - 25.9
rs = filt(swept_noise(r_len, 250, 2500, q=2.0), 'lowpass', 3000)
rs *= np.linspace(0, 1, len(rs)) ** 2
out(25.9, rs, gain=.05, rev=.6)
tn = np.arange(int(r_len * SR)) / SR
rise_tone = np.sin(2 * np.pi * np.cumsum(220 * 2 ** (tn / r_len * 1.0)) / SR) * (tn / r_len) ** 2
out(25.9, rise_tone, gain=.03, rev=.8)

# --- final sustained chord after the hit ---
final = supersaw([45, 57, 64, 69, 71, 76], 2.6, cutoff=2200) * adsr(int(2.6 * SR), .05, .5, .6, 2.0)[:, None]
out(HIT2, final, gain=.22, rev=.8)

# ============================ REVERB ============================
ir_len = int(3.2 * SR)
ti = np.arange(ir_len) / SR
ir = rng.standard_normal((ir_len, 2)) * np.exp(-ti / .75)[:, None]
ir = filt(ir, 'lowpass', 6500)
ir[:int(.02 * SR)] = 0                               # pre-delay
ir /= np.sqrt(np.sum(ir ** 2, axis=0))
send = filt(send, 'highpass', 180)
wet = np.stack([signal.fftconvolve(send[:, c], ir[:, c])[:N] for c in range(2)], 1)
mix = dry + wet * .55

# ============================ MASTER ============================
mix = filt(mix, 'highpass', 28)
mix = filt(mix, 'lowpass', 11000, 1)                # soften the top end
fade_in = np.minimum(1, T / .05)
fade_out = np.clip((DUR - T) / 1.2, 0, 1) ** 1.5
mix *= (fade_in * fade_out)[:, None]
mix /= np.max(np.abs(mix)) + 1e-9
mix = np.tanh(mix * 1.25) / np.tanh(1.25)            # gentle saturation / glue
mix *= 10 ** (-1.0 / 20) / (np.max(np.abs(mix)) + 1e-9)
mix *= MASTER_GAIN                                   # comfortable level (~-18 LUFS)
pcm = (mix * 32767).astype(np.int16)

with wave.open(sys.argv[1] if len(sys.argv) > 1 else 'soundtrack.wav', 'wb') as w:
    w.setnchannels(2)
    w.setsampwidth(2)
    w.setframerate(SR)
    w.writeframes(pcm.tobytes())
print('ok')
