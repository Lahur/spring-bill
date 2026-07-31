package hr.bill.spring_bill.dto.eposlovanje.eposlovanje.request;

import lombok.Builder;

@Builder
public record AccountRegistrationRequest(
        String companyName,
        String vatId,
        String iban,
        String referenceId,
        String businessUnit,
        String contactFirstname,
        String contactLastname,
        String contactPhone,
        String contactGsm,
        String contactEmail,
        String address,
        String city,
        String zip,
        String country,
        String username,
        String password,
        String softwareId,
        String plan
) {}
