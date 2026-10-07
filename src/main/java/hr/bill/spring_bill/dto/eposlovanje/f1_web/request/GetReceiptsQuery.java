package hr.bill.spring_bill.dto.eposlovanje.f1_web.request;

import hr.bill.spring_bill.dto.eposlovanje.f1_web.common.FiscalStatus;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.common.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetReceiptsQuery {
    private String dateFrom;
    private String dateTo;
    private FiscalStatus fiscalStatus;
    private PaymentMethod paymentMethod;
    private Integer cashRegisterId;
    private String searchTerm;
    private Integer page;
    private Integer pageSize;
    private String sortBy;
    private Boolean sortDescending;
}
