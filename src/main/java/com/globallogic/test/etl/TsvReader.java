package com.globallogic.test.etl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TsvReader { // TODO: take out headers to constants
    private static final Path PATH = Paths.get("src\\main\\resources\\etl_0.tsv");
    private static final String TSV_SEPARATOR = "\t";
    private static List<String> headers = new ArrayList<>(); // needed for set up headers order
    private static final Map<String, BiPredicate<String, String>> validators = new HashMap<>();
    private static final Map<String, BiConsumer<String, TsvItem>> setters = new HashMap<>();

    public TsvReader() {
        initValidators();
        initSetters();
    }

    private void initValidators() {
        validators.put("ID", (field, line) -> true); // no validation
        validators.put("NAME", (field, line) -> {
            if (field.length() > 50) {
                System.err.printf("[WARN]: \"NAME\" must be less than 50 in line: %s\n", line);
                return false;
            } else {
                return true;
            }
        });
        validators.put("QUANTITY", (field, line) -> {
            try {
                Integer.parseInt(field);
                return true;
            } catch (NumberFormatException e) {
                System.err.printf("[WARN]: \"QUANTITY\" must be a digit in line: %s\n", line);
                return false;
            }
        });
        validators.put("DATE_CREATED", (field, line) -> {
            try {
                LocalDate.parse(field);
                return true;
            } catch (DateTimeParseException e) {
                System.err.printf("[WARN]: \"DATE_CREATED\" must be in format \"yyyy-MM-dd\" in line: %s\n", line);
                return false;
            }
        });
    }

    private void initSetters() {
        setters.put("ID", (value, item) -> item.setId(value));
        setters.put("NAME", (value, item) -> item.setName(value));
        setters.put("QUANTITY", (value, item) -> item.setQuantity(Integer.valueOf(value)));
        setters.put("DATE_CREATED", (value, item) -> item.setDateCreated(LocalDate.parse(value)));
    }

    void validateHeaders() {
        final Set<String> allowedHeaders = new HashSet<>(Arrays.asList("ID", "NAME", "QUANTITY", "DATE_CREATED"));
        try (Stream<String> lines = Files.lines(PATH, Charset.defaultCharset()).limit(1)) {
            List<String> headers = lines.flatMap(line -> Arrays.stream(line.split(TSV_SEPARATOR)))
                    .collect(Collectors.toList());

            if (headers.size() != allowedHeaders.size()) {
                throw new TsvValidationException(String.format("Expected %d headers but found: %d",
                        allowedHeaders.size(), headers.size()));
            }

            headers.forEach(header -> {
                if (!allowedHeaders.remove(header)) {
                    if (TsvReader.headers.contains(header)) {
                        throw new TsvValidationException(String.format("Duplicate header: %s", header));
                    }
                    throw new TsvValidationException(String.format("Header not allowed: %s", header));
                }
                TsvReader.headers.add(header);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void validateData() {
        List<TsvItem> items = new ArrayList<>();
        try (Stream<String> lines = Files.lines(PATH, Charset.defaultCharset()).skip(1)) {
            lines.forEach(line -> processLine(line, items));
        } catch (IOException e) {
            e.printStackTrace();
        }
        items.forEach(item -> System.out.printf("%s\n", item));
    }

    private void processLine(String line, List<TsvItem> items) {
        String[] fields = line.split(TSV_SEPARATOR);
        if (fields.length != headers.size()) {
            throw new TsvValidationException(String.format("Expected %d items but found %d. Line: %s",
                    headers.size(), fields.length, line));
        }

        int validFields = 0;
        TsvItem item = new TsvItem();
        for (int i = 0; i < headers.size(); i++) {
            final String currHeader = headers.get(i);
            boolean isValid = validators.get(currHeader).test(fields[i], line); // validate field
            if (isValid) {
                validFields++;
                setters.get(currHeader).accept(fields[i], item); // set value to field
            }
        }

        if (validFields == headers.size()) {
            items.add(item);
        }
    }
}
