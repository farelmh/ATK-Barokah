package fungsi_lain;

import java.text.NumberFormat;
import java.util.Locale;

public class formatUang {

    public static String formatRp(double nilai) {
        NumberFormat format = NumberFormat.getNumberInstance(new Locale("id", "ID"));
        format.setMaximumFractionDigits(0);
        return format.format(nilai);
    }

    public static double setDefault(String nilaiStr) {
        try {
            String clean = nilaiStr.replaceAll("[^\\d,]", "");
            clean = clean.replace(",", ".");
            return Double.parseDouble(clean);

        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
