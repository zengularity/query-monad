package com.zengularity.querymonad.test.core.module.sql

import acolyte.jdbc.RowLists.rowList3
import org.specs2.mutable.Specification

// TODO: 2 methods: commitWithTransaction & commitWithoutTransaction

object SqlQueryRunnerSpec extends Specification {

  val userSchema = rowList3(
    classOf[String] -> "id",
    classOf[String] -> "name",
    classOf[Int]    -> "age"
  )

  "SqlQueryRunner" should {
    // execute single query


    // execute composed queries into a single transaction


  }
}
