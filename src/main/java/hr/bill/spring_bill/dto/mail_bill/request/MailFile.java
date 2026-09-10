package hr.bill.spring_bill.dto.mail_bill.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record MailFile(
        @JsonProperty("file_content") String fileContent,
        @JsonProperty("file_name") String fileName
) {}
