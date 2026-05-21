package com.tuyensinh.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "xt_diemcongxettuyen")
public class BonusScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddiemcong")
    private Integer id;

    @Column(name = "ts_cccd")
    private String cccd;

    @Column(name = "manganh")
    private String maNganh;

    @Column(name = "matohop")
    private String maToHop;

    @Column(name = "phuongthuc")
    private String phuongThuc;

    @Column(name = "diemCC")
    private BigDecimal diemCc;

    @Column(name = "diemUtxt")
    private BigDecimal diemUtxt;

    @Column(name = "diemTong")
    private BigDecimal diemTong;

    @Column(name = "ghichu")
    private String ghiChu;

    @Column(name = "dc_keys")
    private String dcKeys;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getMaNganh() {
        return maNganh;
    }

    public void setMaNganh(String maNganh) {
        this.maNganh = maNganh;
    }

    public String getMaToHop() {
        return maToHop;
    }

    public void setMaToHop(String maToHop) {
        this.maToHop = maToHop;
    }

    public String getPhuongThuc() {
        return phuongThuc;
    }

    public void setPhuongThuc(String phuongThuc) {
        this.phuongThuc = phuongThuc;
    }

    public BigDecimal getDiemCc() {
        return diemCc;
    }

    public void setDiemCc(BigDecimal diemCc) {
        this.diemCc = diemCc;
    }

    public BigDecimal getDiemUtxt() {
        return diemUtxt;
    }

    public void setDiemUtxt(BigDecimal diemUtxt) {
        this.diemUtxt = diemUtxt;
    }

    public BigDecimal getDiemTong() {
        return diemTong;
    }

    public void setDiemTong(BigDecimal diemTong) {
        this.diemTong = diemTong;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getDcKeys() {
        return dcKeys;
    }

    public void setDcKeys(String dcKeys) {
        this.dcKeys = dcKeys;
    }
}
