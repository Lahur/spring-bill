package hr.bill.spring_bill.dto.web;

import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.response.AmsCheckResponse;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.response.BusinessEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Response with business entity validity and info data")
public record BusinessCheckResponse(

        @Schema(description = "Represents business entity validity in tax scope")
        AmsCheckResponse amsCheckResponse,

        @Schema(description = "Business entity info")
        BusinessEntity businessEntity
) {
}
