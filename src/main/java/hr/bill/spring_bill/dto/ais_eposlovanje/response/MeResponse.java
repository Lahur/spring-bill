package hr.bill.spring_bill.dto.ais_eposlovanje.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Company, balance and requisition counts. Amounts are net EUR. */
public record MeResponse(
        String company,
        String oib,
        @JsonProperty("saldo_net") Double saldoNet,
        @JsonProperty("reserved_net") Double reservedNet,
        @JsonProperty("available_net") Double availableNet,
        @JsonProperty("active_requisitions") Integer activeRequisitions,
        @JsonProperty("pending_requisitions") Integer pendingRequisitions
) {}
