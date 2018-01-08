package object database {

  import java.sql.Connection

  import utils.{Reader, ReaderT}

  type Query[A] = Reader[Connection, A]

  object Query {
    def pure[A] = Reader.pure[Connection, A] _

    val ask = Reader.ask[Connection]

    def apply[A](f: Connection => A) = new Query(f)
  }

  type QueryT[F[_], A] = ReaderT[F, Connection, A]

  type QueryO[A] = QueryT[Option, A]

  type QueryE[A, Err] = QueryT[Either[Err, ?], A]

}
