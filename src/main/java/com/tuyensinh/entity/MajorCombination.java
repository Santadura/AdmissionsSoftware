package com.tuyensinh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "xt_nganh_tohop")
public class MajorCombination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "manganh", length = 45)
    private String maNganh;

    @Column(name = "matohop", length = 45)
    private String maToHop;

    @Column(name = "th_mon1", length = 10)
    private String thMon1;

    @Column(name = "hsmon1", columnDefinition = "tinyint")
    private Integer hsMon1;

    @Column(name = "th_mon2", length = 10)
    private String thMon2;

    @Column(name = "hsmon2", columnDefinition = "tinyint")
    private Integer hsMon2;

    @Column(name = "th_mon3", length = 10)
    private String thMon3;

    @Column(name = "hsmon3", columnDefinition = "tinyint")
    private Integer hsMon3;

    @Column(name = "tb_keys", length = 45)
    private String tbKeys;

    @Column(name = "N1", columnDefinition = "tinyint(1)")
    private boolean n1;

    @Column(name = "`TO`", columnDefinition = "tinyint(1)")
    private boolean toan;

    @Column(name = "LI", columnDefinition = "tinyint(1)")
    private boolean ly;

    @Column(name = "HO", columnDefinition = "tinyint(1)")
    private boolean hoa;

    @Column(name = "SI", columnDefinition = "tinyint(1)")
    private boolean sinh;

    @Column(name = "VA", columnDefinition = "tinyint(1)")
    private boolean van;

    @Column(name = "SU", columnDefinition = "tinyint(1)")
    private boolean su;

    @Column(name = "DI", columnDefinition = "tinyint(1)")
    private boolean dia;

    @Column(name = "TI", columnDefinition = "tinyint(1)")
    private boolean tiengAnh;

    @Column(name = "KHAC", columnDefinition = "tinyint(1)")
    private boolean khac;

    @Column(name = "KTPL", columnDefinition = "tinyint(1)")
    private boolean ktpl;

    @Column(name = "dolech", columnDefinition = "decimal(6,2)")
    private Double doLech;

    public MajorCombination() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getThMon1() {
        return thMon1;
    }

    public void setThMon1(String thMon1) {
        this.thMon1 = thMon1;
    }

    public Integer getHsMon1() {
        return hsMon1;
    }

    public void setHsMon1(Integer hsMon1) {
        this.hsMon1 = hsMon1;
    }

    public String getThMon2() {
        return thMon2;
    }

    public void setThMon2(String thMon2) {
        this.thMon2 = thMon2;
    }

    public Integer getHsMon2() {
        return hsMon2;
    }

    public void setHsMon2(Integer hsMon2) {
        this.hsMon2 = hsMon2;
    }

    public String getThMon3() {
        return thMon3;
    }

    public void setThMon3(String thMon3) {
        this.thMon3 = thMon3;
    }

    public Integer getHsMon3() {
        return hsMon3;
    }

    public void setHsMon3(Integer hsMon3) {
        this.hsMon3 = hsMon3;
    }

    public String getTbKeys() {
        return tbKeys;
    }

    public void setTbKeys(String tbKeys) {
        this.tbKeys = tbKeys;
    }

    public boolean isN1() {
        return n1;
    }

    public void setN1(boolean n1) {
        this.n1 = n1;
    }

    public boolean isToan() {
        return toan;
    }

    public void setToan(boolean toan) {
        this.toan = toan;
    }

    public boolean isLy() {
        return ly;
    }

    public void setLy(boolean ly) {
        this.ly = ly;
    }

    public boolean isHoa() {
        return hoa;
    }

    public void setHoa(boolean hoa) {
        this.hoa = hoa;
    }

    public boolean isSinh() {
        return sinh;
    }

    public void setSinh(boolean sinh) {
        this.sinh = sinh;
    }

    public boolean isVan() {
        return van;
    }

    public void setVan(boolean van) {
        this.van = van;
    }

    public boolean isSu() {
        return su;
    }

    public void setSu(boolean su) {
        this.su = su;
    }

    public boolean isDia() {
        return dia;
    }

    public void setDia(boolean dia) {
        this.dia = dia;
    }

    public boolean isTiengAnh() {
        return tiengAnh;
    }

    public void setTiengAnh(boolean tiengAnh) {
        this.tiengAnh = tiengAnh;
    }

    public boolean isKhac() {
        return khac;
    }

    public void setKhac(boolean khac) {
        this.khac = khac;
    }

    public boolean isKtpl() {
        return ktpl;
    }

    public void setKtpl(boolean ktpl) {
        this.ktpl = ktpl;
    }

    public Double getDoLech() {
        return doLech;
    }

    public void setDoLech(Double doLech) {
        this.doLech = doLech;
    }
}