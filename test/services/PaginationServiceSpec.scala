/*
 * Copyright 2026 HM Revenue & Customs
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

package services

import base.SpecBase
import org.scalatestplus.mockito.MockitoSugar


class PaginationServiceSpec extends SpecBase with MockitoSugar {

  private val service = new PaginationService()

  "PaginationService" - {

    "must return correct pagination for first page with 20 records per page" in {
      val items = 1 to 25

      val (result, viewModel) = service.paginate(
        allItems = items,
        currentPage = 1,
        baseUrl = "/secure-messages"
      )

      result.items mustEqual (1 to 20)
      result.currentPage mustEqual 1
      result.totalPages mustEqual 2
      result.totalRecords mustEqual 25

      viewModel.show mustEqual true
      viewModel.previousUrl mustBe None
      viewModel.nextUrl mustBe Some("/secure-messages?page=2")
      viewModel.items mustEqual Seq(
        PageLink(1, "/secure-messages?page=1", current = true),
        PageLink(2, "/secure-messages?page=2", current = false)
      )
    }

    "must return correct pagination for last page" in {
      val items = 1 to 25

      val (result, viewModel) = service.paginate(
        allItems = items,
        currentPage = 2,
        baseUrl = "/secure-messages"
      )

      result.items mustEqual (21 to 25)
      result.currentPage mustEqual 2
      result.totalPages mustEqual 2
      result.totalRecords mustEqual 25

      viewModel.show mustEqual true
      viewModel.previousUrl mustBe Some("/secure-messages?page=1")
      viewModel.nextUrl mustBe None
      viewModel.items mustEqual Seq(
        PageLink(1, "/secure-messages?page=1", current = false),
        PageLink(2, "/secure-messages?page=2", current = true)
      )

    }

    "must return correct pagination for middle page" in {
      val items = 1 to 60

      val (result, viewModel) = service.paginate(
        allItems = items,
        currentPage = 2,
        baseUrl = "/secure-messages"
      )

      result.items mustEqual (21 to 40)
      result.currentPage mustEqual 2
      result.totalPages mustEqual 3
      result.totalRecords mustEqual 60

      viewModel.show mustEqual true
      viewModel.previousUrl mustBe Some("/secure-messages?page=1")
      viewModel.nextUrl mustBe Some("/secure-messages?page=3")
      viewModel.items mustEqual Seq(
        PageLink(1, "/secure-messages?page=1", current = false),
        PageLink(2, "/secure-messages?page=2", current = true),
        PageLink(3, "/secure-messages?page=3", current = false)
      )
    }

    "must return all records when under the maximum record limit" in {
      val items = 1 to 120

      val (result, viewModel) = service.paginate(
        allItems = items,
        currentPage = 5,
        baseUrl = "/secure-messages"
      )

      result.totalRecords mustEqual 120
      result.totalPages mustEqual 6
      result.currentPage mustEqual 5
      result.items mustEqual (81 to 100)

      viewModel.show mustEqual true
      viewModel.previousUrl mustBe Some("/secure-messages?page=4")
      viewModel.nextUrl mustBe Some("/secure-messages?page=6")
      viewModel.items mustEqual Seq(
        PageLink(1, "/secure-messages?page=1", current = false),
        PageLink(2, "/secure-messages?page=2", current = false),
        PageLink(3, "/secure-messages?page=3", current = false),
        PageLink(4, "/secure-messages?page=4", current = false),
        PageLink(5, "/secure-messages?page=5", current = true)
      )
    }

    "must default current page to 1 when page is less than 1" in {
      val items = 1 to 25

      val (result, _) = service.paginate(
        allItems = items,
        currentPage = 0,
        baseUrl = "/secure-messages"
      )

      result.currentPage mustEqual 1
      result.items mustEqual (1 to 20)
    }

    "must use the last page when requested page is greater than total pages" in {
      val items = 1 to 25

      val (result, viewModel) = service.paginate(
        allItems = items,
        currentPage = 99,
        baseUrl = "/secure-messages"
      )

      result.currentPage mustEqual 2
      result.totalPages mustEqual 2
      result.items mustEqual (21 to 25)

      viewModel.previousUrl mustBe Some("/secure-messages?page=1")
      viewModel.nextUrl mustBe None
    }

    "must not show pagination when there is only one page" in {
      val items = 1 to 20

      val (result, viewModel) = service.paginate(
        allItems = items,
        currentPage = 1,
        baseUrl = "/secure-messages"
      )

      result.totalPages mustEqual 1
      viewModel.show mustEqual false
      viewModel.items mustEqual Nil
      viewModel.previousUrl mustBe None
      viewModel.nextUrl mustBe None
    }

    "must generate correct pagination links" in {
      val items = 1 to 60

      val (_, viewModel) = service.paginate(
        allItems = items,
        currentPage = 2,
        baseUrl = "/secure-messages"
      )

      viewModel.previousUrl mustBe Some("/secure-messages?page=1")
      viewModel.nextUrl mustBe Some("/secure-messages?page=3")
      viewModel.items.collect { case p: PageLink => p.url } mustEqual Seq(
        "/secure-messages?page=1",
        "/secure-messages?page=2",
        "/secure-messages?page=3"
      )
      viewModel.items.collect { case p: PageLink => p.number } mustEqual Seq(1, 2, 3)
    }

    "must handle empty data" in {
      val items = Seq.empty[Int]

      val (result, viewModel) = service.paginate(
        allItems = items,
        currentPage = 1,
        baseUrl = "/secure-messages"
      )

      result.items mustBe empty
      result.currentPage mustEqual 1
      result.totalRecords mustEqual 0
      result.totalPages mustEqual 1

      viewModel.show mustEqual false
      viewModel.items mustBe empty
      viewModel.previousUrl mustBe None
      viewModel.nextUrl mustBe None
    }

    "must not show ellipsis when current page is near the start" in {
      val items = 1 to 2000

      val (_, viewModel) = service.paginate(
        allItems = items,
        currentPage = 1,
        baseUrl = "/secure-messages"
      )

      viewModel.items mustEqual Seq(
        PageLink(1, "/secure-messages?page=1", current = true),
        PageLink(2, "/secure-messages?page=2", current = false),
        PageLink(3, "/secure-messages?page=3", current = false),
        PageLink(4, "/secure-messages?page=4", current = false),
        PageLink(5, "/secure-messages?page=5", current = false)
      )
    }

    "must show ellipsis when current page is in the middle" in {
      val items = 1 to 2000

      val (_, viewModel) = service.paginate(
        allItems = items,
        currentPage = 50,
        baseUrl = "/secure-messages"
      )

      viewModel.items mustEqual Seq(
        PageLink(1, "/secure-messages?page=1", current = false),
        Ellipsis,
        PageLink(49, "/secure-messages?page=49", current = false),
        PageLink(50, "/secure-messages?page=50", current = true),
        PageLink(51, "/secure-messages?page=51", current = false),
        Ellipsis,
        PageLink(100, "/secure-messages?page=100", current = false)
      )
    }

    "must show ellipsis when current page is near the end" in {
      val items = 1 to 2000

      val (_, viewModel) = service.paginate(
        allItems = items,
        currentPage = 99,
        baseUrl = "/secure-messages"
      )

      viewModel.items mustEqual Seq(
        PageLink(1, "/secure-messages?page=1", current = false),
        Ellipsis,
        PageLink(96, "/secure-messages?page=96", current = false),
        PageLink(97, "/secure-messages?page=97", current = false),
        PageLink(98, "/secure-messages?page=98", current = false),
        PageLink(99, "/secure-messages?page=99", current = true),
        PageLink(100, "/secure-messages?page=100", current = false)
      )
    }
  }
}
