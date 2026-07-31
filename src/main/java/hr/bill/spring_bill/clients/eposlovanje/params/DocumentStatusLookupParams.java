package hr.bill.spring_bill.clients.eposlovanje.params;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentStatusLookupParams {
    private String documentId;
    private String supplierPartyVATId;
    private String customerPartyVATId;
    private Integer issueYear;
}
