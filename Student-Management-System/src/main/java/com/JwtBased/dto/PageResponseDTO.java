package com.JwtBased.dto;

import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PageResponseDTO<T> {

    private List<T> content;        // Actual data
    private int pageNumber;         // Current page (0 based)
    private int pageSize;           // Records per page
    private long totalElements;     // Total records in DB
    private int totalPages;         // Total pages
    private boolean isFirst;        // First page ahe ka
    private boolean isLast;         // Last page ahe ka
    private boolean hasNext;        // Next page ahe ka
    private boolean hasPrevious;    // Previous page ahe ka
    private String keyword;         // Search keyword (optional)
}

