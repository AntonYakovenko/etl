package com.globallogic.test.etl

import spock.lang.Specification

class TestApp extends Specification {

    def "simple test"() {
        given:
        int a = 2
        int b = 3
        when:
        int c = a + b
        then:
        c == 5
    }
}
