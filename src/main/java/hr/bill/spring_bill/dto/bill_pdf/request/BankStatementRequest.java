package hr.bill.spring_bill.dto.bill_pdf.request;

import hr.bill.spring_bill.dto.bill_pdf.common.BankStatementLineDto;
import lombok.Builder;

import java.util.List;

@Builder
public record BankStatementRequest(
        String bankName,
        String bankAddress,
        String bankOib,
        String bankBic,
        String statementNumber,
        String statementDate,
        String accountIban,
        String accountName,
        String currency,
        String ownerName,
        String ownerAddress,
        String ownerOib,
        String periodFrom,
        String periodTo,
        String openingBalance,
        String closingBalance,
        String creditCount,
        String creditSum,
        String debitCount,
        String debitSum,
        List<BankStatementLineDto> transactions
) {}
