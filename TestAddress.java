
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

public class TestAddress {
    private static final Pattern NON_ADDRESS_CHARS = Pattern.compile("[^a-z0-9\\s]");
    private static final Pattern ADDRESS_PREFIXES = Pattern.compile("\\b(thanh pho|tp|tinh|quan|huyen|thi xa|phuong|thi tran|p|q|h)\\b");
    private static final Pattern LEADING_ADMIN_PREFIX = Pattern.compile("^(xa|phuong|thi tran)\\s+");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern NUMERIC_NAME = Pattern.compile("\\d+");
    private static final int MIN_ADDRESS_NAME_MATCH_LENGTH = 5;

    public static void main(String[] args) {
        String address = "Số 161 Phố Thái Hà, Phường Láng Hạ, quận Đống Đa, Thành phố Hà Nội";
        String normalizedAddress = normalizeAddress(address);
        System.out.println("Normalized Address: " + normalizedAddress);

        String[] districts = {"Quận Ba Đình", "Quận Đống Đa", "Huyện Gia Lâm", "Đống Đa"};
        for (String d : districts) {
            System.out.println("District " + d + " match: " + bestNameLength(normalizedAddress, d, Collections.emptyList()));
        }

        String[] wards = {"Phường Láng Hạ", "Láng Hạ", "P. Láng Hạ", "Phường Thái Hà"};
        for (String w : wards) {
            System.out.println("Ward " + w + " match: " + bestNameLength(normalizedAddress, w, Collections.emptyList()));
        }
    }

    private static String normalizeAddress(String value) {
        if (value == null) return "";
        String ascii = value.replace('\u0110', 'D').replace('\u0111', 'd');
        String normalized = Normalizer.normalize(ascii, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
        normalized = NON_ADDRESS_CHARS.matcher(normalized).replaceAll(" ");
        normalized = ADDRESS_PREFIXES.matcher(normalized).replaceAll(" ");
        return WHITESPACE.matcher(normalized).replaceAll(" ").trim();
    }

    private static int bestNameLength(String normalizedAddress, String name, List<String> extensions) {
        return addressNameVariants(name, extensions).stream()
                .filter(variant -> containsNormalizedName(normalizedAddress, variant))
                .mapToInt(String::length)
                .max()
                .orElse(0);
    }

    private static List<String> addressNameVariants(String name, List<String> extensions) {
        List<String> names = new ArrayList<>();
        names.add(name);
        names.addAll(extensions);
        return names.stream().filter(Objects::nonNull).flatMap(value -> normalizedNameVariants(value).stream()).distinct().toList();
    }

    private static List<String> normalizedNameVariants(String value) {
        String normalized = normalizeAddress(value);
        if (normalized.isBlank()) return List.of();
        List<String> variants = new ArrayList<>();
        variants.add(normalized);
        String withoutLeadingAdminPrefix = LEADING_ADMIN_PREFIX.matcher(normalized).replaceFirst("").trim();
        if (!withoutLeadingAdminPrefix.isBlank()) variants.add(withoutLeadingAdminPrefix);
        String withoutTrailingNumber = normalized.replaceAll("\\s+\\d+$", "").trim();
        if (!withoutTrailingNumber.isBlank()) {
            variants.add(withoutTrailingNumber);
            String compactTrailingVariant = LEADING_ADMIN_PREFIX.matcher(withoutTrailingNumber).replaceFirst("").trim();
            if (!compactTrailingVariant.isBlank()) variants.add(compactTrailingVariant);
        }
        return keepStrongVariants(variants);
    }

    private static List<String> keepStrongVariants(List<String> variants) {
        return variants.stream().distinct().filter(value -> value.length() >= MIN_ADDRESS_NAME_MATCH_LENGTH || NUMERIC_NAME.matcher(value).matches()).toList();
    }

    private static boolean containsNormalizedName(String normalizedAddress, String normalizedName) {
        return Pattern.compile("(^|\\s)" + Pattern.quote(normalizedName) + "(\\s|$)").matcher(normalizedAddress).find();
    }
}
