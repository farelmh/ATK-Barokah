/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package struk;

import admin.Transaksi;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

/**
 *
 * @author farel manalif h
 */
public class CetakStrukJual {

    private List<ItemPenjualan> listBarang;
    private JRBeanCollectionDataSource dataSource;
    private Map<String, Object> parameters;

    public void isiStruk(DataStrukPenjualan data) {
        listBarang = Transaksi.getListBarang();
        dataSource = new JRBeanCollectionDataSource(listBarang);

        parameters = new HashMap<>();
        parameters.put("IDPenjualan", data.getId());
        parameters.put("Tanggal", data.getTanggal());      // Tanggal
        parameters.put("Kasir", data.getKasir());           // Kasir
        parameters.put("Total", data.getTotal());              // Total    
        parameters.put("Dibayar", data.getBayar());             // Bayar
        parameters.put("Kembali", data.getKembalian());              // Kembali
    }

    public void cetak() {
        try {
            InputStream is = CetakStrukJual.class.getResourceAsStream("/struk/strukJual.jasper");
            JasperPrint print = JasperFillManager.fillReport(is, parameters, dataSource);
            JasperViewer.viewReport(print, false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
