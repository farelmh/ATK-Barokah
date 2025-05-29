/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package struk;

public class DataStrukPenjualan {
    private String id, tanggal, kasir = "";
    private double total, bayar, kembalian = 0;
    
    public DataStrukPenjualan() {
        
    }
    
    public DataStrukPenjualan(String id, String tanggal, String kasir, double total, double bayar, double kembalian) {
            this.id = id;
            this.kasir = kasir;
            this.tanggal = tanggal;
            this.total = total;
            this.bayar = bayar;
            this.kembalian = kembalian;
        }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public String getKasir() {
        return kasir;
    }

    public void setKasir(String kasir) {
        this.kasir = kasir;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getBayar() {
        return bayar;
    }

    public void setBayar(double bayar) {
        this.bayar = bayar;
    }

    public double getKembalian() {
        return kembalian;
    }

    public void setKembalian(double kembalian) {
        this.kembalian = kembalian;
    }
    
    
}
