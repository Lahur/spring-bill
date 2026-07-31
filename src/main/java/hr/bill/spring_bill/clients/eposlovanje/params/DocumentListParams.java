package hr.bill.spring_bill.clients.eposlovanje.params;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentListParams {
    private String insertedFrom;
    private String insertedTo;
    private String modifiedFrom;
    private String modifiedTo;
    private String issuedFrom;
    private String issuedTo;
    /** 1–9, maps to DocumentStatus value */
    private Integer status;
    private Integer limit;
    private Integer offset;
}
