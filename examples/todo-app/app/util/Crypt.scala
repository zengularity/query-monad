package com.zengularity.querymonad.examples.todoapp.util

import org.mindrot.jbcrypt.BCrypt

object Crypt {

  def hashPassword(clearPassword: String): String = {
    val workload = 12
    val salt: String = BCrypt.gensalt(workload)
    val hashedPassword: String = BCrypt.hashpw(clearPassword, salt)

    hashedPassword
  }

  def checkPassword(clearPassword: String)(storedHash: String): Boolean = {
    if (storedHash.startsWith("$2a$"))
      BCrypt.checkpw(clearPassword, storedHash)
    else
      false
  }

}
