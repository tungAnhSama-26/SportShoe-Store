import java.text.Normalizer;
import java.util.regex.Pattern;
public class TestAddress2 {
    public static void main(String[] args) {
        String value = "Số 161 Phố Thái Hà, Phường Láng Hạ, Quận Đống Đa, thành phố Hà Nội";
        String ascii = value.replace('\u0110', 'D').replace('\u0111', 'd');
        String normalized = Normalizer.normalize(ascii, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(java.util.Locale.ROOT);
        System.out.println(normalized);
        
        normalized = Pattern.compile("\\b(thanh pho|tp|tinh|quan|huyen|thi xa|phuong|thi tran|p|q|h)\\b").matcher(normalized).replaceAll(" ");
        normalized = Pattern.compile("[^a-z0-9 ]").matcher(normalized).replaceAll(" ");
        normalized = Pattern.compile("\\s+").matcher(normalized).replaceAll(" ").trim();
        System.out.println(normalized);
    }
}
