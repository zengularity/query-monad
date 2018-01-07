package database

case class QueryBuilder[DB <: Database](db: DD, ec: ExecutionContext) {
  def pure[A](a: => A): Query[DB, A] = apply(_ => a)
  
  def apply[A](atomic: Connection => A): Query[DD, A] = {
    val underlying = for {
      c <- Reader.ask[Connection]
    } yield atomic(c)
    Query(underlying)
  }
}
