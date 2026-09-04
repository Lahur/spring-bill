package hr.bill.spring_bill.service.document;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public record RemoteMatchResult(int matchedCount, Set<LocalDate> updatedMonths) {

    public static RemoteMatchResult empty() {
        return new RemoteMatchResult(0, Set.of());
    }

    public RemoteMatchResult add(RemoteMatchResult other) {
        if (other.matchedCount() == 0 && other.updatedMonths().isEmpty()) {
            return this;
        }
        Set<LocalDate> merged = new HashSet<>(updatedMonths);
        merged.addAll(other.updatedMonths());
        return new RemoteMatchResult(matchedCount + other.matchedCount(), merged);
    }
}
