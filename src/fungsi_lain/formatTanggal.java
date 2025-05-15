package fungsi_lain;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;

public class formatTanggal {

    public static String formatTgl(String tanggalInput) {
        try {
            // Formatter input dengan bulan dalam teks bahasa Indonesia
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            LocalDate localDate = LocalDate.parse(tanggalInput, inputFormatter);
            return localDate.format(outputFormatter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setTanggalIndo(JDateChooser tanggal) {
        Locale localeID = Locale.forLanguageTag("id-ID");
        tanggal.setLocale(localeID); // Locale ke komponen

        // Format tanggal panjang: 07 Mei 2025
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", localeID);

        // Set format langsung ke editor-nya
        tanggal.setDateFormatString("dd MMMM yyyy");
        ((JTextFieldDateEditor) tanggal.getDateEditor()).setFormatterFactory(new DefaultFormatterFactory(new DateFormatter(sdf))
        );
    }

    public static String formatTgl(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return localDate.format(outputFormatter);
    }
}
