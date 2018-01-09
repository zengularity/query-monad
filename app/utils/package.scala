package object utils {

  /**
   * The identity type constructor
   */
  type Id[A] = A

  /**
   * This type alias is equivalent to this code:
   * {{{
   *   case class Reader[-A, +B](f: A => B) {
   *     def map[C](g: B => C): Reader[A, C] = Reader(g compose f)
   *
   *     def flatMap[C](g: B => Reader[A, C]): Reader[A, C] = Reader(a => g(f(a)).f(a))
   *   }
   *
   *   object Reader {
   *     def pure[A, B](b: B): Reader[A, B] = Reader(_ => b)
   *
   *     def ask[A]: Reader[A, A] = Reader(identity)
   *   }
   * }}}
   */
  type Reader[A, B] = ReaderT[Id, A, B]

  object Reader {
    def pure[A, B](b: B) = ReaderT.pure[Id, A, B](b)

    def ask[A] = ReaderT.ask[Id, A]
  }
}
