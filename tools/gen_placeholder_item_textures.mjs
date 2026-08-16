// Generates placeholder 16x16 item textures for the Phase 2 biome fish items.
//
// These are deliberately plain - a flat tinted circle per item, distinguishable by colour only -
// so the items, loot tables and code can land now. Real art replaces these later (see todo.md
// Phase 2). Re-run any time to regenerate; it paints from scratch, so it's idempotent.
//
// Run: node tools/gen_placeholder_item_textures.mjs

import fs from 'node:fs';
import zlib from 'node:zlib';

const DIR = 'src/main/resources/assets/ddv_fishing/textures/item';
const SIZE = 16;

// [fill, outline]
const ITEMS = {
    ocean_pearl: [[223, 243, 255], [150, 200, 230]],
    large_fish: [[76, 134, 201], [40, 80, 140]],
    algae: [[76, 154, 70], [40, 100, 40]],
    catfish: [[122, 106, 93], [70, 58, 48]],
    exotic_fish: [[224, 85, 155], [160, 40, 100]],
    river_piranha: [[201, 76, 76], [140, 30, 30]],
};

function encodePng(width, height, px) {
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
    ihdr.writeUInt32BE(width, 0);
    ihdr.writeUInt32BE(height, 4);
    ihdr[8] = 8;
    ihdr[9] = 6;

    const stride = width * 4;
    const rows = Buffer.alloc(height * (stride + 1));
    for (let y = 0; y < height; y++) {
        rows[y * (stride + 1)] = 0;
        px.copy(rows, y * (stride + 1) + 1, y * stride, (y + 1) * stride);
    }

    return Buffer.concat([
        Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
        chunk('IHDR', ihdr),
        chunk('IDAT', zlib.deflateSync(rows, {level: 9})),
        chunk('IEND', Buffer.alloc(0)),
    ]);
}

fs.mkdirSync(DIR, {recursive: true});

for (const [name, [fill, outline]] of Object.entries(ITEMS)) {
    const px = Buffer.alloc(SIZE * SIZE * 4, 0);
    const cx = SIZE / 2;
    const cy = SIZE / 2;
    const rOuter = 6.2;
    const rInner = 5.2;

    for (let y = 0; y < SIZE; y++) {
        for (let x = 0; x < SIZE; x++) {
            const dx = x + 0.5 - cx;
            const dy = y + 0.5 - cy;
            const d = Math.sqrt(dx * dx + dy * dy);
            let colour = null;
            if (d <= rInner) colour = fill;
            else if (d <= rOuter) colour = outline;
            if (colour) {
                const i = (y * SIZE + x) * 4;
                px[i] = colour[0];
                px[i + 1] = colour[1];
                px[i + 2] = colour[2];
                px[i + 3] = 255;
            }
        }
    }

    const out = `${DIR}/${name}.png`;
    fs.writeFileSync(out, encodePng(SIZE, SIZE, px));
    console.log(`wrote ${out}`);
}
