package fungsi_lain;

import java.util.Comparator;
import java.util.regex.Pattern;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class CariData extends DefaultTableModel {

    public static void TableSorter(JTable table, JTextField tf, int[] index, int[] indexUang, int[] indexAngka) {

        Comparator<String> RpComparator = (s1, s2) -> {
            double n1 = formatUang.setDefault(s1);
            double n2 = formatUang.setDefault(s2);

            return Double.compare(n1, n2);
        };

        Comparator<String> IntComparator = (s1, s2) -> {
            int n1 = Integer.parseInt(s1);
            int n2 = Integer.parseInt(s2);
            return Integer.compare(n1, n2);
        };

        TableModel model = table.getModel();
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);

        if (indexUang != null) {
            for (int col : indexUang) {
                sorter.setComparator(col, RpComparator);
            }
        }

        if (indexAngka != null) {
            for (int col : indexAngka) {
                sorter.setComparator(col, IntComparator);
            }
        }

        // Nonaktifkan sorting untuk kolom tipe Boolean
        for (int i = 0; i < model.getColumnCount(); i++) {
            if (model.getColumnClass(i) == Boolean.class) {
                sorter.setSortable(i, false);
            }
            if (model.getColumnClass(i) == String.class) {
                
            }
        }

        table.setRowSorter(sorter);

        tf.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filter();
            }

            private void filter() {
                String keyword = tf.getText().trim();

                if (keyword.isEmpty()) {
                    sorter.setRowFilter(null); // Tampilkan semua baris
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(keyword), index));
                }
            }

        });
    }
    
    public static void resetTableSorting(JTable table) {
    // Mendapatkan row sorter yang sedang digunakan
    TableRowSorter<? extends TableModel> sorter = 
        (TableRowSorter<? extends TableModel>) table.getRowSorter();
    
    if (sorter != null) {
        // Hapus semua urutan sorting yang aktif
        sorter.setSortKeys(null);
        
        // Atau alternatif lain: gunakan daftar kosong
        // sorter.setSortKeys(new ArrayList<>());
        
        // Refresh tabel
        sorter.sort();
    }
}

    public static void hitungTotal(JTable table, JTextField txt_jumlah, JTextField txt_total, int rowjumlah, int rowtotal) {
        TableModel model = table.getModel();
        int jumlah = 0;
        double total = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            int jumlah1 = Integer.parseInt(model.getValueAt(i, rowjumlah).toString());
            String totalstr = (String) model.getValueAt(i, rowtotal);
            double total1 = formatUang.setDefault(totalstr);

            jumlah += jumlah1;
            total += total1;
        }

        String jml = String.valueOf(jumlah);
        String ttl = formatUang.formatRp(total);

        txt_jumlah.setText(jml);
        txt_total.setText(ttl);
    }

    ///
    public static void TableSorterDenganHitung(JTable table, JTextField tf, int[] index,
            JTextField txt_jumlah, JTextField txt_total,
            int rowjumlah, int rowtotal) {

        TableModel model = table.getModel();
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        tf.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterDanHitung();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterDanHitung();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterDanHitung();
            }

            private void filterDanHitung() {
                String keyword = tf.getText();
                sorter.setRowFilter(keyword.trim().isEmpty() ? null : RowFilter.regexFilter("(?i)" + keyword, index));

                // Hitung total setelah filter
                int jumlah = 0;
                double total = 0;

                for (int i = 0; i < table.getRowCount(); i++) {
                    int jumlah1 = Integer.parseInt(table.getValueAt(i, rowjumlah).toString());
                    String totalstr = (String) table.getValueAt(i, rowtotal);
                    double total1 = formatUang.setDefault(totalstr);
                    jumlah += jumlah1;
                    total += total1;
                }

                String jml = String.valueOf(jumlah);
                String ttl = formatUang.formatRp(total);
                txt_jumlah.setText(jml);
                txt_total.setText(ttl);
            }
        });
    }
}
