package com.monitor.repository;

import com.monitor.model.BehaviorDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BehaviorRepository extends MongoRepository<BehaviorDocument, String> {
}
