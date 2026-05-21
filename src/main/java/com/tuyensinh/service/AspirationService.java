package com.tuyensinh.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tuyensinh.entity.Aspiration;
import com.tuyensinh.repository.AspirationRepository;
import com.tuyensinh.repository.BonusScoreRepository;

public class AspirationService {

    private final AspirationRepository repository;
    private final BonusScoreRepository bonusRepository;
    private final com.tuyensinh.repository.NganhRepository nganhRepository = new com.tuyensinh.repository.NganhRepository();
    private final com.tuyensinh.repository.CandidateRepository candidateRepository;

    public AspirationService() {
        this.repository = new AspirationRepository();
        this.bonusRepository = new BonusScoreRepository();
        this.candidateRepository = new com.tuyensinh.repository.CandidateRepository();
    }

    public List<Object[]> getAspirationsWithCandidate(String searchTerm) {
        return repository.findAllWithCandidate(searchTerm);
    }

    public List<Aspiration> getAspirations(String searchTerm) {
        return repository.findAll();
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
        Map<String, String> rootCombinations = repository.findMajorRootCombinations();
        Map<String, BigDecimal[]> bonusTotals = bonusRepository.findBonusTotals();
        
        // Load data for best combination calculation
        List<com.tuyensinh.entity.CandidateScore> allScores = new com.tuyensinh.repository.CandidateScoreRepository().findAll();
        List<com.tuyensinh.entity.MajorCombination> allMajorCombinations = new com.tuyensinh.repository.MajorCombinationRepository().findAll();
        List<com.tuyensinh.entity.XtToHopMon> allToHopMon = new com.tuyensinh.repository.ToHopMonRepository().findAll();
        
        Map<String, List<com.tuyensinh.entity.CandidateScore>> scoresByCccd = new HashMap<>();
        for (com.tuyensinh.entity.CandidateScore s : allScores) {
            scoresByCccd.computeIfAbsent(s.getCccd(), k -> new ArrayList<>()).add(s);
        }
        
        Map<String, List<com.tuyensinh.entity.MajorCombination>> majorToCombs = new HashMap<>();
        for (com.tuyensinh.entity.MajorCombination mc : allMajorCombinations) {
            majorToCombs.computeIfAbsent(mc.getMaNganh(), k -> new ArrayList<>()).add(mc);
        }
        
        Map<String, com.tuyensinh.entity.XtToHopMon> toHopMap = new HashMap<>();
        for (com.tuyensinh.entity.XtToHopMon thm : allToHopMon) {
            toHopMap.put(thm.getMatohop(), thm);
        }

        // Load conversions and group by (method + mon)
        List<com.tuyensinh.entity.ScoreConversion> allConversions = new com.tuyensinh.repository.ScoreConversionRepository().findAll();
        Map<String, List<com.tuyensinh.entity.ScoreConversion>> conversionMap = new HashMap<>();
        for (com.tuyensinh.entity.ScoreConversion sc : allConversions) {
            String key = (sc.getPhuongThuc() == null ? "" : sc.getPhuongThuc().toUpperCase()) + "_" 
                       + (sc.getMon() == null ? "" : sc.getMon().toUpperCase());
            conversionMap.computeIfAbsent(key, k -> new ArrayList<>()).add(sc);
        }

        List<Aspiration> eligibleAspirations = new ArrayList<>();

        int belowFloor = 0;
        int missingScore = 0;
        int missingMajorConfig = 0;

        for (Aspiration aspiration : aspirations) {
            // 1. Find best combination and base score (ĐTHXT)
            BestScoreResult best = findBestScore(aspiration, scoresByCccd, majorToCombs, toHopMap, conversionMap);
            if (best != null) {
                BigDecimal dthxt = best.score;
                aspiration.setToHop(best.combination);
                
                // 2. Calculate Root Score (ĐTHGXT)
                // Section 3.2: ĐGNL does not use deviation table
                BigDecimal dthgxt;
                if ("DGNL".equalsIgnoreCase(aspiration.getPhuongThuc())) {
                    dthgxt = dthxt;
                } else {
                    String rootComb = rootCombinations.get(aspiration.getMaNganh());
                    BigDecimal deviation = getDeviation(aspiration.getToHop(), rootComb);
                    dthgxt = dthxt.subtract(deviation);
                }
                aspiration.setDiemThxt(dthgxt); // Store ĐTHGXT in diem_thxt
            }

            // 3. Resolve Bonus (ĐC) and Raw Priority (MĐƯT)
            BigDecimal[] bonusData = resolveBonusDetailed(
                    bonusTotals,
                    aspiration.getCccd(),
                    aspiration.getMaNganh(),
                    aspiration.getToHop(),
                    aspiration.getPhuongThuc());
            
            BigDecimal dc = bonusData[0];
            if (dc.compareTo(BigDecimal.valueOf(3)) > 0) dc = BigDecimal.valueOf(3); // Max 3.0
            
            BigDecimal mdut = bonusData[1];
            
            aspiration.setDiemCc(dc);       // diem_cc = ĐC
            aspiration.setDiemUtxt(mdut);   // diem_utxt = MĐƯT
            aspiration.setDiemCong(dc);     // UI uses diem_cong for "Điểm cộng"

            if (aspiration.getDiemThxt() == null) {
                aspiration.setKetQua("chuaxet");
                missingScore++;
                continue;
            }

            // 4. Calculate Scaled Priority (ĐƯT)
            BigDecimal baseForPriority = aspiration.getDiemThxt().add(dc);
            BigDecimal dut = calculateScaledPriority(baseForPriority, mdut);
            aspiration.setDiemUtqd(dut);    // diem_utqd = ĐƯT

            // 5. Calculate Final Score (ĐXT)
            aspiration.setDiemXetTuyen(baseForPriority.add(dut));

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
        // ... (rest of the code for ranking and saving)


        Comparator<Aspiration> ranking = Comparator
                .comparing(Aspiration::getDiemXetTuyen, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(a -> a.getThuTu() == null ? Integer.MAX_VALUE : a.getThuTu())
                .thenComparing(a -> a.getId() == null ? Integer.MAX_VALUE : a.getId());

        int passed = 0;
        
        // Group aspirations by candidate
        Map<String, List<Aspiration>> candidateAspirations = new HashMap<>();
        for (Aspiration aspiration : eligibleAspirations) {
            candidateAspirations.computeIfAbsent(aspiration.getCccd(), k -> new ArrayList<>()).add(aspiration);
        }

        // Sort each candidate's aspirations by priority (ascending)
        for (List<Aspiration> asps : candidateAspirations.values()) {
            asps.sort(Comparator.comparing(a -> a.getThuTu() == null ? Integer.MAX_VALUE : a.getThuTu()));
        }

        java.util.Queue<String> applicantsToCheck = new java.util.LinkedList<>(candidateAspirations.keySet());
        Map<String, Integer> candidateNextAspIndex = new HashMap<>();
        for (String cccd : candidateAspirations.keySet()) {
            candidateNextAspIndex.put(cccd, 0);
        }

        Map<String, List<Aspiration>> currentAdmitted = new HashMap<>();
        for (String major : quotas.keySet()) {
            currentAdmitted.put(major, new ArrayList<>());
        }

        while (!applicantsToCheck.isEmpty()) {
            String cccd = applicantsToCheck.poll();
            int attemptIdx = candidateNextAspIndex.get(cccd);
            List<Aspiration> asps = candidateAspirations.get(cccd);

            if (attemptIdx >= asps.size()) {
                continue; // out of aspirations
            }

            Aspiration proposing = asps.get(attemptIdx);
            String major = proposing.getMaNganh();

            List<Aspiration> admittedInMajor = currentAdmitted.get(major);
            if (admittedInMajor == null) {
                admittedInMajor = new ArrayList<>();
                currentAdmitted.put(major, admittedInMajor);
            }
            
            admittedInMajor.add(proposing);
            admittedInMajor.sort(ranking);

            int quota = quotas.getOrDefault(major, 0);
            if (admittedInMajor.size() > quota) {
                // Reject the lowest ranked aspiration
                Aspiration rejected = admittedInMajor.remove(admittedInMajor.size() - 1);
                String rejectedCccd = rejected.getCccd();
                candidateNextAspIndex.put(rejectedCccd, candidateNextAspIndex.get(rejectedCccd) + 1);
                applicantsToCheck.add(rejectedCccd);
            }
        }

        int failed = 0;
        for (Aspiration aspiration : eligibleAspirations) {
            aspiration.setKetQua("khongtrungtuyen");
            failed++;
        }
        
        for (List<Aspiration> admitted : currentAdmitted.values()) {
            for (Aspiration aspiration : admitted) {
                aspiration.setKetQua("trungtuyen");
                passed++;
                failed--; // Adjust since we set everyone to failed initially
            }
        }

        if (saveResult) {
            repository.saveAll(aspirations);
            
            for (Map.Entry<String, List<Aspiration>> entry : currentAdmitted.entrySet()) {
                String maNganh = entry.getKey();
                List<Aspiration> admittedList = entry.getValue();
                
                if (!admittedList.isEmpty()) {
                    Aspiration lastAdmitted = admittedList.get(admittedList.size() - 1);
                    BigDecimal diemChuan = lastAdmitted.getDiemXetTuyen();
                    
                    nganhRepository.updateDiemChuan(maNganh, diemChuan);
                } else {

                }
            }
        }
        return new AdmissionResult(aspirations.size(), passed, failed, belowFloor, missingScore, missingMajorConfig);
    }

    private BestScoreResult findBestScore(
            Aspiration aspiration,
            Map<String, List<com.tuyensinh.entity.CandidateScore>> scoresByCccd,
            Map<String, List<com.tuyensinh.entity.MajorCombination>> majorToCombs,
            Map<String, com.tuyensinh.entity.XtToHopMon> toHopMap,
            Map<String, List<com.tuyensinh.entity.ScoreConversion>> conversionMap) {
        
        List<com.tuyensinh.entity.CandidateScore> scores = scoresByCccd.get(aspiration.getCccd());
        if (scores == null) return null;
        
        List<com.tuyensinh.entity.MajorCombination> allowedCombs = majorToCombs.get(aspiration.getMaNganh());
        if (allowedCombs == null) return null;
        
        BigDecimal bestScore = null;
        String bestComb = null;
        
        for (com.tuyensinh.entity.CandidateScore s : scores) {
            String method = s.getDPhuongthuc();
            if (aspiration.getPhuongThuc() != null && !aspiration.getPhuongThuc().isEmpty()) {
                if (!aspiration.getPhuongThuc().equalsIgnoreCase(method)) continue;
            }
            
            for (com.tuyensinh.entity.MajorCombination mc : allowedCombs) {
                com.tuyensinh.entity.XtToHopMon thm = toHopMap.get(mc.getMaToHop());
                if (thm == null) continue;
                
                BigDecimal sum = calculateWeightedSum(s, thm, mc, conversionMap);
                if (sum == null) continue;
                
                BigDecimal converted = sum;
                if ("DGNL".equalsIgnoreCase(method)) {
                    // Formula 3.1: DGNL converted to scale 30 using interpolation
                    List<com.tuyensinh.entity.ScoreConversion> configs = conversionMap.get("DGNL_");
                    converted = interpolateScore(sum, configs);
                }
                
                if (bestScore == null || converted.compareTo(bestScore) > 0) {
                    bestScore = converted.setScale(2, RoundingMode.HALF_UP);
                    bestComb = mc.getMaToHop();
                }
            }
        }
        
        return bestScore == null ? null : new BestScoreResult(bestScore, bestComb);
    }

    private BigDecimal calculateWeightedSum(
            com.tuyensinh.entity.CandidateScore s,
            com.tuyensinh.entity.XtToHopMon thm,
            com.tuyensinh.entity.MajorCombination mc,
            Map<String, List<com.tuyensinh.entity.ScoreConversion>> conversionMap) {
        
        BigDecimal m1 = getSubjectScore(s, thm.getMon1());
        BigDecimal m2 = getSubjectScore(s, thm.getMon2());
        BigDecimal m3 = getSubjectScore(s, thm.getMon3());
        
        if (m1 == null || m2 == null || m3 == null) return null;

        String method = s.getDPhuongthuc();
        if ("VSAT".equalsIgnoreCase(method)) {
            // Formula 3.1: V-SAT subjects converted to scale 10 using interpolation
            m1 = interpolateScore(m1, conversionMap.get("VSAT_" + thm.getMon1().toUpperCase()));
            m2 = interpolateScore(m2, conversionMap.get("VSAT_" + thm.getMon2().toUpperCase()));
            m3 = interpolateScore(m3, conversionMap.get("VSAT_" + thm.getMon3().toUpperCase()));
        }
        
        int w1 = mc.getHsMon1() == null ? 1 : mc.getHsMon1();
        int w2 = mc.getHsMon2() == null ? 1 : mc.getHsMon2();
        int w3 = mc.getHsMon3() == null ? 1 : mc.getHsMon3();
        int totalW = w1 + w2 + w3;
        
        // Formula 3.1: (d1*w1 + d2*w2 + d3*w3) / W * 3
        BigDecimal weightedSum = m1.multiply(BigDecimal.valueOf(w1))
                .add(m2.multiply(BigDecimal.valueOf(w2)))
                .add(m3.multiply(BigDecimal.valueOf(w3)));
        
        return weightedSum.multiply(BigDecimal.valueOf(3))
                .divide(BigDecimal.valueOf(totalW), 5, RoundingMode.HALF_UP);
    }

    private BigDecimal interpolateScore(BigDecimal x, List<com.tuyensinh.entity.ScoreConversion> configs) {
        if (x == null || configs == null || configs.isEmpty()) return x;
        
        for (com.tuyensinh.entity.ScoreConversion sc : configs) {
            if (sc.getDiemA() == null || sc.getDiemB() == null || sc.getDiemC() == null || sc.getDiemD() == null) continue;
            
            BigDecimal a = BigDecimal.valueOf(sc.getDiemA());
            BigDecimal b = BigDecimal.valueOf(sc.getDiemB());
            
            // Check if x falls within [a, b]
            if (x.compareTo(a) >= 0 && x.compareTo(b) <= 0) {
                BigDecimal c = BigDecimal.valueOf(sc.getDiemC());
                BigDecimal d = BigDecimal.valueOf(sc.getDiemD());
                
                if (b.compareTo(a) == 0) return c;
                
                // Linear interpolation: y = c + ((x - a) / (b - a)) * (d - c)
                return c.add(
                    x.subtract(a)
                     .multiply(d.subtract(c))
                     .divide(b.subtract(a), 5, RoundingMode.HALF_UP)
                );
            }
        }
        return x; // Fallback to raw score if no matching range is found
    }

    private BigDecimal getSubjectScore(com.tuyensinh.entity.CandidateScore s, String mon) {
        if (mon == null) return null;
        switch (mon.toUpperCase()) {
            case "TO": return s.getTo();
            case "LI": return s.getLi();
            case "HO": return s.getHo();
            case "SI": return s.getSi();
            case "SU": return s.getSu();
            case "DI": return s.getDi();
            case "VA": return s.getVa();
            case "N1_THI": return s.getN1Thi();
            case "NL1": return s.getNl1();
            default: return null;
        }
    }

    private static class BestScoreResult {
        BigDecimal score;
        String combination;
        BestScoreResult(BigDecimal s, String c) { this.score = s; this.combination = c; }
    }

    private static final Map<String, Map<String, BigDecimal>> DEVIATION_TABLE = new HashMap<>();
    static {
        Map<String, BigDecimal> rowA00 = new HashMap<>();
        rowA00.put("A01", new BigDecimal("-0.69"));
        rowA00.put("B00", new BigDecimal("-1.21"));
        rowA00.put("C00", new BigDecimal("2.32"));
        rowA00.put("C01", new BigDecimal("0.94"));
        rowA00.put("D01", new BigDecimal("-0.68"));
        rowA00.put("D07", new BigDecimal("-1.62"));
        DEVIATION_TABLE.put("A00", rowA00);
        
        Map<String, BigDecimal> rowA01 = new HashMap<>();
        rowA01.put("A00", new BigDecimal("0.69"));
        rowA01.put("B00", new BigDecimal("-0.52"));
        rowA01.put("C00", new BigDecimal("3.01"));
        rowA01.put("C01", new BigDecimal("1.63"));
        rowA01.put("D01", new BigDecimal("0.01"));
        rowA01.put("D07", new BigDecimal("-0.93"));
        DEVIATION_TABLE.put("A01", rowA01);
        
        Map<String, BigDecimal> rowB00 = new HashMap<>();
        rowB00.put("A00", new BigDecimal("1.21"));
        rowB00.put("A01", new BigDecimal("0.52"));
        rowB00.put("C00", new BigDecimal("3.53"));
        rowB00.put("C01", new BigDecimal("2.15"));
        rowB00.put("D01", new BigDecimal("0.53"));
        rowB00.put("D07", new BigDecimal("-0.41"));
        DEVIATION_TABLE.put("B00", rowB00);
        
        Map<String, BigDecimal> rowC00 = new HashMap<>();
        rowC00.put("A00", new BigDecimal("-2.32"));
        rowC00.put("A01", new BigDecimal("-3.01"));
        rowC00.put("B00", new BigDecimal("-3.53"));
        rowC00.put("C01", new BigDecimal("-1.38"));
        rowC00.put("D01", new BigDecimal("-3.00"));
        rowC00.put("D07", new BigDecimal("-3.94"));
        DEVIATION_TABLE.put("C00", rowC00);
        
        Map<String, BigDecimal> rowC01 = new HashMap<>();
        rowC01.put("A00", new BigDecimal("-0.94"));
        rowC01.put("A01", new BigDecimal("-1.63"));
        rowC01.put("B00", new BigDecimal("-2.15"));
        rowC01.put("C00", new BigDecimal("1.38"));
        rowC01.put("D01", new BigDecimal("-1.62"));
        rowC01.put("D07", new BigDecimal("-2.56"));
        DEVIATION_TABLE.put("C01", rowC01);
        
        Map<String, BigDecimal> rowD01 = new HashMap<>();
        rowD01.put("A00", new BigDecimal("0.68"));
        rowD01.put("A01", new BigDecimal("-0.01"));
        rowD01.put("B00", new BigDecimal("-0.53"));
        rowD01.put("C00", new BigDecimal("3.00"));
        rowD01.put("C01", new BigDecimal("1.62"));
        rowD01.put("D07", new BigDecimal("-0.94"));
        DEVIATION_TABLE.put("D01", rowD01);
    }

    private BigDecimal getDeviation(String combination, String rootCombination) {
        if (combination == null || rootCombination == null || combination.equals(rootCombination)) {
            return BigDecimal.ZERO;
        }
        Map<String, BigDecimal> row = DEVIATION_TABLE.get(rootCombination);
        if (row == null) return BigDecimal.ZERO;
        return row.getOrDefault(combination, BigDecimal.ZERO);
    }

    private BigDecimal[] resolveBonusDetailed(
            Map<String, BigDecimal[]> bonusTotals,
            String cccd,
            String maNganh,
            String maToHop,
            String phuongThuc) {
        
        BigDecimal totalDc = BigDecimal.ZERO;
        BigDecimal totalMdut = BigDecimal.ZERO;

        for (Map.Entry<String, BigDecimal[]> entry : bonusTotals.entrySet()) {
            String key = entry.getKey();
            String[] parts = key.split("\\|", -1);
            if (parts.length < 4) continue;
            
            String kCccd = parts[0];
            String kMaNganh = parts[1];
            String kMaToHop = parts[2];
            String kPhuongThuc = parts[3];
            
            if (!kCccd.equalsIgnoreCase(cccd)) continue;
            
            // Match Major: entry major must be empty (applies to all) or match exactly
            if (!kMaNganh.isEmpty() && !kMaNganh.equalsIgnoreCase(maNganh)) continue;
            
            // Match Combination: entry combination must be empty or match exactly
            if (!kMaToHop.isEmpty() && !kMaToHop.equalsIgnoreCase(maToHop)) continue;
            
            // Match Method: entry method must be empty or match exactly
            if (!kPhuongThuc.isEmpty() && !kPhuongThuc.equalsIgnoreCase(phuongThuc)) continue;
            
            totalDc = totalDc.add(entry.getValue()[0]);
            totalMdut = totalMdut.add(entry.getValue()[1]);
        }
        
        return new BigDecimal[]{totalDc, totalMdut};
    }

    private BigDecimal calculateScaledPriority(BigDecimal basePlusBonus, BigDecimal rawPriority) {
        if (basePlusBonus == null || rawPriority == null || rawPriority.signum() <= 0) {
            return BigDecimal.ZERO;
        }

        // Trường hợp 1: (ĐTHGXT + ĐC) < 22,5 điểm
        if (basePlusBonus.compareTo(BigDecimal.valueOf(22.5)) < 0) {
            return rawPriority.setScale(2, RoundingMode.HALF_UP);
        }

        // Trường hợp 2: (ĐTHGXT + ĐC) >= 22,5 điểm
        // ĐƯT = [(30 - (ĐTHGXT + ĐC)) / 7.5] * MĐƯT
        BigDecimal remaining = BigDecimal.valueOf(30).subtract(basePlusBonus);
        if (remaining.signum() <= 0) {
            return BigDecimal.ZERO;
        }

        return remaining
                .multiply(rawPriority)
                .divide(BigDecimal.valueOf(7.5), 5, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void prepare(Aspiration aspiration) {
        aspiration.setCccd(required(aspiration.getCccd(), "CCCD"));
        
        // Populate hoTen from Candidate if missing or to keep it updated
        try {
            Integer candidateId = Integer.parseInt(aspiration.getCccd());
            com.tuyensinh.entity.Candidate candidate = candidateRepository.findById(candidateId);
            if (candidate != null) {
                aspiration.setHoTen(candidate.getHoTen());
            }
        } catch (NumberFormatException e) {
            // If cccd is not an ID, maybe it's a real CCCD? 
            // The system seems to use ID in nn_cccd for aspirations.
        }

        aspiration.setMaNganh(required(aspiration.getMaNganh(), "Ma nganh"));
        if (aspiration.getThuTu() == null || aspiration.getThuTu() <= 0) {
            throw new RuntimeException("Thu tu nguyen vong phai lon hon 0.");
        }
        aspiration.setPhuongThuc(clean(aspiration.getPhuongThuc()));
        aspiration.setToHop(clean(aspiration.getToHop()));
        aspiration.setKetQua(clean(aspiration.getKetQua()));

        aspiration.setDiemCc(zeroIfNull(aspiration.getDiemCc()));
        aspiration.setDiemUtxt(zeroIfNull(aspiration.getDiemUtxt()));
        
        // Re-calculate scores if base values are present
        if (aspiration.getDiemThxt() != null) {
            BigDecimal basePlusBonus = aspiration.getDiemThxt().add(aspiration.getDiemCc());
            aspiration.setDiemUtqd(calculateScaledPriority(basePlusBonus, aspiration.getDiemUtxt()));
            aspiration.setDiemXetTuyen(basePlusBonus.add(aspiration.getDiemUtqd()));
            aspiration.setDiemCong(aspiration.getDiemCc());
        }

        aspiration.setNvKeys(aspiration.getCccd() + "_" + aspiration.getMaNganh() + "_"
                + keyPart(aspiration.getPhuongThuc()) + "_" + keyPart(aspiration.getToHop()));
        if (aspiration.getKetQua() == null) {
            aspiration.setKetQua("chuaxet");
        }
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

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String keyPart(String value) {
        return value == null ? "" : value.trim();
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public List<Object[]> getSuccessfulCandidatesReport() {
        return repository.findAllSuccessfulWithCandidate();
    }

    public List<Object[]> getAdmissionCountByMethodReport() {
        return repository.countSuccessfulByMethodAndMajor();
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
