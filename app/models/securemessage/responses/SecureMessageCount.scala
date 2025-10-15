/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.securemessage.responses

import play.api.libs.json.{Json, OFormat, OWrites, Reads, __}

case class SecureMessageCount(total: Long, unread: Long) {
  lazy val read: Long =  total - unread
}

object SecureMessageCount {
  val reads: Reads[SecureMessageCount] = {

    import play.api.libs.functional.syntax.*

    (
      (__ \ "total").read[Long] and
        (__ \ "unread").read[Long]
      )(SecureMessageCount.apply _)
  }

  val writes: OWrites[SecureMessageCount] = {

    import play.api.libs.functional.syntax.*

    (
      (__ \ "total").write[Long] and
        (__ \ "unread").write[Long]
      )(messageCount => Tuple.fromProductTyped(messageCount))
  }
  
  implicit val formats: OFormat[SecureMessageCount] = OFormat(reads, writes)
  
}
