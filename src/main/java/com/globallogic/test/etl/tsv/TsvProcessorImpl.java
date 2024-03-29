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

/**
 * ETL implementation
 */
public class TsvProcessorImpl implements TsvProcessor {
    private static final String TSV_SEPARATOR = "\t";
    private static final String ID = "ID";
    private static final String NAME = "NAME";
    private static final String QUANTITY = "QUANTITY";
    private static final String DATE_CREATED = "DATE_CREATED";

    // package private access for tests
    final List<String> headers = new ArrayList<>(); // needed for set up headers order
    final Map<String, BiPredicate<String, String>> validators = new HashMap<>();
    final Map<String, BiConsumer<String, TsvItem>> setters = new HashMap<>();
    final ExecutorService executor = Executors.newFixedThreadPool(5);
    final TsvItemRepository repository;

    public TsvProcessorImpl(TsvItemRepository repository) {
        initValidators();
        initSetters();
        this.repository = repository;
    }

    private void initValidators() {
        validators.put(ID, (field, line) -> true); // no validation
        validators.put(NAME, (field, line) -> {
            if (field.length() > 50) {
                System.err.printf("WARN: \"%s\" must be less than 50. Skipping the line: %s\n", NAME, line);
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
                System.err.printf("WARN: \"%s\" must be a digit. Skipping the line: %s\n", QUANTITY, line);
                return false;
            }
        });
        validators.put(DATE_CREATED, (field, line) -> {
            try {
                LocalDate.parse(field);
                return true;
            } catch (DateTimeParseException e) {
                System.err.printf("WARN: \"%s\" must be in format \"yyyy-MM-dd\". Skipping the line: %s\n",
                        DATE_CREATED, line);
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
        migrate(path, item -> true);
    }

    @Override
    public void migrate(String path, Predicate<? super TsvItem> predicate) {
        validateFilePath(path);
        migrate(Paths.get(path), predicate);
    }

    @Override
    public void migrate(URI uri) {
        migrate(uri, item -> true);
    }

    @Override
    public void migrate(URI uri, Predicate<? super TsvItem> predicate) {
        migrate(Paths.get(uri), predicate);
    }

    void validateFilePath(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Null path");
        }
        if (!path.endsWith(".tsv") && !path.endsWith(".tab")) {
            throw new IllegalArgumentException("Only .tsv and .tab extensions allowed");
        }
    }

    /**
     * Main method for ETL. Using methods {@link TsvProcessorImpl#processHeaders(Path)} and
     * {@link TsvProcessorImpl#processLine(String, Predicate)} provides data migration from the file
     * to the database. Invalid lines of .tsv file are skipped
     *
     * @param path      the path to source file
     * @param predicate the predicate to filter items to save to the database
     */
    void migrate(Path path, Predicate<? super TsvItem> predicate) {
        processHeaders(path);
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

    /**
     * Reads and validates headers from .tsv file and sets up headers order in the field <tt>headers</tt>
     *
     * @param path the path to source file
     * @throws TsvValidationException if headers or their count are invalid
     */
    void processHeaders(Path path) {
        final Set<String> allowedHeaders = new HashSet<>(Arrays.asList(ID, NAME, QUANTITY, DATE_CREATED));
        try (Stream<String> lines = Files.lines(path, Charset.defaultCharset()).limit(1)) {
            final List<String> tsvHeaders = lines.flatMap(line -> Arrays.stream(line.split(TSV_SEPARATOR)))
                    .collect(Collectors.toList());

            if (tsvHeaders.size() != allowedHeaders.size()) {
                throw new TsvValidationException(String.format("ERROR: Expected %d headers but found: %d",
                        allowedHeaders.size(), tsvHeaders.size()));
            }

            tsvHeaders.forEach(header -> {
                if (!allowedHeaders.remove(header)) {
                    if (headers.contains(header)) {
                        throw new TsvValidationException(String.format("ERROR: Duplicate header: %s", header));
                    }
                    throw new TsvValidationException(String.format("ERROR: Header not allowed: %s", header));
                }
                headers.add(header);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads and validate the line of data from .tsv file. If the line is valid and matches predicate -
     * saves it to the database, else skips
     *
     * @param line      the line of the file to process
     * @param predicate the predicate to filter items to save to the database
     */
    void processLine(String line, Predicate<? super TsvItem> predicate) {
        final String[] fields = line.split(TSV_SEPARATOR);
        if (fields.length != headers.size()) {
            System.err.printf("WARN: Expected %d items in the line but found %d. Skipping the line: %s",
                    headers.size(), fields.length, line);
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
            executor.submit(() -> repository.save(item)); // speeds up bulk file processing
        }
    }
}
