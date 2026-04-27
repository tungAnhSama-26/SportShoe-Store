const QR_VERSION = 4;
const QR_SIZE = QR_VERSION * 4 + 17;
const QR_DATA_CODEWORDS = 80;
const QR_ERROR_CORRECTION_CODEWORDS = 20;
const QR_MAX_INPUT_BYTES = 78;

const FORMAT_INFO_LEVEL_L = [
  0b111011111000100,
  0b111001011110011,
  0b111110110101010,
  0b111100010011101,
  0b110011000101111,
  0b110001100011000,
  0b110110001000001,
  0b110100101110110,
];

const GF_EXP = new Uint8Array(512);
const GF_LOG = new Uint8Array(256);

let gfValue = 1;
for (let index = 0; index < 255; index += 1) {
  GF_EXP[index] = gfValue;
  GF_LOG[gfValue] = index;
  gfValue <<= 1;
  if (gfValue & 0x100) {
    gfValue ^= 0x11d;
  }
}

for (let index = 255; index < GF_EXP.length; index += 1) {
  GF_EXP[index] = GF_EXP[index - 255];
}

function gfMultiply(first, second) {
  if (first === 0 || second === 0) {
    return 0;
  }

  return GF_EXP[GF_LOG[first] + GF_LOG[second]];
}

function createGeneratorPolynomial(degree) {
  let polynomial = [1];

  for (let rootIndex = 0; rootIndex < degree; rootIndex += 1) {
    const nextPolynomial = new Array(polynomial.length + 1).fill(0);

    for (let coefficientIndex = 0; coefficientIndex < polynomial.length; coefficientIndex += 1) {
      nextPolynomial[coefficientIndex] ^= polynomial[coefficientIndex];
      nextPolynomial[coefficientIndex + 1] ^= gfMultiply(
        polynomial[coefficientIndex],
        GF_EXP[rootIndex],
      );
    }

    polynomial = nextPolynomial;
  }

  return polynomial;
}

const RS_GENERATOR = createGeneratorPolynomial(QR_ERROR_CORRECTION_CODEWORDS);

function appendBits(bitBuffer, value, length) {
  for (let shift = length - 1; shift >= 0; shift -= 1) {
    bitBuffer.push((value >>> shift) & 1);
  }
}

function encodeDataCodewords(value) {
  const encoder = new TextEncoder();
  const dataBytes = Array.from(encoder.encode(value));

  if (dataBytes.length > QR_MAX_INPUT_BYTES) {
    throw new Error("Mã quá dài để tạo QR trên màn này");
  }

  const bitBuffer = [];
  appendBits(bitBuffer, 0b0100, 4);
  appendBits(bitBuffer, dataBytes.length, 8);
  dataBytes.forEach((byte) => appendBits(bitBuffer, byte, 8));

  const maxBits = QR_DATA_CODEWORDS * 8;
  const terminatorLength = Math.min(4, maxBits - bitBuffer.length);
  appendBits(bitBuffer, 0, terminatorLength);

  while (bitBuffer.length % 8 !== 0) {
    bitBuffer.push(0);
  }

  const codewords = [];
  for (let index = 0; index < bitBuffer.length; index += 8) {
    let codeword = 0;
    for (let offset = 0; offset < 8; offset += 1) {
      codeword = (codeword << 1) | bitBuffer[index + offset];
    }
    codewords.push(codeword);
  }

  let padByte = 0xec;
  while (codewords.length < QR_DATA_CODEWORDS) {
    codewords.push(padByte);
    padByte = padByte === 0xec ? 0x11 : 0xec;
  }

  return codewords;
}

function computeErrorCorrection(dataCodewords) {
  const result = new Array(QR_ERROR_CORRECTION_CODEWORDS).fill(0);

  dataCodewords.forEach((dataCodeword) => {
    const factor = dataCodeword ^ result[0];
    result.shift();
    result.push(0);

    for (let index = 0; index < QR_ERROR_CORRECTION_CODEWORDS; index += 1) {
      result[index] ^= gfMultiply(RS_GENERATOR[index + 1], factor);
    }
  });

  return result;
}

function createEmptyMatrix() {
  return Array.from({ length: QR_SIZE }, () => Array(QR_SIZE).fill(null));
}

function createFunctionMap() {
  return Array.from({ length: QR_SIZE }, () => Array(QR_SIZE).fill(false));
}

function setFunctionModule(modules, functionModules, row, column, isDark) {
  if (
    row < 0 ||
    row >= QR_SIZE ||
    column < 0 ||
    column >= QR_SIZE
  ) {
    return;
  }

  modules[row][column] = Boolean(isDark);
  functionModules[row][column] = true;
}

function drawFinderPattern(modules, functionModules, top, left) {
  for (let rowOffset = -1; rowOffset <= 7; rowOffset += 1) {
    for (let columnOffset = -1; columnOffset <= 7; columnOffset += 1) {
      const row = top + rowOffset;
      const column = left + columnOffset;

      const isSeparator =
        rowOffset === -1 ||
        rowOffset === 7 ||
        columnOffset === -1 ||
        columnOffset === 7;

      const isDarkModule =
        rowOffset >= 0 &&
        rowOffset <= 6 &&
        columnOffset >= 0 &&
        columnOffset <= 6 &&
        (
          rowOffset === 0 ||
          rowOffset === 6 ||
          columnOffset === 0 ||
          columnOffset === 6 ||
          (
            rowOffset >= 2 &&
            rowOffset <= 4 &&
            columnOffset >= 2 &&
            columnOffset <= 4
          )
        );

      setFunctionModule(
        modules,
        functionModules,
        row,
        column,
        !isSeparator && isDarkModule,
      );
    }
  }
}

function drawAlignmentPattern(modules, functionModules, centerRow, centerColumn) {
  if (modules[centerRow][centerColumn] != null) {
    return;
  }

  for (let rowOffset = -2; rowOffset <= 2; rowOffset += 1) {
    for (let columnOffset = -2; columnOffset <= 2; columnOffset += 1) {
      const ring = Math.max(Math.abs(rowOffset), Math.abs(columnOffset));
      setFunctionModule(
        modules,
        functionModules,
        centerRow + rowOffset,
        centerColumn + columnOffset,
        ring !== 1,
      );
    }
  }
}

function reserveFormatAreas(modules, functionModules) {
  for (let index = 0; index < 9; index += 1) {
    if (index !== 6) {
      setFunctionModule(modules, functionModules, 8, index, false);
      setFunctionModule(modules, functionModules, index, 8, false);
    }
  }

  for (let index = 0; index < 8; index += 1) {
    setFunctionModule(modules, functionModules, 8, QR_SIZE - 1 - index, false);
    setFunctionModule(modules, functionModules, QR_SIZE - 1 - index, 8, false);
  }
}

function drawBasePatterns(modules, functionModules) {
  drawFinderPattern(modules, functionModules, 0, 0);
  drawFinderPattern(modules, functionModules, 0, QR_SIZE - 7);
  drawFinderPattern(modules, functionModules, QR_SIZE - 7, 0);

  for (let index = 8; index < QR_SIZE - 8; index += 1) {
    const isDark = index % 2 === 0;
    setFunctionModule(modules, functionModules, 6, index, isDark);
    setFunctionModule(modules, functionModules, index, 6, isDark);
  }

  const alignmentCenters = [6, 26];
  alignmentCenters.forEach((rowCenter) => {
    alignmentCenters.forEach((columnCenter) => {
      drawAlignmentPattern(modules, functionModules, rowCenter, columnCenter);
    });
  });

  reserveFormatAreas(modules, functionModules);
  setFunctionModule(modules, functionModules, QR_SIZE - 8, 8, true);
}

function toCodewordBits(codewords) {
  const bits = [];

  codewords.forEach((codeword) => {
    for (let shift = 7; shift >= 0; shift -= 1) {
      bits.push((codeword >>> shift) & 1);
    }
  });

  return bits;
}

function placeDataBits(modules, functionModules, dataBits) {
  let bitIndex = 0;
  let movingUp = true;

  for (let rightColumn = QR_SIZE - 1; rightColumn >= 1; rightColumn -= 2) {
    if (rightColumn === 6) {
      rightColumn -= 1;
    }

    for (let step = 0; step < QR_SIZE; step += 1) {
      const row = movingUp ? QR_SIZE - 1 - step : step;

      for (let column = rightColumn; column >= rightColumn - 1; column -= 1) {
        if (functionModules[row][column]) {
          continue;
        }

        modules[row][column] = dataBits[bitIndex] === 1;
        bitIndex += 1;
      }
    }

    movingUp = !movingUp;
  }
}

function maskCondition(mask, row, column) {
  switch (mask) {
    case 0:
      return (row + column) % 2 === 0;
    case 1:
      return row % 2 === 0;
    case 2:
      return column % 3 === 0;
    case 3:
      return (row + column) % 3 === 0;
    case 4:
      return (Math.floor(row / 2) + Math.floor(column / 3)) % 2 === 0;
    case 5:
      return ((row * column) % 2) + ((row * column) % 3) === 0;
    case 6:
      return ((((row * column) % 2) + ((row * column) % 3)) % 2) === 0;
    case 7:
      return ((((row + column) % 2) + ((row * column) % 3)) % 2) === 0;
    default:
      return false;
  }
}

function applyMask(modules, functionModules, mask) {
  for (let row = 0; row < QR_SIZE; row += 1) {
    for (let column = 0; column < QR_SIZE; column += 1) {
      if (functionModules[row][column]) {
        continue;
      }

      if (maskCondition(mask, row, column)) {
        modules[row][column] = !modules[row][column];
      }
    }
  }
}

function getBit(value, bitIndex) {
  return ((value >>> bitIndex) & 1) === 1;
}

function drawFormatInformation(modules, functionModules, mask) {
  const formatBits = FORMAT_INFO_LEVEL_L[mask];

  for (let index = 0; index <= 5; index += 1) {
    setFunctionModule(modules, functionModules, 8, index, getBit(formatBits, index));
  }

  setFunctionModule(modules, functionModules, 8, 7, getBit(formatBits, 6));
  setFunctionModule(modules, functionModules, 8, 8, getBit(formatBits, 7));
  setFunctionModule(modules, functionModules, 7, 8, getBit(formatBits, 8));

  for (let index = 9; index < 15; index += 1) {
    setFunctionModule(
      modules,
      functionModules,
      14 - index,
      8,
      getBit(formatBits, index),
    );
  }

  for (let index = 0; index < 8; index += 1) {
    setFunctionModule(
      modules,
      functionModules,
      8,
      QR_SIZE - 1 - index,
      getBit(formatBits, index),
    );
  }

  for (let index = 8; index < 15; index += 1) {
    setFunctionModule(
      modules,
      functionModules,
      QR_SIZE - 15 + index,
      8,
      getBit(formatBits, index),
    );
  }

  setFunctionModule(modules, functionModules, QR_SIZE - 8, 8, true);
}

function penaltyForRuns(line) {
  let penalty = 0;
  let currentColor = line[0];
  let runLength = 1;

  for (let index = 1; index < line.length; index += 1) {
    if (line[index] === currentColor) {
      runLength += 1;
      continue;
    }

    if (runLength >= 5) {
      penalty += runLength - 2;
    }

    currentColor = line[index];
    runLength = 1;
  }

  if (runLength >= 5) {
    penalty += runLength - 2;
  }

  return penalty;
}

const FINDER_PATTERN_A = [
  true,
  false,
  true,
  true,
  true,
  false,
  true,
  false,
  false,
  false,
  false,
];

const FINDER_PATTERN_B = [
  false,
  false,
  false,
  false,
  true,
  false,
  true,
  true,
  true,
  false,
  true,
];

function matchesPattern(line, startIndex, pattern) {
  for (let index = 0; index < pattern.length; index += 1) {
    if (line[startIndex + index] !== pattern[index]) {
      return false;
    }
  }

  return true;
}

function penaltyForFinderLikePatterns(line) {
  let penalty = 0;

  for (let index = 0; index <= line.length - 11; index += 1) {
    if (
      matchesPattern(line, index, FINDER_PATTERN_A) ||
      matchesPattern(line, index, FINDER_PATTERN_B)
    ) {
      penalty += 40;
    }
  }

  return penalty;
}

function calculatePenalty(modules) {
  let penalty = 0;
  let darkModules = 0;

  for (let row = 0; row < QR_SIZE; row += 1) {
    penalty += penaltyForRuns(modules[row]);
    penalty += penaltyForFinderLikePatterns(modules[row]);

    for (let column = 0; column < QR_SIZE; column += 1) {
      if (modules[row][column]) {
        darkModules += 1;
      }

      if (
        row < QR_SIZE - 1 &&
        column < QR_SIZE - 1 &&
        modules[row][column] === modules[row + 1][column] &&
        modules[row][column] === modules[row][column + 1] &&
        modules[row][column] === modules[row + 1][column + 1]
      ) {
        penalty += 3;
      }
    }
  }

  for (let column = 0; column < QR_SIZE; column += 1) {
    const columnValues = [];
    for (let row = 0; row < QR_SIZE; row += 1) {
      columnValues.push(modules[row][column]);
    }
    penalty += penaltyForRuns(columnValues);
    penalty += penaltyForFinderLikePatterns(columnValues);
  }

  const totalModules = QR_SIZE * QR_SIZE;
  const darkRatio = (darkModules * 100) / totalModules;
  penalty += Math.floor(Math.abs(darkRatio - 50) / 5) * 10;

  return penalty;
}

export function createQrCodeMatrix(value) {
  const normalizedValue = String(value ?? "").trim();

  if (!normalizedValue) {
    throw new Error("Không có dữ liệu để tạo QR");
  }

  const dataCodewords = encodeDataCodewords(normalizedValue);
  const errorCorrection = computeErrorCorrection(dataCodewords);
  const allCodewords = [...dataCodewords, ...errorCorrection];
  const dataBits = toCodewordBits(allCodewords);

  let bestModules = null;
  let bestPenalty = Number.POSITIVE_INFINITY;

  for (let mask = 0; mask < 8; mask += 1) {
    const modules = createEmptyMatrix();
    const functionModules = createFunctionMap();

    drawBasePatterns(modules, functionModules);
    placeDataBits(modules, functionModules, dataBits);
    applyMask(modules, functionModules, mask);
    drawFormatInformation(modules, functionModules, mask);

    const penalty = calculatePenalty(modules);
    if (penalty < bestPenalty) {
      bestPenalty = penalty;
      bestModules = modules.map((row) => [...row]);
    }
  }

  return {
    size: QR_SIZE,
    modules: bestModules,
  };
}

function escapeXml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&apos;");
}

function buildSvgPath(modules, border) {
  let path = "";

  for (let row = 0; row < modules.length; row += 1) {
    for (let column = 0; column < modules[row].length; column += 1) {
      if (!modules[row][column]) {
        continue;
      }

      const x = column + border;
      const y = row + border;
      path += `M${x} ${y}h1v1H${x}z`;
    }
  }

  return path;
}

export function createQrCodeSvg(value, options = {}) {
  const {
    border = 4,
    darkColor = "#0f172a",
    lightColor = "#ffffff",
    title = "QR code",
    description = "",
  } = options;

  const normalizedBorder = Math.max(0, Number(border) || 0);
  const { modules, size } = createQrCodeMatrix(value);
  const viewportSize = size + normalizedBorder * 2;
  const path = buildSvgPath(modules, normalizedBorder);

  return [
    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${viewportSize} ${viewportSize}" role="img" aria-label="${escapeXml(title)}" shape-rendering="crispEdges">`,
    `<title>${escapeXml(title)}</title>`,
    description ? `<desc>${escapeXml(description)}</desc>` : "",
    `<rect width="${viewportSize}" height="${viewportSize}" fill="${escapeXml(lightColor)}"/>`,
    `<path d="${path}" fill="${escapeXml(darkColor)}"/>`,
    "</svg>",
  ].join("");
}
