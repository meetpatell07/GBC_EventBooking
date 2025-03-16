package ca.gbc.approvalservice.repository;

import ca.gbc.approvalservice.model.Approval;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApprovalRepository extends MongoRepository<Approval, String> {
    Optional<Approval> findByEventId(String eventId);
}
