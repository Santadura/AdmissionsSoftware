package com.tuyensinh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "xt_tohop_monthi")
public class XtToHopMon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idtohop")
    private Integer idtohop;

    @Column(name = "matohop", nullable = false)
    private String matohop;

    @Column(name = "mon1", nullable = false)
    private String mon1;

    @Column(name = "mon2", nullable = false)
    private String mon2;

    @Column(name = "mon3", nullable = false)
    private String mon3;

    @Column(name = "tentohop")
    private String tentohop;

    public XtToHopMon() {}

    public XtToHopMon(String matohop, String mon1, String mon2, String mon3, String tentohop) {
        this.matohop = matohop;
        this.mon1 = mon1;
        this.mon2 = mon2;
        this.mon3 = mon3;
        this.tentohop = tentohop;
    }

    public Integer getIdtohop() { return idtohop; }
    public void setIdtohop(Integer idtohop) { this.idtohop = idtohop; }
    public String getMatohop() { return matohop; }
    public void setMatohop(String matohop) { this.matohop = matohop; }
    public String getMon1() { return mon1; }
    public void setMon1(String mon1) { this.mon1 = mon1; }
    public String getMon2() { return mon2; }
    public void setMon2(String mon2) { this.mon2 = mon2; }
    public String getMon3() { return mon3; }
    public void setMon3(String mon3) { this.mon3 = mon3; }
    public String getTentohop() { return tentohop; }
    public void setTentohop(String tentohop) { this.tentohop = tentohop; }

    @Override
    public String toString() { return matohop + " - " + tentohop; }
}
