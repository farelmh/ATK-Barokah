/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package struk;

/**
 *
 * @author farel manalif h
 */
public class ItemPenjualan {

    private String namaBarang;
    private int jumlah;
    private int harga;
    private int subtotal;

    public ItemPenjualan(String nama, int jumlah, int harga) {
        this.namaBarang = nama;
        this.jumlah = jumlah;
        this.harga = harga;
        this.subtotal = jumlah * harga;
    }

    public String getnamaBarang() {
        return namaBarang;
    }

    public int getjumlah() {
        return jumlah;
    }

    public int getharga() {
        return harga;
    }

    public int getsubtotal() {
        return subtotal;
    }
}
