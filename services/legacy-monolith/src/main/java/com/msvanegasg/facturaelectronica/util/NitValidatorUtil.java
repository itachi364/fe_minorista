package com.msvanegasg.facturaelectronica.util;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NitValidatorUtil {
	private static final Logger logger = LoggerFactory.getLogger(NitValidatorUtil.class);

	/**
	 * Valida si un NIT es válido para tipo de documento 31 o 50 (jurídicos).
	 *
	 * @param tipoDocumentoCodigo Código del tipo de documento
	 * @param numeroDocumento     Número del documento (NIT)
	 * @param digitoVerificacion  (Opcional) Dígito de verificación, si se
	 *                            proporciona
	 * @return true si el NIT es válido
	 */
	public static boolean esNitValido(Long tipoDocumentoCodigo, Long numeroDocumento,
			Optional<Integer> digitoVerificacion) {
		Logger logger = LoggerFactory.getLogger(NitValidatorUtil.class);

		if (tipoDocumentoCodigo == null || numeroDocumento == null) {
			logger.warn("Tipo de documento o número de documento es nulo.");
			return false;
		}

// Solo aplica para NIT (31 o 50)
		if (tipoDocumentoCodigo != 31 && tipoDocumentoCodigo != 50) {
			return true; // No es persona jurídica, no aplica DV
		}

		String nitStr = numeroDocumento.toString();

// Validación de longitud
		if (nitStr.length() < 9 || nitStr.length() > 10) {
			logger.warn("Longitud del NIT inválida: {}", nitStr.length());
			return false;
		}

// El dígito de verificación es obligatorio para NIT
		if (digitoVerificacion.isEmpty()) {
			logger.warn("Dígito de verificación no proporcionado para NIT: {}", numeroDocumento);
			return false;
		}

		long dvCalculado = calcularDigitoVerificacion(numeroDocumento);
		boolean esValido = dvCalculado == digitoVerificacion.get();

		if (!esValido) {
			logger.warn("Dígito verificación inválido. Esperado: {}, Recibido: {}", dvCalculado,
					digitoVerificacion.get());
		} else {
			logger.info("Dígito verificación correcto para NIT {}", numeroDocumento);
		}

		return esValido;
	}

	/**
	 * Cálculo del dígito de verificación usando el algoritmo oficial DIAN.
	 * https://www.dian.gov.co/ (Método ponderado con tabla de factores primos)
	 */
	private static long calcularDigitoVerificacion(Long numero) {
		Logger logger = LoggerFactory.getLogger(NitValidatorUtil.class);

		int[] pesos = { 71, 67, 59, 53, 47, 43, 41, 37, 29, 23, 19, 17, 13, 7, 3 };
		String numeroStr = numero.toString();

		logger.info("Calculando dígito de verificación para NIT: {}", numero);

		long suma = 0;
		int longitud = numeroStr.length();
		for (int i = 0; i < longitud; i++) {
			int digito = Character.getNumericValue(numeroStr.charAt(i));
			int peso = pesos[pesos.length - longitud + i]; // Alinea pesos con la longitud real
			int producto = digito * peso;
			suma += producto;

			logger.debug("Índice: {}, Dígito: {}, Peso: {}, Producto: {}, Suma parcial: {}", i, digito, peso, producto,
					suma);
		}

		long residuo = suma % 11;
		long digitoVerificacion = (residuo > 1) ? 11 - residuo : residuo;

		logger.info("Suma total: {}, Residuo: {}, Dígito de verificación calculado: {}", suma, residuo,
				digitoVerificacion);

		return digitoVerificacion;
	}

}
