# Architecture

## Estilo seleccionado

Clean Architecture basada en microservicios.

## Principios

- Dominio independiente de Spring, JPA, HTTP y proveedor tecnologico.
- Casos de uso como centro de la aplicacion.
- Puertos de entrada para comandos y consultas.
- Puertos de salida para persistencia, proveedor tecnologico, mensajeria y otros servicios.
- Adaptadores intercambiables.
- Contratos API versionados.
- Secretos fuera del repositorio.

## Bounded contexts

- Identidad y seguridad.
- Terceros.
- Catalogos fiscales.
- Inventario.
- Facturacion electronica y POS.
- Integracion proveedor tecnologico DIAN.
- Contabilidad.
- Reportes.

## Comunicacion

Fase inicial:

- REST sincrono entre servicios.
- Idempotencia en operaciones fiscales.
- Correlation ID propagado.

Fase posterior:

- Eventos para `DocumentValidated`, `InventoryMovementRegistered`, `AccountingEntryPosted`.
- Broker de mensajeria si el volumen o resiliencia lo exige.

## Persistencia

- Base de datos por microservicio cuando se extraigan fisicamente.
- En fase transitoria, esquemas separados o modulos separados dentro del backend actual.
- Migraciones versionadas obligatorias.

## Despliegue sugerido

- `billing-service`
- `inventory-service`
- `accounting-service`
- `dian-provider-service`
- `thirdparty-service`
- `catalog-service`
- `identity-service`
- PostgreSQL por servicio o por esquema en fase inicial.

## Recomendacion de migracion incremental

1. Corregir configuracion sensible.
2. Introducir paquetes Clean Architecture dentro del proyecto actual.
3. Implementar `billing` como modulo nuevo sin romper CRUD existente.
4. Refactorizar modulos CRUD existentes de forma incremental hacia la misma estructura usada por `billing`.
5. Usar `Categoria` como piloto de refactor por ser un modulo pequeno y de bajo riesgo.
6. Separar `dian-provider` como servicio o modulo independiente.
7. Extraer inventario y contabilidad cuando contratos esten estables.
8. Mantener pruebas de contrato durante la extraccion.

## Refactorizacion de modulos existentes

Los paquetes legacy `controller`, `service`, `repository`, `models`, `DTO` y `mappers` se migraran gradualmente hacia bounded contexts alineados con Clean Architecture.

Orden recomendado:

1. `catalog`: `Categoria`, `Producto`, `Impuesto`, `Pais`, `MetodoPago`, `TipoDocumento`, `TipoGasto`.
2. `thirdparty`: `Cliente`, `Proveedor`.
3. `inventory`: stock, compras, productos inventariables y movimientos.
4. `billing`: facturas, POS, numeracion, proveedor DIAN y documentos electronicos.
5. `accounting`: PUC, asientos, libro diario y libro mayor.
6. `audit`: auditoria y registro de accesos.

Reglas de migracion:

- Mantener compatibilidad de endpoints existentes salvo aprobacion explicita.
- Migrar primero comportamiento a casos de uso y puertos.
- Mantener adaptadores JPA como detalle de infraestructura.
- Agregar pruebas antes o durante cada refactor para fijar comportamiento.
- No mezclar refactor arquitectonico con nuevas reglas de negocio.

## Riesgos arquitectonicos

- Microservicios prematuros pueden aumentar complejidad operacional.
- La integracion con proveedor tecnologico depende de contrato comercial y documentacion especifica.
- La normatividad cambia y requiere mantenimiento continuo.
- El modelo contable debe ser validado por contador.
