package hr.bill.spring_bill.dto.eposlovanje.eposlovanje.response;

import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.common.BankingTransactionType;

public record BankingTransaction(
        String id,
        String insertedOn,
        String iban,
        String bookedOn,
        BankingTransactionType transactionType,
        Double amount,
        String currency,
        Double exchangeRate,
        String sourceCurrency,
        String creditorName,
        String creditorAccount,
        String ultimateCreditor,
        String debtorName,
        String debtorAccount,
        String ultimateDebtor,
        String remittanceInformationUnstructured,
        String remittanceInformationStructured,
        String additionalInformation,
        String purposeCode,
        String bankTransactionCode,
        String propriatyBankTransactionCode,
        String bankTransactionId
) {}
