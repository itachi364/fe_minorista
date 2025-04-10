package com.msvanegasg.facturaelectronica.DTO.request;

import jakarta.validation.constraints.*;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AumentarStockRequestDTO {

	@NotNull
    private Long codigoBarras;

    @NotNull
    @Positive
    private Integer cantidadASumar;
}
