# ADR-001: Clean Architecture basada en microservicios

## Status

Accepted

## Context

El proyecto debe evolucionar hacia un backend para facturacion electronica colombiana, POS electronico, inventario y contabilidad. El dominio fiscal y contable tiene reglas complejas, integraciones externas y cambios normativos frecuentes. El sistema tambien debe aislar al proveedor tecnologico DIAN para evitar acoplar la logica de negocio a un proveedor concreto.

## Decision

Usar Clean Architecture dentro de una estrategia basada en microservicios.

Cada microservicio separara:

- Dominio.
- Casos de uso.
- Puertos de entrada y salida.
- Adaptadores de infraestructura.
- Interfaces REST.

Los microservicios iniciales propuestos son:

- `billing-service`
- `dian-provider-service`
- `inventory-service`
- `accounting-service`
- `thirdparty-service`
- `catalog-service`
- `identity-service`
- `reporting-service`

## Alternatives considered

- MVC monolitico: menor complejidad inicial, pero alto acoplamiento para reglas fiscales y proveedor tecnologico.
- Monolito modular: recomendado como fase transitoria, pero no como objetivo final confirmado por el usuario.
- Microservicios sin Clean Architecture: permite despliegues separados, pero puede replicar acoplamiento framework/proveedor en cada servicio.

## Consequences

Positivas:

- Dominio fiscal mas testeable.
- Integracion DIAN desacoplada.
- Extraccion progresiva de servicios.
- Mejor separacion entre facturacion, inventario y contabilidad.

Negativas:

- Mayor complejidad de despliegue.
- Necesidad de contratos claros entre servicios.
- Mayor esfuerzo en observabilidad, seguridad y pruebas de contrato.

## Follow-up

Definir proveedor tecnologico especifico, contratos de integracion, estrategia de despliegue y politica transaccional entre facturacion, inventario y contabilidad.
