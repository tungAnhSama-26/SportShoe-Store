/**
 * Utility lọc từ thô tục, tục tĩu tại Frontend.
 * Tự động che các từ vi phạm thành "*******".
 */
const REPLACEMENT = "*******";

const PROFANITY_WORDS = [
  "ngu", "đồ ngu", "do ngu", "thằng ngu", "thang ngu", "con ngu", "ngu học", "ngu hoc",
  "ngu lồn", "ngu lon", "ngu cặc", "ngu cac", "ngu vkl", "ngu vcl", "ngu vl", "ngu vãi", "ngu vai",
  "ngu như chó", "ngu nhu cho", "ngu quá", "ngu qua", "ngu dốt", "ngu dot",
  "đm", "dm", "dkm", "đkm", "đờ mờ", "do ma", "đụ má", "đụ mẹ", "du ma", "du me",
  "vkl", "vcl", "vl", "cặc", "cac", "cặt", "cat", "con cặc", "con cac",
  "lồn", "lon", "con lồn", "con lon", "cái lồn",
  "đụ", "du", "đéo", "deo", "bố láo", "bo lao", "chửi", "óc chó", "oc cho",
  "mẹ kiếp", "me kiep", "đồ chó", "do cho", "chó đẻ", "cho de",
  "fuck", "fucking", "fucker", "bitch", "shit", "bastard", "asshole", "dick", "pussy", "cunt", "whore", "slut"
];

function escapeRegex(string) {
  return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

export function filterProfanity(input) {
  if (!input || typeof input !== 'string') return input;
  let result = input;

  for (const word of PROFANITY_WORDS) {
    const escaped = escapeRegex(word);
    let regex;
    if (word.includes('.')) {
      regex = new RegExp(escaped, 'gi');
    } else {
      regex = new RegExp(`(?<=\\s|^|[\\s\\p{P}])` + escaped + `(?=\\s|$|[\\s\\p{P}])`, 'gui');
    }
    result = result.replace(regex, REPLACEMENT);
  }

  return result;
}
