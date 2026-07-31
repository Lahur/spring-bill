package hr.bill.spring_bill.service;

public class HrPaymentReferenceService {

    public static String extractHrModel(String paymentId) {
        String noSpaces = paymentId.replace(" ", "");
        if (noSpaces.startsWith("HR")) {
            String afterHr = noSpaces.substring(2);
            String digits = afterHr.chars()
                    .limit(2)
                    .filter(Character::isDigit)
                    .collect(StringBuilder::new, (sb, c) -> sb.append((char) c), StringBuilder::append)
                    .toString();
            return digits.length() == 2 ? "HR" + digits : "HR00";
        }
        return "HR00";
    }

    public static String trimHrPrefix(String paymentId) {
        String noSpaces = paymentId.replace(" ", "");
        String afterHr = noSpaces.startsWith("HR") ? noSpaces.substring(2) : noSpaces;
        String afterModel = afterHr.length() >= 2 && afterHr.substring(0, 2).chars().allMatch(Character::isDigit)
                ? afterHr.substring(2)
                : afterHr;
        int start = 0;
        while (start < afterModel.length() && afterModel.charAt(start) == '-') start++;
        return afterModel.substring(start);
    }

    public static String buildReference(String fullBillId) {
        return "HR00" + fullBillId.replace("/", "-");
    }

    public static String fullReference(String paymentId) {
        return extractHrModel(paymentId) + trimHrPrefix(paymentId);
    }

    public static boolean matches(String reference, String expectedReference) {
        if (reference == null || expectedReference == null) return false;
        return expectedReference.equalsIgnoreCase(reference.replace(" ", ""));
    }

    public static boolean referencesMatch(String reference, String fullBillId) {
        if (fullBillId == null) return false;
        return matches(reference, buildReference(fullBillId));
    }
}