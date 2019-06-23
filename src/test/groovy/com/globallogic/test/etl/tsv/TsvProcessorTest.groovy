package com.globallogic.test.etl.tsv

import com.globallogic.test.etl.db.TsvItemRepository
import spock.lang.Specification
import spock.lang.Subject

import java.nio.file.Path
import java.nio.file.Paths

@Subject(TsvProcessorImpl)
class TsvProcessorTest extends Specification {

    TsvItemRepository repository
    TsvProcessorImpl processor

    def setup() {
        repository = Mock(TsvItemRepository)
        processor = new TsvProcessorImpl(repository)
    }

    def "invalid file extension"() {
        given:
        String path = "src\\test\\resources\\extension_invalid.txt"
        when:
        processor.validateFilePath(path)
        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Only .tsv and .tab extensions allowed"
    }

    def "headers valid"() {
        given:
        Path path = Paths.get("src\\test\\resources\\headers_valid.tsv")
        when:
        processor.processHeaders(path)
        then:
        noExceptionThrown()
        processor.headers.size() == 4
    }

    def "invalid headers count"() {
        given:
        Path path = Paths.get("src\\test\\resources\\headers_count_invalid.tsv")
        when:
        processor.processHeaders(path)
        then:
        def ex = thrown(TsvValidationException)
        ex.message.matches("ERROR: Expected 4 headers but found: \\d+")
    }

    def "invalid headers name"() {
        given:
        Path path = Paths.get("src\\test\\resources\\headers_name_invalid.tsv")
        when:
        processor.processHeaders(path)
        then:
        def ex = thrown(TsvValidationException)
        ex.message.matches("ERROR: Header not allowed: .*")
    }

    def "duplicate headers"() {
        given:
        Path path = Paths.get("src\\test\\resources\\headers_duplicated.tsv")
        when:
        processor.processHeaders(path)
        then:
        def ex = thrown(TsvValidationException)
        ex.message.matches("ERROR: Duplicate header: .*")
    }

    def "line valid"() {
        given:
        processor.headers.addAll(["ID", "NAME", "QUANTITY", "DATE_CREATED"])
        String line = "1\titem_1\t1\t2019-06-23"
        when:
        processor.processLine(line, { true })
        then:
        noExceptionThrown()
    }
}
