package hr.bill.spring_bill.dto.web.bill.info;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Bill fiscalization info (B2C only)")
public record FiscalInfo(

        @Schema(description = "ZKI (Zaštitni Kod Izdavatelja - Security Code of Issuer)", example = "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4")
        String zki,

        @Schema(description = "JIR (Jedinstveni Identifikator Računa - Unique Receipt Identifier)", example = "550e8400-e29b-41d4-a716-446655440000")
        String jir,

        @Schema(description = "Date and time when the receipt was fiscalized", example = "2026-06-26T11:00:00")
        LocalDateTime fiscalizedAt
) {
}