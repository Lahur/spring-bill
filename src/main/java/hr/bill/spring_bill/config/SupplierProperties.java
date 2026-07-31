package hr.bill.spring_bill.config;

import hr.bill.spring_bill.validation.ValidOib;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "bill.supplier")
public record SupplierProperties(
        String oib,
        String name,
        String street,
        String city,
        String postalZone,
        String countryCode,
        String contactOib,
        String contactName,
        String phone,
        String email,
        String iban
) {
}