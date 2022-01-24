package com.ssmmhh.jibam.utils.repeatTests

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement


class RepeatRule : TestRule {

    override fun apply(statement: Statement, description: Description): Statement =
        description.getAnnotation(Repeat::class.java)?.let {
            RepeatStatement(statement, it.times)
        } ?: statement


    private class RepeatStatement(
        private val statement: Statement,
        private val times: Int
    ) : Statement() {
        @Throws(Throwable::class)
        override fun evaluate() {
            repeat(times) {
                statement.evaluate()
            }
        }
    }

}