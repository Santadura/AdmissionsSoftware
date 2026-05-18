package com.tuyensinh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "xt_bangquydoi")
public class ScoreConversion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idqd")
    private Integer idqd;

    @Column(name = "d_phuongthuc", length = 45)
    private String phuongThuc;

    @Column(name = "d_tohop", length = 45)
    private String toHop;

    @Column(name = "d_mon", length = 45)
    private String mon;

    @Column(name = "d_diema", columnDefinition = "decimal(6,2)")
    private Double diemA;

    @Column(name = "d_diemb", columnDefinition = "decimal(6,2)")
    private Double diemB;

    @Column(name = "d_diemc", columnDefinition = "decimal(6,2)")
    private Double diemC;

    @Column(name = "d_diemd", columnDefinition = "decimal(6,2)")
    private Double diemD;

    @Column(name = "d_maquydoi", length = 45)
    private String maQuyDoi;

    @Column(name = "d_phanvi", length = 45)
    private String phanVi;

    public ScoreConversion() {
    }

    // Getters and Setters
    public Integer getIdqd() { return idqd; }
    public void setIdqd(Integer idqd) { this.idqd = idqd; }

    public String getPhuongThuc() { return phuongThuc; }
    public void setPhuongThuc(String phuongThuc) { this.phuongThuc = phuongThuc; }

    public String getToHop() { return toHop; }
    public void setToHop(String toHop) { this.toHop = toHop; }

    public String getMon() { return mon; }
    public void setMon(String mon) { this.mon = mon; }

    public Double getDiemA() { return diemA; }
    public void setDiemA(Double diemA) { this.diemA = diemA; }

    public Double getDiemB() { return diemB; }
    public void setDiemB(Double diemB) { this.diemB = diemB; }

    public Double getDiemC() { return diemC; }
    public void setDiemC(Double diemC) { this.diemC = diemC; }

    public Double getDiemD() { return diemD; }
    public void setDiemD(Double diemD) { this.diemD = diemD; }

    public String getMaQuyDoi() { return maQuyDoi; }
    public void setMaQuyDoi(String maQuyDoi) { this.maQuyDoi = maQuyDoi; }

    public String getPhanVi() { return phanVi; }
    public void setPhanVi(String phanVi) { this.phanVi = phanVi; }
}