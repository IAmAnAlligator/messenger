package com.jeannimi.messenger.dto;

import com.jeannimi.messenger.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

  private Long id;
  private String username;
  private Role role;
}
