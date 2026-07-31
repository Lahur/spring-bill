package hr.bill.spring_bill.dto.eposlovanje.eposlovanje.response;

import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.common.EReportingRequestItem;

import java.util.List;

public record EReportingRequestsResponse(List<EReportingRequestItem> requests) {}
