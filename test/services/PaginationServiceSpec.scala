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
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.{Pagination, PaginationItem, PaginationLink}


class PaginationServiceSpec extends SpecBase with MockitoSugar {

  private val service = new PaginationService()

  "PaginationService" - {

    "core pagination behaviour" - {

      "must return correct pagination for first page with 20 records per page" in {
        val items = 1 to 25

        val result = service.paginate(
          allItems = items,
          currentPage = 1,
          baseUrl = "/secure-messages"
        )

        result.items mustEqual (1 to 20)
        result.currentPage mustEqual 1
        result.totalPages mustEqual 2
        result.totalRecords mustEqual 25

        result.pagination mustBe defined
        result.pagination.value.previous mustBe None
        result.pagination.value.next mustBe Some(PaginationLink(href = "/secure-messages?page=2"))
        result.pagination.value.items.value mustEqual Seq(
          PaginationItem(
            number = Some("1"),
            href = "/secure-messages?page=1",
            current = Some(true)
          ),
          PaginationItem(
            number = Some("2"),
            href = "/secure-messages?page=2",
            current = None
          )
        )
      }

      "must return correct pagination for last page" in {
        val items = 1 to 25

        val result = service.paginate(
          allItems = items,
          currentPage = 2,
          baseUrl = "/secure-messages"
        )

        result.items mustEqual (21 to 25)
        result.currentPage mustEqual 2
        result.totalPages mustEqual 2
        result.totalRecords mustEqual 25

        result.pagination mustBe defined
        result.pagination.value.previous mustBe Some(PaginationLink(href = "/secure-messages?page=1"))
        result.pagination.value.next mustBe None
        result.pagination.value.items.value mustEqual Seq(
          PaginationItem(
            number = Some("1"),
            href = "/secure-messages?page=1",
            current = None
          ),
          PaginationItem(
            number = Some("2"),
            href = "/secure-messages?page=2",
            current = Some(true)
          )
        )
      }

      "must return correct pagination for middle page" in {
        val items = 1 to 60

        val result = service.paginate(
          allItems = items,
          currentPage = 2,
          baseUrl = "/secure-messages"
        )

        result.items mustEqual (21 to 40)
        result.currentPage mustEqual 2
        result.totalPages mustEqual 3
        result.totalRecords mustEqual 60

        result.pagination mustBe defined
        result.pagination.value.previous mustBe Some(PaginationLink(href = "/secure-messages?page=1"))
        result.pagination.value.next mustBe Some(PaginationLink(href = "/secure-messages?page=3"))
        result.pagination.value.items.value mustEqual Seq(
          PaginationItem(number = Some("1"), href = "/secure-messages?page=1", current = None),
          PaginationItem(number = Some("2"), href = "/secure-messages?page=2", current = Some(true)),
          PaginationItem(number = Some("3"), href = "/secure-messages?page=3", current = None)
        )
      }

      "must return all records when under the maximum record limit" in {
        val items = 1 to 120

        val result = service.paginate(
          allItems = items,
          currentPage = 5,
          baseUrl = "/secure-messages"
        )

        result.totalRecords mustEqual 120
        result.totalPages mustEqual 6
        result.currentPage mustEqual 5
        result.items mustEqual (81 to 100)

        result.pagination mustBe defined
        result.pagination.value.previous mustBe Some(PaginationLink(href = "/secure-messages?page=4"))
        result.pagination.value.next mustBe Some(PaginationLink(href = "/secure-messages?page=6"))
      }

      "must default current page to 1 when page is less than 1" in {
        val items = 1 to 25

        val result = service.paginate(
          allItems = items,
          currentPage = 0,
          baseUrl = "/secure-messages"
        )

        result.currentPage mustEqual 1
        result.items mustEqual (1 to 20)
      }

      "must use the last page when requested page is greater than total pages" in {
        val items = 1 to 25

        val result = service.paginate(
          allItems = items,
          currentPage = 99,
          baseUrl = "/secure-messages"
        )

        result.currentPage mustEqual 2
        result.totalPages mustEqual 2
        result.items mustEqual (21 to 25)

        result.pagination mustBe defined
        result.pagination.value.previous mustBe Some(PaginationLink(href = "/secure-messages?page=1"))
        result.pagination.value.next mustBe None
      }

      "must not show pagination when there is only one page" in {
        val items = 1 to 20

        val result = service.paginate(
          allItems = items,
          currentPage = 1,
          baseUrl = "/secure-messages"
        )

        result.totalPages mustEqual 1
        result.pagination mustBe None
      }

      "must generate correct pagination links" in {
        val items = 1 to 60

        val result = service.paginate(
          allItems = items,
          currentPage = 2,
          baseUrl = "/secure-messages"
        )

        result.pagination mustBe defined
        result.pagination.value.previous mustBe Some(PaginationLink(href = "/secure-messages?page=1"))
        result.pagination.value.next mustBe Some(PaginationLink(href = "/secure-messages?page=3"))
        result.pagination.value.items.value.map(_.href) mustEqual Seq(
          "/secure-messages?page=1",
          "/secure-messages?page=2",
          "/secure-messages?page=3"
        )
        result.pagination.value.items.value.flatMap(_.number) mustEqual Seq("1", "2", "3")
      }

      "must handle empty data" in {
        val items = Seq.empty[Int]

        val result = service.paginate(
          allItems = items,
          currentPage = 1,
          baseUrl = "/secure-messages"
        )

        result.items mustBe empty
        result.currentPage mustEqual 1
        result.totalRecords mustEqual 0
        result.totalPages mustEqual 1
        result.pagination mustBe None
      }
    }

    "ellipsis display behaviour" - {

      "must not show ellipsis when total pages are five" in {
        val items = 1 to 100

        val result = service.paginate(
          allItems = items,
          currentPage = 3,
          baseUrl = "/secure-messages"
        )

        result.pagination.value.items.value mustEqual Seq(
          PaginationItem(number = Some("1"), href = "/secure-messages?page=1", current = None),
          PaginationItem(number = Some("2"), href = "/secure-messages?page=2", current = None),
          PaginationItem(number = Some("3"), href = "/secure-messages?page=3", current = Some(true)),
          PaginationItem(number = Some("4"), href = "/secure-messages?page=4", current = None),
          PaginationItem(number = Some("5"), href = "/secure-messages?page=5", current = None)
        )
      }

      "must start using ellipsis when total pages exceed five" in {
        val items = 1 to 120

        val result = service.paginate(
          allItems = items,
          currentPage = 1,
          baseUrl = "/secure-messages"
        )

        result.pagination.value.items.value mustEqual Seq(
          PaginationItem(number = Some("1"), href = "/secure-messages?page=1", current = Some(true)),
          PaginationItem(number = Some("2"), href = "/secure-messages?page=2", current = None),
          PaginationItem(ellipsis = Some(true), href = ""),
          PaginationItem(number = Some("6"), href = "/secure-messages?page=6", current = None)
        )
      }

      "must show correct pagination for page 1 of 100" in {
        val items = 1 to 2000

        val result = service.paginate(
          allItems = items,
          currentPage = 1,
          baseUrl = "/secure-messages"
        )

        result.pagination.value.items.value mustEqual Seq(
          PaginationItem(number = Some("1"), href = "/secure-messages?page=1", current = Some(true)),
          PaginationItem(number = Some("2"), href = "/secure-messages?page=2", current = None),
          PaginationItem(ellipsis = Some(true), href = ""),
          PaginationItem(number = Some("100"), href = "/secure-messages?page=100", current = None)
        )
      }

      "must show correct pagination for page 2 of 100" in {
        val items = 1 to 2000

        val result = service.paginate(
          allItems = items,
          currentPage = 2,
          baseUrl = "/secure-messages"
        )

        result.pagination.value.items.value mustEqual Seq(
          PaginationItem(number = Some("1"), href = "/secure-messages?page=1", current = None),
          PaginationItem(number = Some("2"), href = "/secure-messages?page=2", current = Some(true)),
          PaginationItem(number = Some("3"), href = "/secure-messages?page=3", current = None),
          PaginationItem(ellipsis = Some(true), href = ""),
          PaginationItem(number = Some("100"), href = "/secure-messages?page=100", current = None)
        )
      }

      "must show correct pagination for page 3 of 100" in {
        val items = 1 to 2000

        val result = service.paginate(
          allItems = items,
          currentPage = 3,
          baseUrl = "/secure-messages"
        )

        result.pagination.value.items.value mustEqual Seq(
          PaginationItem(number = Some("1"), href = "/secure-messages?page=1", current = None),
          PaginationItem(number = Some("2"), href = "/secure-messages?page=2", current = None),
          PaginationItem(number = Some("3"), href = "/secure-messages?page=3", current = Some(true)),
          PaginationItem(number = Some("4"), href = "/secure-messages?page=4", current = None),
          PaginationItem(ellipsis = Some(true), href = ""),
          PaginationItem(number = Some("100"), href = "/secure-messages?page=100", current = None)
        )
      }

      "must show correct pagination for page 4 of 100" in {
        val items = 1 to 2000

        val result = service.paginate(
          allItems = items,
          currentPage = 4,
          baseUrl = "/secure-messages"
        )

        result.pagination.value.items.value mustEqual Seq(
          PaginationItem(number = Some("1"), href = "/secure-messages?page=1", current = None),
          PaginationItem(number = Some("2"), href = "/secure-messages?page=2", current = None),
          PaginationItem(number = Some("3"), href = "/secure-messages?page=3", current = None),
          PaginationItem(number = Some("4"), href = "/secure-messages?page=4", current = Some(true)),
          PaginationItem(number = Some("5"), href = "/secure-messages?page=5", current = None),
          PaginationItem(ellipsis = Some(true), href = ""),
          PaginationItem(number = Some("100"), href = "/secure-messages?page=100", current = None)
        )
      }

      "must show correct pagination for page 5 of 100" in {
        val items = 1 to 2000

        val result = service.paginate(
          allItems = items,
          currentPage = 5,
          baseUrl = "/secure-messages"
        )

        result.pagination.value.items.value mustEqual Seq(
          PaginationItem(number = Some("1"), href = "/secure-messages?page=1", current = None),
          PaginationItem(ellipsis = Some(true), href = ""),
          PaginationItem(number = Some("4"), href = "/secure-messages?page=4", current = None),
          PaginationItem(number = Some("5"), href = "/secure-messages?page=5", current = Some(true)),
          PaginationItem(number = Some("6"), href = "/secure-messages?page=6", current = None),
          PaginationItem(ellipsis = Some(true), href = ""),
          PaginationItem(number = Some("100"), href = "/secure-messages?page=100", current = None)
        )
      }

      "must show correct pagination for page 50 of 100" in {
        val items = 1 to 2000

        val result = service.paginate(
          allItems = items,
          currentPage = 50,
          baseUrl = "/secure-messages"
        )

        result.pagination.value.items.value mustEqual Seq(
          PaginationItem(number = Some("1"), href = "/secure-messages?page=1", current = None),
          PaginationItem(ellipsis = Some(true), href = ""),
          PaginationItem(number = Some("49"), href = "/secure-messages?page=49", current = None),
          PaginationItem(number = Some("50"), href = "/secure-messages?page=50", current = Some(true)),
          PaginationItem(number = Some("51"), href = "/secure-messages?page=51", current = None),
          PaginationItem(ellipsis = Some(true), href = ""),
          PaginationItem(number = Some("100"), href = "/secure-messages?page=100", current = None)
        )
      }

      "must show correct pagination for page 97 of 100" in {
        val items = 1 to 2000

        val result = service.paginate(
          allItems = items,
          currentPage = 97,
          baseUrl = "/secure-messages"
        )

        result.pagination.value.items.value mustEqual Seq(
          PaginationItem(number = Some("1"), href = "/secure-messages?page=1", current = None),
          PaginationItem(ellipsis = Some(true), href = ""),
          PaginationItem(number = Some("96"), href = "/secure-messages?page=96", current = None),
          PaginationItem(number = Some("97"), href = "/secure-messages?page=97", current = Some(true)),
          PaginationItem(number = Some("98"), href = "/secure-messages?page=98", current = None),
          PaginationItem(ellipsis = Some(true), href = ""),
          PaginationItem(number = Some("100"), href = "/secure-messages?page=100", current = None)
        )
      }

      "must show correct pagination for page 98 of 100" in {
        val items = 1 to 2000

        val result = service.paginate(
          allItems = items,
          currentPage = 98,
          baseUrl = "/secure-messages"
        )

        result.pagination.value.items.value mustEqual Seq(
          PaginationItem(number = Some("1"), href = "/secure-messages?page=1", current = None),
          PaginationItem(ellipsis = Some(true), href = ""),
          PaginationItem(number = Some("97"), href = "/secure-messages?page=97", current = None),
          PaginationItem(number = Some("98"), href = "/secure-messages?page=98", current = Some(true)),
          PaginationItem(number = Some("99"), href = "/secure-messages?page=99", current = None),
          PaginationItem(number = Some("100"), href = "/secure-messages?page=100", current = None)
        )
      }

      "must show correct pagination for page 99 of 100" in {
        val items = 1 to 2000

        val result = service.paginate(
          allItems = items,
          currentPage = 99,
          baseUrl = "/secure-messages"
        )

        result.pagination.value.items.value mustEqual Seq(
          PaginationItem(number = Some("1"), href = "/secure-messages?page=1", current = None),
          PaginationItem(ellipsis = Some(true), href = ""),
          PaginationItem(number = Some("98"), href = "/secure-messages?page=98", current = None),
          PaginationItem(number = Some("99"), href = "/secure-messages?page=99", current = Some(true)),
          PaginationItem(number = Some("100"), href = "/secure-messages?page=100", current = None)
        )
      }

      "must show correct pagination for page 100 of 100" in {
        val items = 1 to 2000

        val result = service.paginate(
          allItems = items,
          currentPage = 100,
          baseUrl = "/secure-messages"
        )

        result.pagination.value.items.value mustEqual Seq(
          PaginationItem(number = Some("1"), href = "/secure-messages?page=1", current = None),
          PaginationItem(ellipsis = Some(true), href = ""),
          PaginationItem(number = Some("99"), href = "/secure-messages?page=99", current = None),
          PaginationItem(number = Some("100"), href = "/secure-messages?page=100", current = Some(true))
        )
      }
    }
  }
}
