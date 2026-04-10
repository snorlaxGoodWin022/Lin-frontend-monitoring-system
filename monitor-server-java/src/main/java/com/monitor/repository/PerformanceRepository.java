package com.monitor.repository;

import com.monitor.model.PerformanceDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerformanceRepository extends MongoRepository<PerformanceDocument, String> {
}
