package com.tuyensinh.service;

import java.math.BigDecimal;
import java.util.List;

import com.tuyensinh.entity.BonusScore;
import com.tuyensinh.repository.BonusScoreRepository;

public class BonusScoreService {

    private final BonusScoreRepository repository;

    public BonusScoreService() {
        this.repository = new BonusScoreRepository();
    }

    public List<BonusScore> getBonusScores(String searchTerm) {
        return repository.findAll(searchTerm);
    }

    public BonusScore getById(Integer id) {
        return repository.findById(id);
    }

    public void addBonusScore(BonusScore bonusScore) {
        prepare(bonusScore);
        repository.save(bonusScore);
    }

    public void updateBonusScore(BonusScore bonusScore) {
        if (bonusScore.getId() == null) {
            throw new RuntimeException("Thieu ID diem cong.");
        }
        prepare(bonusScore);
        repository.update(bonusScore);
    }

    public void deleteBonusScore(Integer id) {
        if (id == null) {
            throw new RuntimeException("Vui long chon ban ghi diem cong.");
        }
        repository.delete(id);
    }

    private void prepare(BonusScore bonusScore) {
        bonusScore.setCccd(required(bonusScore.getCccd(), "CCCD"));
        bonusScore.setMaNganh(required(bonusScore.getMaNganh(), "Ma nganh"));
        bonusScore.setMaToHop(required(bonusScore.getMaToHop(), "Ma to hop"));
        bonusScore.setPhuongThuc(clean(bonusScore.getPhuongThuc()));
        bonusScore.setGhiChu(clean(bonusScore.getGhiChu()));

        BigDecimal diemCc = zeroIfNull(bonusScore.getDiemCc());
        BigDecimal diemUtxt = zeroIfNull(bonusScore.getDiemUtxt());
        if (bonusScore.getDiemTong() == null) {
            bonusScore.setDiemTong(diemCc.add(diemUtxt));
        }

        bonusScore.setDcKeys(
                bonusScore.getCccd() + "_" + bonusScore.getMaNganh() + "_" + bonusScore.getMaToHop());
    }

    private String required(String value, String fieldName) {
        String cleaned = clean(value);
        if (cleaned == null) {
            throw new RuntimeException(fieldName + " khong duoc de trong.");
        }
        return cleaned;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
