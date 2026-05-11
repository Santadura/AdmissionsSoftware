package com.tuyensinh.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.tuyensinh.entity.Aspiration;
import com.tuyensinh.repository.AspirationRepository;
import com.tuyensinh.repository.BonusScoreRepository;

public class AspirationService {

    private final AspirationRepository repository;
    private final BonusScoreRepository bonusRepository;

    public AspirationService() {
        this.repository = new AspirationRepository();
        this.bonusRepository = new BonusScoreRepository();
    }

    public List<Aspiration> getAspirations(String searchTerm) {
        return repository.findAll(searchTerm);
    }

    public Aspiration getById(Integer id) {
        return repository.findById(id);
    }

    public void addAspiration(Aspiration aspiration) {
        prepare(aspiration);
        repository.save(aspiration);
    }

    public void updateAspiration(Aspiration aspiration) {
        if (aspiration.getId() == null) {
            throw new RuntimeException("Thieu ID nguyen vong.");
        }
        prepare(aspiration);
        repository.update(aspiration);
    }

    public void deleteAspiration(Integer id) {
        if (id == null) {
            throw new RuntimeException("Vui long chon nguyen vong.");
        }
        repository.delete(id);
    }

    public AdmissionResult runAdmission() {
        return calculateAdmission(true);
    }

    public AdmissionResult previewAdmission() {
        return calculateAdmission(false);
    }

    private AdmissionResult calculateAdmission(boolean saveResult) {
        List<Aspiration> aspirations = repository.findAll();
        Map<String, BigDecimal> floors = repository.findMajorFloors();
        Map<String, Integer> quotas = repository.findMajorQuotas();
        List<Aspiration> eligibleAspirations = new ArrayList<>();

        int belowFloor = 0;
        int missingScore = 0;
        int missingMajorConfig = 0;

        for (Aspiration aspiration : aspirations) {
            BigDecimal bonus = bonusRepository.sumBonus(
                    aspiration.getCccd(),
                    aspiration.getMaNganh(),
                    aspiration.getToHop(),
                    aspiration.getPhuongThuc());
            aspiration.setDiemCong(bonus);
            aspiration.setDiemXetTuyen(totalScore(aspiration));

            if (aspiration.getDiemThxt() == null) {
                aspiration.setKetQua("chuaxet");
                missingScore++;
                continue;
            }

            if (!floors.containsKey(aspiration.getMaNganh()) || !quotas.containsKey(aspiration.getMaNganh())) {
                aspiration.setKetQua("chuacauhinh");
                missingMajorConfig++;
                continue;
            }

            BigDecimal floor = floors.get(aspiration.getMaNganh());
            if (aspiration.getDiemXetTuyen().compareTo(floor) < 0) {
                aspiration.setKetQua("duoisan");
                belowFloor++;
                continue;
            }

            eligibleAspirations.add(aspiration);
        }

        Comparator<Aspiration> ranking = Comparator
                .comparing(Aspiration::getDiemXetTuyen, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(a -> a.getThuTu() == null ? Integer.MAX_VALUE : a.getThuTu())
                .thenComparing(a -> a.getId() == null ? Integer.MAX_VALUE : a.getId());

        int passed = 0;
        Set<String> admittedCandidates = new HashSet<>();
        Map<String, Integer> remainingQuotas = new HashMap<>(quotas);
        TreeSet<Integer> priorities = new TreeSet<>();
        for (Aspiration aspiration : eligibleAspirations) {
            priorities.add(aspiration.getThuTu() == null ? Integer.MAX_VALUE : aspiration.getThuTu());
        }

        for (Integer priority : priorities) {
            Map<String, List<Aspiration>> currentRoundByMajor = new HashMap<>();
            for (Aspiration aspiration : eligibleAspirations) {
                int aspirationPriority = aspiration.getThuTu() == null ? Integer.MAX_VALUE : aspiration.getThuTu();
                if (aspirationPriority == priority && !admittedCandidates.contains(aspiration.getCccd())) {
                    currentRoundByMajor
                            .computeIfAbsent(aspiration.getMaNganh(), key -> new ArrayList<>())
                            .add(aspiration);
                }
            }

            for (Map.Entry<String, List<Aspiration>> entry : currentRoundByMajor.entrySet()) {
                int remainingQuota = Math.max(remainingQuotas.getOrDefault(entry.getKey(), 0), 0);
                if (remainingQuota <= 0) {
                    continue;
                }

                List<Aspiration> ranked = entry.getValue();
                ranked.sort(ranking);
                for (Aspiration aspiration : ranked) {
                    if (remainingQuota <= 0) {
                        break;
                    }
                    aspiration.setKetQua("trungtuyen");
                    admittedCandidates.add(aspiration.getCccd());
                    remainingQuota--;
                    passed++;
                }
                remainingQuotas.put(entry.getKey(), remainingQuota);
            }
        }

        int failed = 0;
        for (Aspiration aspiration : eligibleAspirations) {
            if (!"trungtuyen".equals(aspiration.getKetQua())) {
                aspiration.setKetQua("khongtrungtuyen");
                failed++;
            }
        }

        if (saveResult) {
            repository.saveAll(aspirations);
        }
        return new AdmissionResult(aspirations.size(), passed, failed, belowFloor, missingScore, missingMajorConfig);
    }

    private void prepare(Aspiration aspiration) {
        aspiration.setCccd(required(aspiration.getCccd(), "CCCD"));
        aspiration.setMaNganh(required(aspiration.getMaNganh(), "Ma nganh"));
        if (aspiration.getThuTu() == null || aspiration.getThuTu() <= 0) {
            throw new RuntimeException("Thu tu nguyen vong phai lon hon 0.");
        }
        aspiration.setPhuongThuc(clean(aspiration.getPhuongThuc()));
        aspiration.setToHop(clean(aspiration.getToHop()));
        aspiration.setKetQua(clean(aspiration.getKetQua()));

        aspiration.setDiemCong(zeroIfNull(aspiration.getDiemCong()));
        aspiration.setDiemXetTuyen(totalScore(aspiration));
        aspiration.setNvKeys(aspiration.getCccd() + "_" + aspiration.getMaNganh() + "_"
                + (aspiration.getPhuongThuc() == null ? "" : aspiration.getPhuongThuc()));
        if (aspiration.getKetQua() == null) {
            aspiration.setKetQua("chuaxet");
        }
    }

    private BigDecimal totalScore(Aspiration aspiration) {
        return zeroIfNull(aspiration.getDiemThxt())
                .add(zeroIfNull(aspiration.getDiemUtqd()))
                .add(zeroIfNull(aspiration.getDiemCong()));
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

    public static class AdmissionResult {
        private final int total;
        private final int passed;
        private final int failed;
        private final int belowFloor;
        private final int missingScore;
        private final int missingMajorConfig;

        public AdmissionResult(
                int total,
                int passed,
                int failed,
                int belowFloor,
                int missingScore,
                int missingMajorConfig) {
            this.total = total;
            this.passed = passed;
            this.failed = failed;
            this.belowFloor = belowFloor;
            this.missingScore = missingScore;
            this.missingMajorConfig = missingMajorConfig;
        }

        public int getTotal() {
            return total;
        }

        public int getPassed() {
            return passed;
        }

        public int getFailed() {
            return failed;
        }

        public int getBelowFloor() {
            return belowFloor;
        }

        public int getMissingScore() {
            return missingScore;
        }

        public int getMissingMajorConfig() {
            return missingMajorConfig;
        }
    }
}
