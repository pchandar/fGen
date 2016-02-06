package com.pchandar.utils


import com.mongodb.DBObject
import com.mongodb.casbah.Imports._
import com.mongodb.casbah._
import com.mongodb.util.JSON
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsSuccess, Json, Reads, Writes}

import scala.concurrent.duration._
import scala.util.Random
import scalaz.concurrent.Task
import scalaz.stream._

object MongoUtils {
  val logger = LoggerFactory.getLogger(getClass.getName)

  // Safe bulk inserts to mongo  (usually this is faster)
  implicit class SafeInsert(val builder: BulkWriteOperation) {
    def insertIfNonEmpty(o: DBObject): Unit = if (o.nonEmpty) builder.insert(o) else ()
  }


  // Log when mongo bulk inserts happen
  implicit class TaskLoggingOps[A](val t: Task[A]) {
    def recordStartEnd(marker: String): Task[A] =
      for {
        r <- Task.delay(Random.alphanumeric.take(10).mkString)
        _ <- Task.delay(logger.debug(s"$r Starting " + marker + " at " + new DateTime()))
        out <- t
        _ <- Task.delay(logger.debug(s"$r Completing " + marker + " at " + new DateTime()))
      } yield out
  }


  def defaultSink(collection: MongoCollection): Sink[Task, Vector[Imports.DBObject]] = {
    val mongoSink: Sink[Task, Vector[DBObject]] = Process.constant { augmentedObjects =>
      Task {
        val builder = collection.initializeUnorderedBulkOperation
        augmentedObjects.foreach(builder.insertIfNonEmpty)
        if (augmentedObjects.nonEmpty) {
          builder.execute()
        }
        ()
      }.recordStartEnd("mongo persisting").
        unsafePerformRetry(Seq(1.minutes, 2.minutes, 5.minutes)).
        handle {
          case e: Throwable => logger.error("Unexpected mongo persistence error", e)
        }
    }
    mongoSink
  }

  def processCollection(datasetCollection: Option[MongoCollection])(
    getSourceCursor: MongoCollection => Iterator[DBObject]): Process[Task, DBObject] =
    io.resource(Task(datasetCollection))(c =>
      Task {
        logger.info(s"Cursor [$c] closed")
      }) { sourceCollection =>
      assert(sourceCollection.isDefined, "Dataset Collection must be defined")
      lazy val emailObjs = getSourceCursor(sourceCollection.get)
      Task {
        if (emailObjs.hasNext)
          emailObjs.next()
        else {
          logger.info("Done reading from source collection")
          throw Cause.Terminated(Cause.End)
        }
      }.handleWith {
        case e: Cause.Terminated =>
          Task.fail(e)
        case t: Throwable =>
          logger.error("Error reading from source collection", t)
          Task.fail(t)

      }
    }

  def getMongoCollection(dbName: String, collName: String, host: String = "localhost", port: Int = 27018) = {
    val url: String = host + ":" + port
    val sourceURI: com.mongodb.casbah.MongoClientURI = com.mongodb.casbah.MongoClientURI(url)
    com.mongodb.casbah.MongoClient(sourceURI)(dbName)(collName)
  }


  // An easy way to deserialze classes from Mongo
  def getObjectFromMongo[T: Reads](dbo: DBObject): T = {
    val id = dbo.get("_id")
    Json.fromJson[T](Json.parse(dbo.toString)) match {
      case JsSuccess(t, _) => t
      case e => throw new IllegalArgumentException(s"Parsing failure [$e]:\nin [$id]")
    }
  }

  // An easy way to serialze classes to Mongo
  def convertToDBObject[T: Writes](t: T): DBObject =
    JSON.parse(Json.stringify(Json.toJson(t))).asInstanceOf[DBObject]


}


