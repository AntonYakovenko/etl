package com.globallogic.test.etl.tsv

import com.globallogic.test.etl.db.DatabaseInitializer
import com.globallogic.test.etl.db.MongoTsvItemRepository
import com.globallogic.test.etl.db.TsvItemRepository
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDate

@Subject(MongoTsvItemRepository)
class TsvItemRepositoryTest extends Specification {

    TsvItemRepository repository
    TsvItem item

    def setup() {
        def db = DatabaseInitializer.initDb()
        repository = new MongoTsvItemRepository(db)
        item = new TsvItem(UUID.randomUUID().toString(), "name", 10, LocalDate.now())
    }

    def cleanup() {
        repository.delete(item.id)
    }

    def "item saved"() {
        when:
        repository.save(item)
        then:
        def dbItem = repository.find(item.id).get()
        item == dbItem
    }
}
