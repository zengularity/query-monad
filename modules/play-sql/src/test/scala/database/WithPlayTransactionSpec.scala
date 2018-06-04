package com.zengularity.querymonad.test.module.playsql.database

import play.api.db.Databases
import org.specs2.mutable.Specification

import com.zengularity.querymonad.test.module.sql.utils.SqlConnectionFactory

class WithPlayTransactionSpec extends Specification {

  "WithPlayTransaction" should {

    // FIXME: 
    "execute an (select 1) statement" in {
      val database = Databases.inMemory()
    }

  }

}
