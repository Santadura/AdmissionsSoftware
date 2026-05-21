package com.tuyensinh.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "xt_diemthixettuyen")
public class CandidateScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddiemthi")
    private Integer iddiemthi;

    @Column(name = "cccd", length = 20)
    private String cccd;

    @Column(name = "sobaodanh", length = 45)
    private String sobaodanh;

    // 3 = THPT, 4 = DGNL, 5 = VSAT
    @Column(name = "d_phuongthuc", length = 10)
    private String dPhuongthuc;

    @Column(name = "`TO`")
    private BigDecimal to;

    @Column(name = "`LI`")
    private BigDecimal li;

    @Column(name = "`HO`")
    private BigDecimal ho;

    @Column(name = "`SI`")
    private BigDecimal si;

    @Column(name = "`SU`")
    private BigDecimal su;

    @Column(name = "`DI`")
    private BigDecimal di;

    @Column(name = "`VA`")
    private BigDecimal va;

    @Column(name = "`N1_THI`")
    private BigDecimal n1Thi;

    @Column(name = "`N1_CC`")
    private BigDecimal n1Cc;

    @Column(name = "`CNCN`")
    private BigDecimal cncn;

    @Column(name = "`CNNN`")
    private BigDecimal cnnn;

    @Column(name = "`TI`")
    private BigDecimal ti;

    @Column(name = "`KTPL`")
    private BigDecimal ktpl;

    @Column(name = "`NL1`")
    private BigDecimal nl1;

    @Column(name = "`NK1`")
    private BigDecimal nk1;

    @Column(name = "`NK2`")
    private BigDecimal nk2;

    @Column(name = "`NK3`")
    private BigDecimal nk3;

    @Column(name = "`NK4`")
    private BigDecimal nk4;

    public CandidateScore() {
    }

    public Integer getIddiemthi() {
        return iddiemthi;
    }

    public void setIddiemthi(Integer iddiemthi) {
        this.iddiemthi = iddiemthi;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getSobaodanh() {
        return sobaodanh;
    }

    public void setSobaodanh(String sobaodanh) {
        this.sobaodanh = sobaodanh;
    }

    public String getDPhuongthuc() {
        return dPhuongthuc;
    }

    public void setDPhuongthuc(String dPhuongthuc) {
        this.dPhuongthuc = dPhuongthuc;
    }

    public BigDecimal getTo() {
        return to;
    }

    public void setTo(BigDecimal to) {
        this.to = to;
    }

    public BigDecimal getLi() {
        return li;
    }

    public void setLi(BigDecimal li) {
        this.li = li;
    }

    public BigDecimal getHo() {
        return ho;
    }

    public void setHo(BigDecimal ho) {
        this.ho = ho;
    }

    public BigDecimal getSi() {
        return si;
    }

    public void setSi(BigDecimal si) {
        this.si = si;
    }

    public BigDecimal getSu() {
        return su;
    }

    public void setSu(BigDecimal su) {
        this.su = su;
    }

    public BigDecimal getDi() {
        return di;
    }

    public void setDi(BigDecimal di) {
        this.di = di;
    }

    public BigDecimal getVa() {
        return va;
    }

    public void setVa(BigDecimal va) {
        this.va = va;
    }

    public BigDecimal getN1Thi() {
        return n1Thi;
    }

    public void setN1Thi(BigDecimal n1Thi) {
        this.n1Thi = n1Thi;
    }

    public BigDecimal getN1Cc() {
        return n1Cc;
    }

    public void setN1Cc(BigDecimal n1Cc) {
        this.n1Cc = n1Cc;
    }

    public BigDecimal getCncn() {
        return cncn;
    }

    public void setCncn(BigDecimal cncn) {
        this.cncn = cncn;
    }

    public BigDecimal getCnnn() {
        return cnnn;
    }

    public void setCnnn(BigDecimal cnnn) {
        this.cnnn = cnnn;
    }

    public BigDecimal getTi() {
        return ti;
    }

    public void setTi(BigDecimal ti) {
        this.ti = ti;
    }

    public BigDecimal getKtpl() {
        return ktpl;
    }

    public void setKtpl(BigDecimal ktpl) {
        this.ktpl = ktpl;
    }

    public BigDecimal getNl1() {
        return nl1;
    }

    public void setNl1(BigDecimal nl1) {
        this.nl1 = nl1;
    }

    public BigDecimal getNk1() {
        return nk1;
    }

    public void setNk1(BigDecimal nk1) {
        this.nk1 = nk1;
    }

    public BigDecimal getNk2() {
        return nk2;
    }

    public void setNk2(BigDecimal nk2) {
        this.nk2 = nk2;
    }

    public BigDecimal getNk3() {
        return nk3;
    }

    public void setNk3(BigDecimal nk3) {
        this.nk3 = nk3;
    }

    public BigDecimal getNk4() {
        return nk4;
    }

    public void setNk4(BigDecimal nk4) {
        this.nk4 = nk4;
    }
}