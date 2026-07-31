package hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.response;

public record FiscalizeDocumentResponseDTO(
        Boolean success,
        String jir,
        String zki,
        String qrCodeBase64,
        String rawRequest,
        String rawResponse,
        String errorMessage
) {}
