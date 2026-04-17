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


import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.{Pagination, PaginationItem, PaginationLink}

case class PaginationConfig(
                             recordsPerPage: Int = 20,
                             maxRecords: Int = 9999,
                             maxVisiblePages: Int = 5
                           )

case class PaginationResult[A](
                              items: Seq[A],
                              currentPage: Int,
                              totalPages: Int,
                              totalRecords: Int,
                              pagination: Option[Pagination]
                           )

sealed trait PaginationItem
case class PageLink(
                     number: Int,
                     url: String,
                     current: Boolean
                   ) extends PaginationItem

case object Ellipsis extends PaginationItem

case class PaginationViewModel(
                                items: Seq[PaginationItem] = Nil,
                                previousUrl: Option[String] = None,
                                nextUrl: Option[String] = None
                              ) {
  def show: Boolean =
    previousUrl.nonEmpty || nextUrl.nonEmpty || items.nonEmpty
}

@Singleton
class PaginationService @Inject()() {
  
  private val config = PaginationConfig()
  
  def paginate[A](
                   allItems: Seq[A],
                   currentPage: Int,
                   baseUrl: String
                 ): PaginationResult[A] = {

    val cappedItems = allItems.take(config.maxRecords)
    val totalRecords = cappedItems.size
    val totalPages = math.max(1, math.ceil(totalRecords.toDouble / config.recordsPerPage).toInt)
    val validCurrentPage = math.max(1, math.min(currentPage, totalPages))
    val startIndex = (validCurrentPage - 1) * config.recordsPerPage
    val endIndex = math.min(startIndex + config.recordsPerPage, totalRecords)
    val paginatedItems = cappedItems.slice(startIndex, endIndex)

    PaginationResult(
      items = paginatedItems,
      currentPage = validCurrentPage,
      totalPages = totalPages,
      totalRecords = totalRecords,
      pagination =
        if (totalPages <= 1) {
          None
        } else {
          Some(buildGovukPagination(validCurrentPage, totalPages, baseUrl))
        }
    )
  }

  private def pageUrl(baseUrl: String, page: Int): String = {
    val separator = if (baseUrl.contains("?")) "&" else "?"
    s"$baseUrl${separator}page=$page"
  }

  private def buildGovukPagination(
                                    currentPage: Int,
                                    totalPages: Int,
                                    baseUrl: String
                                  ): Pagination = {
    val items = buildPaginationItems(currentPage, totalPages, baseUrl)

    Pagination(
      items = Some(
        items.map {
          case PageLink(number, url, current) =>
            PaginationItem(
              number = Some(number.toString),
              href = url,
              current = if (current) Some(true) else None
            )

          case Ellipsis =>
            PaginationItem(
              ellipsis = Some(true),
              href = ""
            )
        }
      ),
      previous =
        if (currentPage > 1) {
          Some(
            PaginationLink(
              href = pageUrl(baseUrl, currentPage - 1)
            )
          )
        } else {
          None
        },
      next =
        if (currentPage < totalPages) {
          Some(
            PaginationLink(
              href = pageUrl(baseUrl, currentPage + 1)
            )
          )
        } else {
          None
        }
    )
  }

  private def buildPaginationItems(
                                    currentPage: Int,
                                    totalPages: Int,
                                    baseUrl: String
                                  ): Seq[PaginationItem] = {

    val pagesToShow: Seq[Int] =
      if (totalPages <= 5) {
        1 to totalPages
      } else if (currentPage == 1) {
        Seq(1, 2, totalPages)
      } else if (currentPage == 2) {
        Seq(1, 2, 3, totalPages)
      } else if (currentPage == 3) {
        Seq(1, 2, 3, 4, totalPages)
      } else if (currentPage == 4) {
        Seq(1, 2, 3, 4, 5, totalPages)
      } else if (currentPage == totalPages) {
        Seq(1, totalPages - 1, totalPages)
      } else if (currentPage == totalPages - 1) {
        Seq(1, totalPages - 2, totalPages - 1, totalPages)
      } else if (currentPage == totalPages - 2) {
        Seq(1, totalPages - 3, totalPages - 2, totalPages - 1, totalPages)
      } else {
        Seq(1, currentPage - 1, currentPage, currentPage + 1, totalPages)
      }
    
    val distinctSortedPages = pagesToShow.distinct.filter(p => p >= 1 && p <= totalPages).sorted

    val items = scala.collection.mutable.ListBuffer[PaginationItem]()

    distinctSortedPages.zipWithIndex.foreach { case (page, index) =>
      if (index > 0) {
        val previousPage = distinctSortedPages(index - 1)
        if (page - previousPage > 1) {
          items += Ellipsis
        }
      }

      items += PageLink(
        number = page,
        url = pageUrl(baseUrl, page),
        current = page == currentPage
      )
    }

    items.toSeq
  }
}