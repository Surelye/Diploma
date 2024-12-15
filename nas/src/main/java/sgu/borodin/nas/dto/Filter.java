package sgu.borodin.nas.dto;

import lombok.experimental.UtilityClass;
import sgu.borodin.nas.enums.CompareOperator;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static java.util.Map.entry;

@UtilityClass
public class Filter {
    private static final Map<String, BiPredicate<FileMetadata, String>> PREDICATE_MAP = Map.ofEntries(
            entry("byNameContains", (metadata, substring) -> metadata.getName().contains(substring)),
            entry("byNameContainsCaseInsensitive", (metadata, substring) ->
                    metadata.getName().toLowerCase().contains(substring.toLowerCase())),
            entry("bySizeGeq", (metadata, greaterValue) -> metadata.getSize() > Long.parseLong(greaterValue)),
            entry("bySizeEq", (metadata, equalValue) -> metadata.getSize() == Long.parseLong(equalValue)),
            entry("bySizeLeq", (metadata, lesserValue) -> metadata.getSize() < Long.parseLong(lesserValue)),
            entry("afterDate", (metadata, afterDate) ->
                    compareZdtAndStringDate(metadata.getCreationTime(), afterDate, CompareOperator.GREATER)),
            entry("onDate", (metadata, onDate) ->
                    compareZdtAndStringDate(metadata.getCreationTime(), onDate, CompareOperator.EQUAL)),
            entry("beforeDate", (metadata, beforeDate) ->
                    compareZdtAndStringDate(metadata.getCreationTime(), beforeDate, CompareOperator.LESSER))
    );

    public static Predicate<FileMetadata> getPredicate(String filterName, String filterValue) {
        var biPredicate = PREDICATE_MAP.get(filterName);
        return Objects.isNull(biPredicate)
                ? null
                : metadata -> biPredicate.test(metadata, filterValue);
    }

    private boolean compareZdtAndStringDate(ZonedDateTime zdt, String strDate, CompareOperator compareOperator) {
        LocalDate creationDate = zdt.toLocalDate();
        LocalDate dateToCheck = LocalDate.parse(strDate, DateTimeFormatter.ISO_DATE);

        return switch (compareOperator) {
            case GREATER -> creationDate.isAfter(dateToCheck);
            case EQUAL -> creationDate.isEqual(dateToCheck);
            case LESSER -> creationDate.isBefore(dateToCheck);
        };
    }
}
