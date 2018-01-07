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
   *     def pure[C, A](a: A): Reader[C, A] = Reader(_ => a)
   *
   *     def ask[A]: Reader[A, A] = Reader(identity)
   *   }
   * }}}
   */
  type Reader[C, A] = ReaderT[Id, C, A]
  
}
