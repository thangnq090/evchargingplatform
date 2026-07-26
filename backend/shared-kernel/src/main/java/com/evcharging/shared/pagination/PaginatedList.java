package com.evcharging.shared.pagination;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Lightweight paginated list response. Returned inside {@code ApiResponse}'s data field.
 *
 * <pre>
 * {
 *   "data": { "items": [...], "pagination": { "nextCursor": "...", "hasMore": true } },
 *   "success": true,
 *   "timestamp": "..."
 * }
 * </pre>
 */
public record PaginatedList<T>(List<T> items, Pagination pagination) {

  public static <T> PaginatedList<T> of(List<T> items, int limit, UUID lastId, boolean hasMore) {
    String next =
        lastId != null
            ? Base64.getUrlEncoder().withoutPadding().encodeToString(lastId.toString().getBytes())
            : null;
    return new PaginatedList<>(items, new Pagination(next, limit, hasMore));
  }

  public static <T> PaginatedList<T> empty(int limit) {
    return new PaginatedList<>(List.of(), new Pagination(null, limit, false));
  }

  /** Decodes a cursor to UUID. Returns null if invalid. */
  public static UUID decode(String cursor) {
    if (cursor == null || cursor.isBlank()) return null;
    try {
      return UUID.fromString(new String(Base64.getUrlDecoder().decode(cursor)));
    } catch (Exception e) {
      return null;
    }
  }

  public record Pagination(String nextCursor, int limit, boolean hasMore) {}
}
