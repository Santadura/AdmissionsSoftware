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

    @Column(name = "nganh_id")
    private Integer nganhId;

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

    public Integer getNganhId() {
        return nganhId;
    }

    public void setNganhId(Integer nganhId) {
        this.nganhId = nganhId;
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

    public Double getDoLech() {
        return doLech;
    }

    public void setDoLech(Double doLech) {
        this.doLech = doLech;
    }
}
