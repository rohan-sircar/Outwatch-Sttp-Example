package outwatchexample

import outwatch._
import outwatch.dsl._

import cats.effect.ExitCode
import monix.bio._
import colibri.ext.monix._
import sttp.client.impl.monix.FetchMonixBackend
import outwatch.reactive.handlers.monix._
import sttp.client._
import monix.{eval => me}

object OutwatchApp extends BIOApp {

  implicit val backend = FetchMonixBackend()

  val handlerTask = Handler.createF[Task, String]("empty")

  def request(query: String) = {
    val id = query.toIntOption.getOrElse(0)
    basicRequest
      .get(uri"https://jsonplaceholder.typicode.com/todos/$id")
      .send()
      .flatMap {
        _.body match {
          case Right(value) =>
            me.Task(println(value)) >>
              me.Task(value)
          case Left(error) =>
            me.Task(println(error)) >>
              me.Task(error)

        }
      }
  }
  val component = Task.deferAction(implicit s =>
    for {
      handler <- handlerTask
      res <- Task(
        div(
          "input",
          input(
            typ := "text",
            onInput.value --> handler
          ),
          div(
            handler
              .doOnNextF(str => Task(println(str)))
              .mapEval(query => request(query))
              .map(e => div(e))
          )
        )
      )
    } yield res
  )

  def run(args: List[String]): UIO[ExitCode] = {
    import org.scalajs.dom.document
    val el =
      document.createElement("div")
    el.setAttribute("id", "#app")
    document.body.appendChild(el)
    OutWatch
      .renderInto(
        el,
        div(component)
      )
      .onErrorHandle(_.printStackTrace())
      .as(ExitCode.Success)
  }
}
