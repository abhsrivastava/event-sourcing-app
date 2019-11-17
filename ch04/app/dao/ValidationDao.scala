package dao

import scalikejdbc._
import scala.util.{Try, Success, Failure}
import java.util.UUID

class ValidationDao {
    def updateTagCreated(tagId: UUID, tagText: String) : Option[String] = {
        invokeUpdate {
            NamedDB('validation).localTx{implicit session =>
                sql"""
                    insert into tags(tag_id, tag_text) values ($tagId, $tagText)
                """.update().apply()
            }
        }
    }
    def validateTagCreated(tagText: String, userId: UUID) : Option[String] = {
        validateUser(userId) {
            doesTagExist(tagText) match {
                case Success(Some(_)) => Some("Tag already exists")
                case Success(None) => None
                case Failure(_) => Some("Validation State Exception")
            }
        }
    }
    def validateTagDeleted(tagId: UUID, userId: UUID) : Option[String] = {
        validateUser(userId) {
            val mayBeExistingTag = NamedDB('validation).readOnly{implicit session =>
                sql"""
                    select tag_id from tags where tag_id = $tagId
                """.map(_.string("tag_id")).headOption().apply()
            }
            val mayBeExistingQuestion = NamedDB('valiadtion).readOnly{implicit session => 
                sql"""
                    select question_id from tag_question where tag_id=$tagId
                """.map(_.string("question_id")).headOption().apply()
            }
            (mayBeExistingTag, mayBeExistingQuestion) match {
                case (None, _) => Some("Tag does not exists")
                case (_, Some(_)) => Some("There are questions associated with this tag")
                case (_, _) => None
            }
        }
    }
    def validateUser(userId: UUID)(block : => Option[String]) : Option[String] = {
        isActivated(userId) match {
            case Success(false) => Some("The user is not active")
            case Success(true) => block
            case Failure(_) => Some("Validation State Exception") 
        }
    }
    def validateUserActivated(userId: UUID) : Option[String] = {
        isActivated(userId) match {
            case Success(true) => Some("User is already active")
            case Success(false) => None
            case Failure(_) => Some("Validation State Exception")
        }
    }
    def updateUserActivated(userId: UUID) : Option[String] = {
        invokeUpdate{
            NamedDB('validation).localTx{implicit session=>
                sql"""
                    insert into active_users(user_id) values($userId)
                """.update().apply()
            }
        }
    }
    def validateUserDeactivated(userId: UUID) : Option[String] = {
        isActivated(userId) match {
            case Success(true) => None
            case Success(false) => Some("user is already deactivated")
            case Failure(_) => Some("Validation State Exception")
        }
    }
    def isActivated(userId: UUID) : Try[Boolean] = {
        Try {
            NamedDB('validation).localTx{implicit session=>
                sql"""
                    select user_id from active_users where user_id = $userId
                """.map(_.string("user_id")).headOption().apply().isDefined
            }
        }
    }
    def doesTagExist(tagText: String) : Try[Option[String]] = {
        Try{
            NamedDB('validation).localTx{implicit session =>
                sql"""select tag_id from tags where tag_text = $tagText""".map(_.string("tag_id")).headOption().apply()
            }
        }
    }
    def updateTagDeleted(tagId: UUID) : Option[String] = {
        invokeUpdate{
            NamedDB('validation).localTx{implicit session =>
                sql"""
                    delete from tags where tag_id=$tagId
                """.update().apply()
            }
        }
    }
    def updateUserActivited(userId: UUID) : Option[String] = {
        invokeUpdate{
            NamedDB('validation).localTx{implicit session =>
                sql"""
                    insert into active_users(user_id) values($userId)
                """.update().apply()
            }
        }
    }
    def updateUserDeactivated(userId: UUID) : Option[String] = {
        invokeUpdate {
            NamedDB('validation).localTx{implicit session =>
                sql"""
                    delete from active_users where user_id = $userId
                """.update().apply()
            }
        }
    }
    def updateQuestionCreated(questionId: UUID, userId: UUID, tags: List[UUID]) : Option[String] = {
        invokeUpdate{
            NamedDB('validation).localTx{implicit session=>
                sql"""
                    insert into question_user(question_id, user_id) values($questionId, $userId)
                """.update().apply()
                tags.foreach{tagId =>
                    sql"""
                        insert into tag_question(tag_id, question_id) values($tagId, $questionId)
                    """.update().apply()
                }
            }
        }
    }
    def validateQuestionCreated(questionId: UUID, userId: UUID, tags: List[UUID]) : Option[String] = {
        validateUser(userId) {
            Try { NamedDB('validation).localTx{implicit session=>
                implicit val binderFactory : ParameterBinderFactory[UUID] = ParameterBinderFactory{
                    value => (stmt, idx) => stmt.setObject(idx, value)
                }
                val tagIdsSql = SQLSyntax.in(sqls"tag_id", tags)
                sql"""select tag_id from tags where $tagIdsSql""".map(_.string("tag_id")).list().apply()
            }} match {
                case Success(existingTags) if existingTags.size == tags.size => None
                case Success(_) => Some("Some tags do not exist")
                case Failure(_) => Some("Validation state exception")
            }
        }
    }
    def updateQuestionDeleted(questionId: UUID) : Option[String] = {
        invokeUpdate{
            NamedDB('validation).localTx{implicit session =>
                sql"""
                    delete from question_user where question_id = $questionId
                """.update().apply()
            }
        }
    }
    def validateQuestionDeleted(questionId: UUID, userId: UUID) : Option[String] = {
        validateUser(userId) {
            val mayBeQuestionOwner : Try[Option[String]] = Try {
                NamedDB('validation).readOnly{implicit session =>
                    sql"""
                        select user_id from question_user where question_id = $questionId
                    """.map(_.string("user_id")).headOption().apply()
                }
            } 
            mayBeQuestionOwner match {
                case Success(None) => Some("question does not exist")
                case Success(Some(questionOwner)) if questionOwner == userId => None
                case Success(Some(_)) => Some("question does not belong to user")
                case Failure(_) => Some("Validation state exception")
            }
        }
    }
    def invokeUpdate(block : => Any) : Option[String] = {
        Try{block} match {
            case Success(_) => None
            case Failure(th) => Some("Validated State Exception")
        }
    }
    def validateAndUpdate(skipValidation: Boolean = true)(validateBlock : => Option[String])(updateBlock : => Option[String]) : Option[String] ={
        if (skipValidation) {
            updateBlock
        } else {
            validateBlock match {
                case None => updateBlock
                case x => x
            }
        }
    }
    def resetState(fromScratch: Boolean) : Option[String] = {
        if(fromScratch) {
            invokeUpdate{
                NamedDB('validation).localTx{implicit session => 
                    sql"delete from tags where 1 > 0".update().apply()
                    sql"delete from active_users where 1 > 0".update().apply()
                    sql"delete from tag_question where 1 > 0".update().apply()
                    sql"delete from question_user where 1 > 0".update().apply()
                }
            }
        } else Option.empty[String]
    }
}