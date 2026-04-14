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
import scala.concurrent.{ExecutionContext, Future}

case class PaginationConfig(
                             recordsPerPage: Int = 20,
                             maxRecords: Int = 99,
                             maxVisiblePages: Int = 5
                           )

case class PaginationResult[A](
                              items: Seq[A],
                              currentPage: Int,
                              totalPages: Int,
                              totalRecords: Int
                           )

case class PageLink(
                     number: Int,
                     url: String,
                     current: Boolean
                   )

case class PaginationViewModel(
                                pages: Seq[PageLink] = Nil,
                                previousUrl: Option[String] = None,
                                nextUrl: Option[String] = None
                              ) {
  def show: Boolean =
    previousUrl.nonEmpty || nextUrl.nonEmpty || pages.nonEmpty
}

@Singleton
class PaginationService @Inject()() {
  
  private val config = PaginationConfig()
  
  def paginate[A](
                   allItems: Seq[A],
                   currentPage: Int,
                   baseUrl: String
                 ): (PaginationResult[A], PaginationViewModel) = {
    
    val cappedItems = allItems.take(config.maxRecords)
    val totalRecords = cappedItems.size
    val totalPages = math.max(1, math.ceil(totalRecords.toDouble / config.recordsPerPage).toInt)
    val validCurrentPage = math.max(1, math.min(currentPage, totalPages))
    val startIndex = (validCurrentPage - 1) * config.recordsPerPage
    val endIndex = math.min(startIndex + config.recordsPerPage, totalRecords)
    val paginatedItems = cappedItems.slice(startIndex, endIndex)
    val result = PaginationResult(
      items = paginatedItems,
      currentPage = validCurrentPage,
      totalPages = totalPages,
      totalRecords = totalRecords
    )
    val viewModel =
      if (totalPages <= 1) {
        PaginationViewModel()
      } else {
        PaginationViewModel(
          pages = visiblePages(validCurrentPage, totalPages).map { page =>
            PageLink(
              number = page,
              url = s"$baseUrl?page=$page",
              current = page == validCurrentPage
            )
          },
          previousUrl =
            if (validCurrentPage > 1) Some(s"$baseUrl?page=${validCurrentPage - 1}") else None,
          nextUrl =
            if (validCurrentPage < totalPages) Some(s"$baseUrl?page=${validCurrentPage + 1}") else None
        )
      }

    (result, viewModel)
    
  }

  private def visiblePages(currentPage: Int, totalPages: Int): Seq[Int] = {
    val maxVisible = config.maxVisiblePages
    val half = maxVisible / 2

    val start = math.max(1, math.min(currentPage - half, totalPages - maxVisible + 1))
    val end = math.min(totalPages, start + maxVisible - 1)

    start to end
  }
}
