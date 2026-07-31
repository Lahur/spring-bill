package hr.bill.spring_bill.service.document;

import lombok.Builder;

@Builder
public record BillDocument(String filename, byte[] content) {}