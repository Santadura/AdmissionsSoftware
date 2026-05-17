package com.tuyensinh.service;

import com.tuyensinh.entity.ScoreConversion;
import com.tuyensinh.repository.ScoreConversionRepository;

import java.util.List;

public class ScoreConversionService {
    private ScoreConversionRepository repository;

    public ScoreConversionService() {
        this.repository = new ScoreConversionRepository();
    }

    public List<ScoreConversion> getAll() {
        return repository.findAll();
    }

    public List<ScoreConversion> search(String term) {
        return repository.search(term);
    }

    public void save(ScoreConversion sc) {
        repository.saveOrUpdate(sc);
    }

    public void delete(int id) {
        repository.delete(id);
    }
}