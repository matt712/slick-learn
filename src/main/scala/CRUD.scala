import slick.jdbc.MySQLProfile.api._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

object CRUD {
  def main(args: Array[String]) = {
    getByAge(36)
    Thread.sleep(1000)
    create("Matthew", "Gadd", 23)
    Thread.sleep(1000)
    birthdayFunction("Tim")
    Thread.sleep(1000)
   deleteFunction("Jack")
    Thread.sleep(1000)
  }
  def create(fname: String, lname: String, age: Int): Unit ={
    val db = Database.forConfig("mysqlDB")
    val peopleTable = TableQuery[Person]
    val create2 = Future {
      val add = peopleTable ++= Seq(
        (30, fname, lname, age)
      )
      db.run(add)
    }
    Await.result(create2, Duration.Inf).andThen {
      case Success(_) => listPeople
      case Failure(error) => println("Welp! Something went wrong! " + error.getMessage)
    }
  }
  def getByAge(age: Int): Unit ={
    val db = Database.forConfig("mysqlDB")
    val peopleTable = TableQuery[Person]
    val getAge = Future {
      val query = peopleTable.filter(_.age === age)
      val action = query.result
      db.run(action).map(_.foreach {
        case (id, fName, lName, age) => println(s"${age} ${fName}")
      })
    }
    Await.result(getAge, Duration.Inf).andThen {
      case Success(_) => println("query finished")
      case Failure(error) => println("Error is: " + error.getMessage)
    }
  }
  def birthdayFunction(fname: String):Unit = {
    println("Birthday start")
    val db = Database.forConfig("mysqlDB")
    val peopleTable = TableQuery[Person]
    val birthday = Future {
      val query = for( x <- peopleTable if x.fName === fname) yield x.age
      val updateAction = query.update(20)
      db.run(updateAction)
    }
    Await.result(birthday, Duration.Inf).andThen {
      case Success(_) => listPeople
      case Failure(error) => println("Error: " + error.getMessage)
    }
  }
  def deleteFunction(fName: String):String = {
    val db = Database.forConfig("mysqlDB")
    val peopleTable = TableQuery[Person]
    val deletePeople = Future {
      val query = peopleTable.filter(_.fName === fName)
      val delly = query.delete
      println(delly.statements.head)
      db.run(delly)
    }
    Await.result(deletePeople, Duration.Inf).andThen {
      case Success(_) => listPeople
      case Failure(error) => println("Welp! Something went wrong! " + error.getMessage)
    }
    "Done"
  }
  def listPeople = {
    val db = Database.forConfig("mysqlDB")
    val peopleTable = TableQuery[Person]
    val queryFuture = Future {
      // simple query that selects everything from People and prints them out
      db.run(peopleTable.result).map(_.foreach {
        case (id, fName, lName, age) => println(s" $id $fName $lName $age")})
    }
    Await.result(queryFuture, Duration.Inf).andThen {
      case Success(_) =>  db.close()  //cleanup DB connection
      case Failure(error) => println("Listing people failed due to: " + error.getMessage)
    }
  }
}
