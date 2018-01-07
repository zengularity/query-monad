package object database {

  type QueryO[DB, A] = QueryT[Option, DB, A]

  type QueryE[DB, A, Err] = QueryT[Either[Err, ?], DB, A]

}
