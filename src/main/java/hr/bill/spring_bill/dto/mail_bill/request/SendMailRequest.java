package hr.bill.spring_bill.dto.mail_bill.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record SendMailRequest(
        String subject,
        List<MailFile> files,
        @JsonProperty("recipient_email") String recipientEmail
) {}
