package hr.bill.spring_bill.dao;

import hr.bill.spring_bill.model.RecipientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecipientRepository extends JpaRepository<RecipientEntity, UUID> {

    Optional<RecipientEntity> findByEmail(String email);

    List<RecipientEntity> findAllByOrderByEmail();
}
