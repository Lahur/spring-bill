package hr.bill.spring_bill.clients.eposlovanje.params;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankingTransactionParams {
    private String iban;
    private String currency;
    private String insertedFrom;
    private String insertedTo;
    private String bookingFrom;
    private String bookingTo;
}
