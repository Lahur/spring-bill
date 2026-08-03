package hr.bill.spring_bill.service;

import feign.FeignException;
import hr.bill.spring_bill.clients.eposlovanje.EposlovanjeClient;
import hr.bill.spring_bill.clients.eposlovanje_util.EposlovanjeUtilClient;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.request.AmsCheckRequest;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.response.AmsCheckResponse;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.response.BusinessEntity;
import hr.bill.spring_bill.dto.web.BusinessCheckResponse;
import hr.bill.spring_bill.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessEntityService {

    private final EposlovanjeClient eposlovanjeClient;

    private final EposlovanjeUtilClient eposlovanjeUtilClient;

    public Optional<BusinessEntity> findByOib(String oib) {
        log.debug("Looking up business entity by OIB {}", oib);
        Optional<BusinessEntity> businessEntityOptional = Optional.empty();
        try {
            businessEntityOptional = Optional.of(eposlovanjeUtilClient.getBusinessEntityByOib(oib));
        }
        catch (FeignException.FeignClientException.NotFound | FeignException.FeignClientException.BadRequest e) {
            log.debug("No business entity found for OIB {}: {}", oib, e.getMessage());
        }
        return businessEntityOptional;
    }

    public AmsCheckResponse checkAmsByOib(String oib) {
        log.debug("Checking AMS status for OIB {}", oib);
        return eposlovanjeClient.amsCheck(AmsCheckRequest.builder()
                        .schema("9934")
                        .identifier(oib)
                .build());
    }

    public BusinessCheckResponse checkByOib(String oib) {
        log.info("Checking business for OIB {}", oib);
        AmsCheckResponse amsCheckResponse = checkAmsByOib(oib);
        BusinessCheckResponse.BusinessCheckResponseBuilder builder = BusinessCheckResponse.builder()
                .amsCheckResponse(amsCheckResponse);
        findByOib(oib).ifPresent(builder::businessEntity);
        return builder.build();
    }
}
