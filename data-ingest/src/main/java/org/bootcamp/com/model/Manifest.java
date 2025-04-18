package org.bootcamp.com.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class Manifest {
    @NotBlank
    private String checksum;

    @NotNull
    @Positive
    private Long recordCount;
}
