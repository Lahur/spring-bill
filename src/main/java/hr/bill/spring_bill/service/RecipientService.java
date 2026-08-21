package hr.bill.spring_bill.service;

import hr.bill.spring_bill.dao.RecipientRepository;
import hr.bill.spring_bill.model.RecipientEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipientService {

    private final RecipientRepository recipientRepository;

    public void save(String email) {
        log.debug("Saving recipient email {}", email);
        if (recipientRepository.findByEmail(email).isPresent()) {
            log.debug("Recipient email {} already stored", email);
            return;
        }
        recipientRepository.save(RecipientEntity.builder().email(email).build());
        log.debug("Saved recipient email {}", email);
    }

    public List<RecipientEntity> findAll() {
        log.debug("Fetching all recipient emails");
        List<RecipientEntity> result = recipientRepository.findAllByOrderByEmail();
        log.debug("Found {} recipient emails", result.size());
        return result;
    }
}
