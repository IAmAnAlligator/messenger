package com.jeannimi.messenger.adapter.in.web.mapper;

import com.jeannimi.messenger.adapter.in.web.pagination.CursorDto;
import com.jeannimi.messenger.adapter.in.web.pagination.CursorPageRequest;
import com.jeannimi.messenger.application.common.pagination.Cursor;
import com.jeannimi.messenger.application.common.pagination.CursorPageQuery;
import com.jeannimi.messenger.domain.common.DomainId;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@Component
public class CursorPaginationMapper {

  public <ID extends DomainId> CursorPageQuery<ID> toQuery(
      CursorPageRequest request,
      Function<UUID, ID> idFactory) {

    ID cursorId =
        request.cursorId() == null
            ? null
            : idFactory.apply(request.cursorId());

    return new CursorPageQuery<>(
        cursorId,
        request.cursorTime(),
        request.limit());
  }

  public CursorDto toDto(Cursor<? extends DomainId> cursor) {

    if (cursor == null) {
      return null;
    }

    return new CursorDto(
        cursor.time(),
        cursor.id().value());
  }
}
