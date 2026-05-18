package com.tuyensinh.service;

import com.tuyensinh.entity.MajorCombination;
import com.tuyensinh.repository.MajorCombinationRepository;
import java.util.List;

public class MajorCombinationService {
    private MajorCombinationRepository repository;

    public MajorCombinationService() {
        this.repository = new MajorCombinationRepository();
    }

    public List<MajorCombination> getAll() {
        return repository.findAll();
    }

    public List<MajorCombination> search(String term) {
        return repository.search(term);
    }

    public void save(MajorCombination mc) {
        // Tự động gen tb_keys: manganh_matohop (VD: 7140114_B03)
        if (mc.getMaNganh() != null && mc.getMaToHop() != null) {
            mc.setTbKeys(mc.getMaNganh() + "_" + mc.getMaToHop());
        }
        repository.saveOrUpdate(mc);
    }

    public void delete(int id) {
        repository.delete(id);
    }
}