package hr.bill.spring_bill.clients.eposlovanje.params;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EReportingRequestsParams {
    private String documentId;
    private Integer issueYear;
    private String supplierPartyVATId;
    private String customerPartyVATId;
    /** Integer value of EReportingRequestType (1–5) — serialized as query param "type" */
    private Integer type;
}
