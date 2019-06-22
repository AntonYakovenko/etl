package com.globallogic.test.etl.tsv;

import com.globallogic.test.etl.db.TsvItemRepository;

import java.io.IOException;
import java.net.URI;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TsvProcessorImpl implements TsvProcessor {
    private static final String TSV_SEPARATOR = "\t";
    private static final String ID = "ID";
    private static final String NAME = "NAME";
    private static final String QUANTITY = "QUANTITY";
    private static final String DATE_CREATED = "DATE_CREATED";

    private final List<String> headers = new ArrayList<>(); // needed for set up headers order
    private final Map<String, BiPredicate<String, String>> validators = new HashMap<>();
    private final Map<String, BiConsumer<String, TsvItem>> setters = new HashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private final TsvItemRepository repository;

    public TsvProcessorImpl(TsvItemRepository repository) {
        initValidators();
        initSetters();
        this.repository = repository;
    }

    private void initValidators() {
        validators.put(ID, (field, line) -> true); // no validation
        validators.put(NAME, (field, line) -> {
            if (field.length() > 50) {
                System.err.printf("[WARN]: \"%s\" must be less than 50 in line: %s\n", NAME, line);
                return false;
            } else {
                return true;
            }
        });
        validators.put(QUANTITY, (field, line) -> {
            try {
                Integer.parseInt(field);
                return true;
            } catch (NumberFormatException e) {
                System.err.printf("[WARN]: \"%s\" must be a digit in line: %s\n", QUANTITY, line);
                return false;
            }
        });
        validators.put(DATE_CREATED, (field, line) -> {
            try {
                LocalDate.parse(field);
                return true;
            } catch (DateTimeParseException e) {
                System.err.printf("[WARN]: \"%s\" must be in format \"yyyy-MM-dd\" in line: %s\n", DATE_CREATED, line);
                return false;
            }
        });
    }

    private void initSetters() {
        setters.put(ID, (value, item) -> item.setId(value));
        setters.put(NAME, (value, item) -> item.setName(value));
        setters.put(QUANTITY, (value, item) -> item.setQuantity(Integer.valueOf(value)));
        setters.put(DATE_CREATED, (value, item) -> item.setDateCreated(LocalDate.parse(value)));
    }

    @Override
    public void migrate(String path) {
        migrate(Paths.get(path), item -> true);
    }

    @Override
    public void migrate(String path, Predicate<? super TsvItem> predicate) {
        migrate(Paths.get(path), predicate);
    }

    @Override
    public void migrate(URI uri) {
        migrate(Paths.get(uri), item -> true);
    }

    @Override
    public void migrate(URI uri, Predicate<? super TsvItem> predicate) {
        migrate(Paths.get(uri), predicate);
    }

    private void migrate(Path path, Predicate<? super TsvItem> predicate) {
        validateHeaders(path);
        try (Stream<String> lines = Files.lines(path, Charset.defaultCharset()).skip(1)) {
            lines.forEach(line -> processLine(line, predicate));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
            try {
                executor.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void validateHeaders(Path path) {
        final Set<String> allowedHeaders = new HashSet<>(Arrays.asList(ID, NAME, QUANTITY, DATE_CREATED));
        try (Stream<String> lines = Files.lines(path, Charset.defaultCharset()).limit(1)) {
            final List<String> tsvHeaders = lines.flatMap(line -> Arrays.stream(line.split(TSV_SEPARATOR)))
                    .collect(Collectors.toList());

            if (tsvHeaders.size() != allowedHeaders.size()) {
                throw new TsvValidationException(String.format("[ERROR]: Expected %d headers but found: %d",
                        allowedHeaders.size(), tsvHeaders.size()));
            }

            tsvHeaders.forEach(header -> {
                if (!allowedHeaders.remove(header)) {
                    if (headers.contains(header)) {
                        throw new TsvValidationException(String.format("[ERROR]: Duplicate header: %s", header));
                    }
                    throw new TsvValidationException(String.format("[ERROR]: Header not allowed: %s", header));
                }
                headers.add(header);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processLine(String line, Predicate<? super TsvItem> predicate) {
        final String[] fields = line.split(TSV_SEPARATOR);
        if (fields.length != headers.size()) {
            throw new TsvValidationException(String.format("[ERROR]: Expected %d items but found %d. Line: %s",
                    headers.size(), fields.length, line));
        }

        int validFields = 0;
        final TsvItem item = new TsvItem();
        for (int i = 0; i < headers.size(); i++) {
            final String currHeader = headers.get(i);
            boolean isValid = validators.get(currHeader).test(fields[i], line); // validate field
            if (isValid) {
                validFields++;
                setters.get(currHeader).accept(fields[i], item); // set value to field
            }
        }

        if (validFields == headers.size() && predicate.test(item)) {
            executor.submit(() -> repository.save(item));
        }
    }
}
