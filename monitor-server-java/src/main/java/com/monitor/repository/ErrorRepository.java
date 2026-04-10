package com.monitor.repository;

import com.monitor.model.ErrorDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorRepository extends MongoRepository<ErrorDocument, String> {
}
