package fungsi_tambah_data;

import barokah_atk.konek;
import java.awt.Frame;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class FormTambahOperasional extends FormTambah {

    konek k = new konek();

    public FormTambahOperasional(Frame parent) {
        super(parent, "Tambah Biaya Operasional", "Tanggal", "Keterangan", "Total Biaya");
        k.connect();
        fieldMap.get("Tanggal").setEnabled(false);

        setTanggal();
    }

    private void setTanggal() {
        LocalDate tgl1 = LocalDate.now();
        Locale localeID = Locale.forLanguageTag("id-ID");
        Locale.setDefault(localeID);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd MMMM yyyy", localeID);
        String tanggal = tgl1.format(df);
        fieldMap.get("Tanggal").setText(tanggal);
    }

    @Override
    protected boolean validateInput() {
        String keterangan = getFieldValue("Keterangan");
        String biaya = getFieldValue("Total Biaya");

        if (keterangan.isEmpty() || biaya.isEmpty()) {
            setPesan("Semua kolom harus diisi!");
            return false;
        }
        if (keterangan.length() < 10) {
            setPesan("Keterangan setidaknya terdiri dari 10 karakter / huruf");
            return false;
        }
        if (!biaya.matches("\\d+")) {
            setPesan("Biaya harus berupa angka!");
            return false;
        }
        double biayadbl = Double.parseDouble(biaya);
        if (biayadbl < 100) {
            setPesan("Biaya minimal Rp 100!");
            return false;
        }
        return true;
    }

    @Override
    protected boolean isDuplicate(String column, String value) {
        return true;
    }

}
