package com.tuyensinh.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "xt_thisinh_chungchi")
public class EnglishCertificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "thisinh_cccd", length = 12, nullable = false)
    private String cccd;

    @Column(name = "loai_cc", length = 50, nullable = false)
    private String loaiCc;

    @Column(name = "diem_so", precision = 6, scale = 2, nullable = false)
    private BigDecimal diemSo;

    @Column(name = "ngay_cap")
    private LocalDate ngayCap;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }
    public String getLoaiCc() { return loaiCc; }
    public void setLoaiCc(String loaiCc) { this.loaiCc = loaiCc; }
    public BigDecimal getDiemSo() { return diemSo; }
    public void setDiemSo(BigDecimal diemSo) { this.diemSo = diemSo; }
    public LocalDate getNgayCap() { return ngayCap; }
    public void setNgayCap(LocalDate ngayCap) { this.ngayCap = ngayCap; }
}
