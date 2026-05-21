package com.tuyensinh;

import java.util.List;

import org.hibernate.Session;

import com.tuyensinh.config.HibernateUtil;
import com.tuyensinh.entity.XtNganh;
import com.tuyensinh.repository.AspirationRepository;
import com.tuyensinh.repository.BonusScoreRepository;
import com.tuyensinh.service.AspirationService;
import com.tuyensinh.service.AspirationService.AdmissionResult;

public class TestConnection {
    public static void main(String[] args) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<XtNganh> dsNganh = session.createQuery("from XtNganh", XtNganh.class).list();

            System.out.println("Ket noi thanh cong. So nganh: " + dsNganh.size());
            for (XtNganh nganh : dsNganh) {
                System.out.println(nganh);
            }

            BonusScoreRepository bonusRepository = new BonusScoreRepository();
            AspirationRepository aspirationRepository = new AspirationRepository();
            System.out.println("So diem cong: " + bonusRepository.findAllWithMajor("").size());
            System.out.println("So nguyen vong: " + aspirationRepository.findAll().size());
            System.out.println("So nganh co diem san: " + aspirationRepository.findMajorFloors().size());
            System.out.println("So nganh co chi tieu: " + aspirationRepository.findMajorQuotas().size());

            AdmissionResult preview = new AspirationService().previewAdmission();
            System.out.println("Preview xet tuyen - tong: " + preview.getTotal()
                    + ", trung tuyen: " + preview.getPassed()
                    + ", khong trung tuyen: " + preview.getFailed()
                    + ", duoi san: " + preview.getBelowFloor()
                    + ", chua co diem: " + preview.getMissingScore()
                    + ", chua cau hinh nganh: " + preview.getMissingMajorConfig());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            HibernateUtil.shutdown();
        }
    }
}
