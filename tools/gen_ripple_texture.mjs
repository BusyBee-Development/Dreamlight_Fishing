// Generates assets/ddv_fishing/textures/entity/water_ripple.png from the ripple geometry.
//
// The ripple model is a stack of flat square plates, one per ring. A plate only reads as a ring
// if its texture erases everything outside the ring stroke, so the artwork has to be an annulus
// with a real alpha channel. The hand-authored texture was a solid filled square per plate, which
// renders as the opaque slab this replaces.
//
// Alpha is strictly binary - 0 or 255, never in between. GeckoLib renders entities with a cutout
// render type by default, which thresholds alpha rather than blending it, so partial values come
// out as hard noise.
//
// Run:  node tools/gen_ripple_texture.mjs
// Idempotent: it paints from scratch every time, so re-running is safe.

import fs from 'node:fs';
import zlib from 'node:zlib';

const GEO = 'src/main/resources/assets/ddv_fishing/geckolib/models/water_ripple.geo.json';
const OUT = 'src/main/resources/assets/ddv_fishing/textures/entity/water_ripple.png';

const WIDTH = 512;
const HEIGHT = 64;

// Strictly greyscale, no hue at all. The renderer multiplies a per-rarity colour over this, and
// multiply keeps only what both sides have: a blue base times an orange tint came out olive. Grey
// carries brightness but no hue, so every tint lands true. Brightness rises towards the core so
// the rings still read as depth once tinted.
const RING_COLOURS = {
    outer_ring: [196, 196, 196],
    middle_ring: [220, 220, 220],
    inner_ring: [238, 238, 238],
    center_ring: [250, 250, 250],
    core_ring: [255, 255, 255],
};
const SPLASH_COLOUR = [246, 246, 246];
const SPARKLE_COLOUR = [255, 255, 255];

// Stroke width per ring, in texture pixels - and one pixel is one model unit here, since every
// plate's UV rect matches its size. Set explicitly rather than as a fraction of the radius: a
// proportional stroke made each ring's inner edge meet the next ring's outer edge, and the whole
// stack rendered as one filled disc. These leave a visible transparent gap between every ring.
const RING_STROKES = {
    outer_ring: 2.5,   // band 13.0 - 15.5
    middle_ring: 2.0,  // band  9.5 - 11.5
    inner_ring: 1.5,   // band  7.0 -  8.5
    center_ring: 1.2,  // band  3.3 -  4.5
    core_ring: 1.0,    // band  1.5 -  2.5
};

const px = Buffer.alloc(WIDTH * HEIGHT * 4, 0); // fully transparent

function set(x, y, [r, g, b]) {
    if (x < 0 || y < 0 || x >= WIDTH || y >= HEIGHT) return;
    const i = (y * WIDTH + x) * 4;
    px[i] = r; px[i + 1] = g; px[i + 2] = b; px[i + 3] = 255;
}

/** Face UVs may carry negative uv_size, so normalise to a plain rect. */
function rectOf(face) {
    const [ux, uy] = face.uv;
    const [sw, sh] = face.uv_size;
    return {
        x0: Math.round(Math.min(ux, ux + sw)),
        y0: Math.round(Math.min(uy, uy + sh)),
        w: Math.round(Math.abs(sw)),
        h: Math.round(Math.abs(sh)),
    };
}

/**
 * Draw into a rect, keeping pixels whose distance from the rect centre falls in [rInner, rOuter].
 * rInner of 0 fills a disc. Distances use pixel centres so the stroke stays symmetric.
 */
function stamp(rect, rInner, rOuter, colour) {
    const cx = rect.x0 + rect.w / 2;
    const cy = rect.y0 + rect.h / 2;
    for (let y = rect.y0; y < rect.y0 + rect.h; y++) {
        for (let x = rect.x0; x < rect.x0 + rect.w; x++) {
            const dx = x + 0.5 - cx;
            const dy = y + 0.5 - cy;
            const d = Math.sqrt(dx * dx + dy * dy);
            if (d >= rInner && d <= rOuter) set(x, y, colour);
        }
    }
}

const geo = JSON.parse(fs.readFileSync(GEO, 'utf8'));
let painted = 0;

for (const bone of geo['minecraft:geometry'][0].bones) {
    for (const cube of bone.cubes ?? []) {
        // Both faces get the same artwork so the ripple reads correctly from below the surface.
        const faces = [cube.uv?.up, cube.uv?.down].filter(f => f?.uv && f?.uv_size);
        if (faces.length === 0) continue; // hitbox cube carries no UVs and must draw nothing

        const ringColour = RING_COLOURS[bone.name];
        for (const face of faces) {
            const rect = rectOf(face);
            const rOuter = Math.min(rect.w, rect.h) / 2 - 0.5;

            if (ringColour) {
                stamp(rect, rOuter - RING_STROKES[bone.name], rOuter, ringColour);
            } else if (bone.name.startsWith('splash')) {
                stamp(rect, 0, rOuter, SPLASH_COLOUR);
            } else {
                stamp(rect, 0, rOuter, SPARKLE_COLOUR);
            }
            painted++;
        }
    }
}

// --- PNG encode ---
const CRC = (() => {
    const t = new Int32Array(256);
    for (let n = 0; n < 256; n++) {
        let c = n;
        for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
        t[n] = c;
    }
    return t;
})();

function crc32(buf) {
    let c = 0xffffffff;
    for (const byte of buf) c = CRC[(c ^ byte) & 0xff] ^ (c >>> 8);
    return (c ^ 0xffffffff) >>> 0;
}

function chunk(type, data) {
    const len = Buffer.alloc(4);
    len.writeUInt32BE(data.length);
    const body = Buffer.concat([Buffer.from(type, 'ascii'), data]);
    const crc = Buffer.alloc(4);
    crc.writeUInt32BE(crc32(body));
    return Buffer.concat([len, body, crc]);
}

const ihdr = Buffer.alloc(13);
ihdr.writeUInt32BE(WIDTH, 0);
ihdr.writeUInt32BE(HEIGHT, 4);
ihdr[8] = 8;  // bit depth
ihdr[9] = 6;  // colour type: RGBA
// 10..12 stay 0: deflate, adaptive filtering, no interlace

const stride = WIDTH * 4;
const rows = Buffer.alloc(HEIGHT * (stride + 1));
for (let y = 0; y < HEIGHT; y++) {
    rows[y * (stride + 1)] = 0; // filter type: none
    px.copy(rows, y * (stride + 1) + 1, y * stride, (y + 1) * stride);
}

fs.writeFileSync(OUT, Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
    chunk('IHDR', ihdr),
    chunk('IDAT', zlib.deflateSync(rows, { level: 9 })),
    chunk('IEND', Buffer.alloc(0)),
]));

let opaque = 0;
for (let i = 3; i < px.length; i += 4) if (px[i] === 255) opaque++;
console.log(`wrote ${OUT}`);
console.log(`  ${WIDTH}x${HEIGHT} RGBA, ${painted} faces painted`);
console.log(`  opaque ${(opaque / (WIDTH * HEIGHT) * 100).toFixed(1)}%, transparent ${(100 - opaque / (WIDTH * HEIGHT) * 100).toFixed(1)}%, partial 0.0%`);
