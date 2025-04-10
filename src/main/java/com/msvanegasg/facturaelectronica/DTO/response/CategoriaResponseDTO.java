package com.msvanegasg.facturaelectronica.DTO.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaResponseDTO {
	private Long id;
    private String nombre;
}
