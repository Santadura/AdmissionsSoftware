package com.tuyensinh;

import java.util.List;

import org.hibernate.Session;

import com.tuyensinh.config.HibernateUtil;
import com.tuyensinh.entity.XtNganh;

public class TestConnection {
    public static void main(String[] args) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<XtNganh> dsNganh = session.createQuery("from XtNganh", XtNganh.class).list();

            System.out.println("Kết nối thành công. Số ngành: " + dsNganh.size());
            for (XtNganh nganh : dsNganh) {
                System.out.println(nganh);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}