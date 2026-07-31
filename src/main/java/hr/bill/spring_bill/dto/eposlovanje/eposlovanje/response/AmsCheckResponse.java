package hr.bill.spring_bill.dto.eposlovanje.eposlovanje.response;

public record AmsCheckResponse(
        String schema,
        String identifier,
        Boolean publishedOnAms,
        String mpsEndpoint
) {}
