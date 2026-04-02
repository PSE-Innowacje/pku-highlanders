import type { DeclarationField } from '../api/declarations';

export interface FieldFormula {
  addPositions: string[];
  subtractPositions: string[];
}

/**
 * Parse formula from field name. Patterns:
 * - "(suma 1.1–1.3)" → range sum of positions 1.1, 1.2, 1.3
 * - "(suma 1.1 + 1.2)" → add positions 1.1 and 1.2
 * - "(1.1 − 1.2)" → subtract: 1.1 minus 1.2
 * - "(poz. 2 + poz. 3 − poz. 4)" → mixed add/subtract
 */
export function parseFormula(fieldName: string): FieldFormula | null {
  // Pattern: "suma X.X–X.X" (range with en-dash or hyphen)
  const rangeMatch = fieldName.match(/suma\s+([\d.]+)[–\-]([\d.]+)/);
  if (rangeMatch) {
    const positions = expandRange(rangeMatch[1], rangeMatch[2]);
    return { addPositions: positions, subtractPositions: [] };
  }

  // Pattern: "poz. X + poz. Y − poz. Z" (mixed with poz. notation)
  const pozPattern = /poz\.\s*([\d.]+)/g;
  const pozMatches = [...fieldName.matchAll(pozPattern)];
  if (pozMatches.length >= 2) {
    // Extract the formula part from parentheses
    const formulaMatch = fieldName.match(/\(([^)]+)\)/);
    if (formulaMatch) {
      return parseOperations(formulaMatch[1], 'poz.');
    }
  }

  // Pattern: "suma X.X + X.X" (simple addition with suma prefix)
  const addMatch = fieldName.match(/suma\s+([\d.]+(?:\s*\+\s*[\d.]+)+)/);
  if (addMatch) {
    const positions = addMatch[1].split(/\s*\+\s*/).map(s => s.trim());
    return { addPositions: positions, subtractPositions: [] };
  }

  // Pattern: "(X.X − X.X)" (simple subtraction without suma)
  const subMatch = fieldName.match(/\(([\d.]+)\s*[−\-]\s*([\d.]+)\)/);
  if (subMatch) {
    return { addPositions: [subMatch[1]], subtractPositions: [subMatch[2]] };
  }

  return null;
}

function expandRange(start: string, end: string): string[] {
  const prefix = start.substring(0, start.lastIndexOf('.') + 1);
  const startNum = parseInt(start.substring(start.lastIndexOf('.') + 1));
  const endNum = parseInt(end.substring(end.lastIndexOf('.') + 1));
  const positions: string[] = [];
  for (let i = startNum; i <= endNum; i++) {
    positions.push(prefix + i);
  }
  return positions;
}

function parseOperations(formula: string, _prefix: string): FieldFormula {
  const addPositions: string[] = [];
  const subtractPositions: string[] = [];

  // Split by + and − while keeping the operator
  const parts = formula.split(/\s*([+−\-])\s*/);
  let currentOp = '+';
  for (const part of parts) {
    if (part === '+') { currentOp = '+'; continue; }
    if (part === '−' || part === '-') { currentOp = '-'; continue; }
    const numMatch = part.match(/(?:poz\.\s*)?([\d.]+)/);
    if (numMatch) {
      if (currentOp === '+') {
        addPositions.push(numMatch[1]);
      } else {
        subtractPositions.push(numMatch[1]);
      }
    }
  }
  return { addPositions, subtractPositions };
}

/**
 * Build a map of fieldCode → FieldFormula for all computed fields
 */
export function buildFormulaMap(fields: DeclarationField[]): Map<string, FieldFormula> {
  const positionToCode = new Map<string, string>();
  fields.forEach(f => positionToCode.set(f.position, f.fieldCode));

  const formulaMap = new Map<string, FieldFormula>();
  for (const field of fields) {
    const formula = parseFormula(field.fieldName);
    if (formula) {
      // Convert positions to field codes
      const resolvedAdd = formula.addPositions
        .map(p => positionToCode.get(p))
        .filter((c): c is string => c !== undefined);
      const resolvedSub = formula.subtractPositions
        .map(p => positionToCode.get(p))
        .filter((c): c is string => c !== undefined);

      if (resolvedAdd.length > 0 || resolvedSub.length > 0) {
        formulaMap.set(field.fieldCode, { addPositions: resolvedAdd, subtractPositions: resolvedSub });
      }
    }
  }
  return formulaMap;
}

/**
 * Recalculate all computed field values
 */
export function recalculate(
  fieldValues: Record<string, string>,
  formulaMap: Map<string, FieldFormula>,
  fields: DeclarationField[]
): Record<string, string> {
  const updated = { ...fieldValues };

  for (const [fieldCode, formula] of formulaMap) {
    let sum = 0;
    for (const code of formula.addPositions) {
      sum += parseFloat(updated[code] || '0') || 0;
    }
    for (const code of formula.subtractPositions) {
      sum -= parseFloat(updated[code] || '0') || 0;
    }

    // Find field to determine precision
    const field = fields.find(f => f.fieldCode === fieldCode);
    const precision = getDecimalPlaces(field?.dataType ?? 'Number');
    updated[fieldCode] = sum === 0 ? '' : sum.toFixed(precision);
  }

  return updated;
}

/**
 * Get validation constraints for a data type
 */
export function getInputProps(dataType: string): { step: string; min: string; max: string } {
  const match = dataType.match(/Number\s*\((\d+),(\d+)\)/);
  if (match) {
    const intDigits = parseInt(match[1]);
    const decDigits = parseInt(match[2]);
    const maxVal = Math.pow(10, intDigits) - Math.pow(10, -decDigits);
    return {
      step: (1 / Math.pow(10, decDigits)).toFixed(decDigits),
      min: '0',
      max: maxVal.toFixed(decDigits),
    };
  }
  // Plain "Number" — integer
  return { step: '1', min: '0', max: '999999999' };
}

/**
 * Validate a field value against its dataType constraints.
 * Returns an error message or null if valid.
 */
export function validateFieldValue(value: string, dataType: string): string | null {
  if (!value || value === '') return null;
  const match = dataType.match(/Number\s*\((\d+),(\d+)\)/);
  if (!match) return null;
  const maxIntDigits = parseInt(match[1]);
  const maxDecDigits = parseInt(match[2]);
  const parts = value.split('.');
  const intPart = parts[0].replace(/^-?0*/, '') || '0';
  if (intPart.length > maxIntDigits) {
    return `Maks. ${maxIntDigits} cyfr przed przecinkiem`;
  }
  if (parts.length > 1 && parts[1].length > maxDecDigits) {
    return `Maks. ${maxDecDigits} cyfr po przecinku`;
  }
  return null;
}

function getDecimalPlaces(dataType: string): number {
  const match = dataType.match(/Number\s*\((\d+),(\d+)\)/);
  return match ? parseInt(match[2]) : 0;
}
