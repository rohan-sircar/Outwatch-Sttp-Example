import cats.arrow.FunctionK
import monix.bio.IO

object IOUtils {
  def toIO[T](task: monix.eval.Task[T]) =
    IO.deferAction(implicit s => IO.from(task))

  def toTask[T](bio: monix.bio.IO[Throwable, T]) =
    monix.eval.Task.deferAction(implicit s => bio.to[monix.eval.Task])

  val ioTaskMapk =
    new FunctionK[monix.eval.Task, monix.bio.Task] {

      override def apply[A](
          fa: monix.eval.Task[A]
      ): monix.bio.Task[A] = toIO(fa)

    }
}
