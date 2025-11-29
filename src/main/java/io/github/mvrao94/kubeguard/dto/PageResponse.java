package io.github.mvrao94.kubeguard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

@Schema(description = "Paginated response wrapper for list endpoints")
public class PageResponse<T> {

  @Schema(description = "List of items in the current page")
  private List<T> content;

  @Schema(description = "Current page number (0-based)", example = "0")
  private int pageNumber;

  @Schema(description = "Number of items per page", example = "10")
  private int pageSize;

  @Schema(description = "Total number of items across all pages", example = "150")
  private long totalElements;

  @Schema(description = "Total number of pages", example = "15")
  private int totalPages;

  @Schema(description = "Whether this is the first page", example = "true")
  private boolean first;

  @Schema(description = "Whether this is the last page", example = "false")
  private boolean last;

  @Schema(description = "Whether there are more pages", example = "true")
  private boolean hasNext;

  @Schema(description = "Whether there are previous pages", example = "false")
  private boolean hasPrevious;

  public PageResponse() {}

  public PageResponse(Page<T> page) {
    this.content = page.getContent();
    this.pageNumber = page.getNumber();
    this.pageSize = page.getSize();
    this.totalElements = page.getTotalElements();
    this.totalPages = page.getTotalPages();
    this.first = page.isFirst();
    this.last = page.isLast();
    this.hasNext = page.hasNext();
    this.hasPrevious = page.hasPrevious();
  }

  public static <T> PageResponse<T> of(Page<T> page) {
    return new PageResponse<>(page);
  }

  // Getters and setters
  public List<T> getContent() {
    return content;
  }

  public void setContent(List<T> content) {
    this.content = content;
  }

  public int getPageNumber() {
    return pageNumber;
  }

  public void setPageNumber(int pageNumber) {
    this.pageNumber = pageNumber;
  }

  public int getPageSize() {
    return pageSize;
  }

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  public long getTotalElements() {
    return totalElements;
  }

  public void setTotalElements(long totalElements) {
    this.totalElements = totalElements;
  }

  public int getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(int totalPages) {
    this.totalPages = totalPages;
  }

  public boolean isFirst() {
    return first;
  }

  public void setFirst(boolean first) {
    this.first = first;
  }

  public boolean isLast() {
    return last;
  }

  public void setLast(boolean last) {
    this.last = last;
  }

  public boolean isHasNext() {
    return hasNext;
  }

  public void setHasNext(boolean hasNext) {
    this.hasNext = hasNext;
  }

  public boolean isHasPrevious() {
    return hasPrevious;
  }

  public void setHasPrevious(boolean hasPrevious) {
    this.hasPrevious = hasPrevious;
  }
}
