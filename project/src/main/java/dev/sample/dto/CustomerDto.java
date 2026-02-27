package dev.sample.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// Redis 세션에 저장될 객체는 반드시 Serializable을 구현해야 합니다.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String role;
}
