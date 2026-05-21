package com.tuyensinh.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "xt_nguyenvongxettuyen")
public class Aspiration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idnv")
    private Integer id;

    @Column(name = "thisinh_cccd", length = 12)
    private String cccd;

    @Column(name = "nv_nganh_id")
    private Integer nganhId;

    @Column(name = "nv_tt")
    private Integer thuTu;

    @Column(name = "diem_thxt")
    private BigDecimal diemThxt;

    @Column(name = "diem_utqd")
    private BigDecimal diemUtqd;

    @Column(name = "diem_utxt")
    private BigDecimal diemUtxt;

    @Column(name = "diem_cc")
    private BigDecimal diemCc;

    @Column(name = "diem_cong")
    private BigDecimal diemCong;

    @Column(name = "diem_xettuyen")
    private BigDecimal diemXetTuyen;

    @Column(name = "nv_ketqua")
    private String ketQua;

    @Column(name = "nv_keys")
    private String nvKeys;

    @Column(name = "tt_phuongthuc")
    private String phuongThuc;

    @Column(name = "tt_thm")
    private String ttThm;

    @Column(name = "nv_matohop")
    private String toHop;

    @Column(name = "nv_rank")
    private Integer nvRank;

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

    public Integer getNganhId() {
        return nganhId;
    }

    public void setNganhId(Integer nganhId) {
        this.nganhId = nganhId;
    }

    public Integer getThuTu() {
        return thuTu;
    }

    public void setThuTu(Integer thuTu) {
        this.thuTu = thuTu;
    }

    public BigDecimal getDiemThxt() {
        return diemThxt;
    }

    public void setDiemThxt(BigDecimal diemThxt) {
        this.diemThxt = diemThxt;
    }

    public BigDecimal getDiemUtqd() {
        return diemUtqd;
    }

    public void setDiemUtqd(BigDecimal diemUtqd) {
        this.diemUtqd = diemUtqd;
    }

    public BigDecimal getDiemUtxt() {
        return diemUtxt;
    }

    public void setDiemUtxt(BigDecimal diemUtxt) {
        this.diemUtxt = diemUtxt;
    }

    public BigDecimal getDiemCc() {
        return diemCc;
    }

    public void setDiemCc(BigDecimal diemCc) {
        this.diemCc = diemCc;
    }

    public BigDecimal getDiemCong() {
        return diemCong;
    }

    public void setDiemCong(BigDecimal diemCong) {
        this.diemCong = diemCong;
    }

    public BigDecimal getDiemXetTuyen() {
        return diemXetTuyen;
    }

    public void setDiemXetTuyen(BigDecimal diemXetTuyen) {
        this.diemXetTuyen = diemXetTuyen;
    }

    public String getKetQua() {
        return ketQua;
    }

    public void setKetQua(String ketQua) {
        this.ketQua = ketQua;
    }

    public String getNvKeys() {
        return nvKeys;
    }

    public void setNvKeys(String nvKeys) {
        this.nvKeys = nvKeys;
    }

    public String getPhuongThuc() {
        return phuongThuc;
    }

    public void setPhuongThuc(String phuongThuc) {
        this.phuongThuc = phuongThuc;
    }

    public String getTtThm() {
        return ttThm;
    }

    public void setTtThm(String ttThm) {
        this.ttThm = ttThm;
    }

    public String getToHop() {
        return toHop;
    }

    public void setToHop(String toHop) {
        this.toHop = toHop;
    }

    public Integer getNvRank() {
        return nvRank;
    }

    public void setNvRank(Integer nvRank) {
        this.nvRank = nvRank;
    }
}
