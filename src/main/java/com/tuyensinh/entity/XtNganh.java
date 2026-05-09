package com.tuyensinh.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "xt_nganh")
public class XtNganh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idnganh")
    private Integer idnganh;

    @Column(name = "manganh")
    private String manganh;

    @Column(name = "tennganh")
    private String tennganh;

    @Column(name = "n_tohopgoc")
    private String nTohopgoc;

    @Column(name = "n_chitieu")
    private Integer nChitieu;

    @Column(name = "n_diemsan")
    private BigDecimal nDiemsan;

    @Column(name = "n_diemtrungtuyen")
    private BigDecimal nDiemtrungtuyen;

    @Column(name = "n_tuyenthang")
    private String nTuyenthang;

    @Column(name = "n_dgnl")
    private String nDgnl;

    @Column(name = "n_thpt")
    private String nThpt;

    @Column(name = "n_vsat")
    private String nVsat;

    @Column(name = "sl_xtt")
    private Integer slXtt;

    @Column(name = "sl_dgnl")
    private Integer slDgnl;

    @Column(name = "sl_vsat")
    private Integer slVsat;

    @Column(name = "sl_thpt")
    private String slThpt;

    public Integer getIdnganh() {
        return idnganh;
    }

    public void setIdnganh(Integer idnganh) {
        this.idnganh = idnganh;
    }

    public String getManganh() {
        return manganh;
    }

    public void setManganh(String manganh) {
        this.manganh = manganh;
    }

    public String getTennganh() {
        return tennganh;
    }

    public void setTennganh(String tennganh) {
        this.tennganh = tennganh;
    }

    public String getNTohopgoc() {
        return nTohopgoc;
    }

    public void setNTohopgoc(String nTohopgoc) {
        this.nTohopgoc = nTohopgoc;
    }

    public Integer getNChitieu() {
        return nChitieu;
    }

    public void setNChitieu(Integer nChitieu) {
        this.nChitieu = nChitieu;
    }

    public BigDecimal getNDiemsan() {
        return nDiemsan;
    }

    public void setNDiemsan(BigDecimal nDiemsan) {
        this.nDiemsan = nDiemsan;
    }

    public BigDecimal getNDiemtrungtuyen() {
        return nDiemtrungtuyen;
    }

    public void setNDiemtrungtuyen(BigDecimal nDiemtrungtuyen) {
        this.nDiemtrungtuyen = nDiemtrungtuyen;
    }

    public String getNTuyenthang() {
        return nTuyenthang;
    }

    public void setNTuyenthang(String nTuyenthang) {
        this.nTuyenthang = nTuyenthang;
    }

    public String getNDgnl() {
        return nDgnl;
    }

    public void setNDgnl(String nDgnl) {
        this.nDgnl = nDgnl;
    }

    public String getNThpt() {
        return nThpt;
    }

    public void setNThpt(String nThpt) {
        this.nThpt = nThpt;
    }

    public String getNVsat() {
        return nVsat;
    }

    public void setNVsat(String nVsat) {
        this.nVsat = nVsat;
    }

    public Integer getSlXtt() {
        return slXtt;
    }

    public void setSlXtt(Integer slXtt) {
        this.slXtt = slXtt;
    }

    public Integer getSlDgnl() {
        return slDgnl;
    }

    public void setSlDgnl(Integer slDgnl) {
        this.slDgnl = slDgnl;
    }

    public Integer getSlVsat() {
        return slVsat;
    }

    public void setSlVsat(Integer slVsat) {
        this.slVsat = slVsat;
    }

    public String getSlThpt() {
        return slThpt;
    }

    public void setSlThpt(String slThpt) {
        this.slThpt = slThpt;
    }

    @Override
    public String toString() {
        return "XtNganh{" +
                "idnganh=" + idnganh +
                ", manganh='" + manganh + '\'' +
                ", tennganh='" + tennganh + '\'' +
                '}';
    }
}