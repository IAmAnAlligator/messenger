package com.jeannimi.messenger.adapter.in.web.mapper;

import com.jeannimi.messenger.application.common.pagination.Cursor;
import com.jeannimi.messenger.application.common.pagination.CursorPageQuery;
import com.jeannimi.messenger.common.pagination.CursorDto;
import com.jeannimi.messenger.common.pagination.CursorPageRequest;
import org.springframework.stereotype.Component;

@Component
public class CursorPaginationMapper {

  public CursorPageQuery toQuery(CursorPageRequest request) {
    return new CursorPageQuery(
        request.cursorId(),
        request.cursorTime(),
        request.limit());
  }

  public CursorDto toDto(Cursor cursor) {
    if (cursor == null) {
      return null;
    }

    return new CursorDto(
        cursor.time(),
        cursor.id());
  }
}