package com.leyou.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * jwt载荷实体类，不要写敏感信息，这里只是取出用来校验的
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

    private Long id;

    private String username;
}
