# ADR-007 — Quién posee los límites del plan: el `tenant-service`

- **Estado:** Aceptado
- **Fecha:** 2026-10-22
- **Change:** `harden-gateway-edge`
- **Ámbito:** `erp-tenant-service` (propietario) y `erp-api-gateway` (consumidor)
- **Precedente:** `ADR-002` (quién posee la numeración de comprobantes y quién ejecuta la anulación)
- **Copia canónica:** `erp-tenant-service/docs/adr/ADR-007-quien-posee-los-limites-del-plan.md` (este repositorio; los demás guardan un espejo)

## Contexto

El **plan** —BASICO, PRO, ENTERPRISE— es un concepto **comercial**: define lo que un taller paga y lo que la plataforma le promete. El `tenant-service` es su dueño desde el principio: el catálogo le asigna *"registro de talleres, planes/límites, branding"*, y su modelo de plan guarda ya los límites de negocio del contrato (`max_users`, `max_vehicles`, `max_iot_msg_per_day`), con su tabla `plans`, su semilla de los tres planes y su endpoint interno `GET /api/v1/internal/tenants/{id}/limits`, que existe precisamente para que el gateway aplique las cuotas.

**Falta lo mismo para el tráfico.** Los límites de tráfico —peticiones por minuto y conexiones concurrentes por taller— **no existen en el modelo de plan**: el gateway **se los inventa**, con un `switch` sobre el código de plan (`TenantServiceClientAdapter.resolveMaxReqPerMin` y `resolveMaxConcurrentSlots`: BASICO 100/15, PRO 500/40, ENTERPRISE 2000/100).

Eso es **una segunda copia de la promesa comercial**, y el hallazgo 6.9 la denuncia con el nombre exacto: *«duplicación de la fuente de verdad, con deriva garantizada»*. El día que el tenant-service cambie el plan —y los planes se cambian, es la operación comercial más frecuente de un SaaS— el gateway seguirá aplicando el valor viejo, **sin avisar**: la plataforma PROMETERÁ una cosa y LÍMITARÁ otra, y el desvío se verá en las quejas de los talleres, no en los registros.

El precedente que lo resuelve ya existe: el **ADR-002** decidió quién posee la numeración de los comprobantes fiscales y quién ejecuta la anulación, con el mismo argumento —el dueño del dato es el que puede cambiarlo, y no puede haber dos dueños—.

## Decisión

**1. Los límites de tráfico son parte del plan, y el `tenant-service` los posee.** Se guardan en el modelo de plan (`max_requests_per_minute` y `max_concurrent_slots`), junto a `max_users`, `max_vehicles` y `max_iot_msg_per_day`, que son el mismo tipo de cosa: lo que el plan promete.

**2. Se exponen en el endpoint que el gateway ya consume.** `GET /api/v1/internal/tenants/{id}/limits` devuelve los dos límites nuevos. No hay ningún endpoint nuevo, ni otro contrato interno, ni otra credencial: el gateway sigue consultando con el token del llamante.

**3. El gateway los consume y no los deduce.** El `switch` por código de plan desaparece del gateway. Si los campos no llegan —un despliegue desordenado, o un rollback del tenant-service—, el gateway **cae al límite más bajo** (BASICO, 100 peticiones por minuto y 15 conexiones concurrentes): **fallar hacia abajo**, nunca hacia arriba.

**4. Los valores no cambian.** Los de la tabla son los que el gateway aplica hoy, para que el cambio no altere ningún comportamiento observable.

## Motivo

**El dueño de un dato comercial es el que puede cambiarlo, y no puede haber dos dueños.** El plan se vende, se actualiza y se escala desde el tenant-service. Que el gateway tenga su propia tabla de valores por plan no es una redundancia inocua: es una **segunda definición** de la misma promesa, escrita en otro lenguaje, en otro repositorio, con su propia forma de equivocarse —un `switch` con un `default` silencioso en lugar de un valor por plan—.

**Mover la traducción al gateway tampoco vale.** Que el plan siga siendo del tenant-service pero que su traducción a tráfico sea del gateway deja **dos dueños de la misma promesa**: el plan dice PRO y el gateway decide qué significa PRO en peticiones por minuto. El día que el negocio quiera "PRO = 800/min", hay que acordarlo entre dos servicios, y el primero que se despliegue manda.

**Mover los valores a la configuración del gateway no arregla nada.** Convierte código duplicado en **configuración duplicada**, y el cambio de un plan sigue exigiendo tocar dos repos. El coste de la opción barata es bajo; el de la **deuda de la deriva** es exactamente el que este ADR viene a cerrar.

**El coste de la opción correcta es bajo de verdad.** Dos columnas y su semilla, dos campos en un DTO, dos campos en una respuesta, dos atributos en el dominio, y la eliminación de un `switch`. No hay tabla nueva, ni endpoint nuevo, ni contrato nuevo, ni migración que bloquee el arranque: el tenant-service despliega primero, el gateway después, y cada orden funciona.

## Alternativas descartadas

- **Que el gateway siga deduciéndolos del código de plan** (lo que hay hoy). Se descarta por la duplicación de la fuente de verdad y por la deriva silenciosa que el hallazgo 6.9 ya denuncia.
- **Que el gateway los tenga en su propia configuración**, leyendo el plan del tenant-service solo para el nombre. Se descarta porque convierte el código duplicado en configuración duplicada y deja la deuda de la deriva intacta.
- **Que el plan siga siendo del tenant-service y su traducción a tráfico sea del gateway.** Se descarta porque deja dos dueños de la misma promesa comercial.
- **Un servicio de planes aparte**, que posee los límites y los reparte. Se descarta por sobreingeniería: hay tres planes y ninguna señal de que el catálogo de límites crezca.

## Consecuencias

**Positivas.**

- **Una sola fuente de verdad** para lo que la plataforma promete por plan, y el gateway deja de tener valores de negocio hardcodeados.
- **Cambiar un plan es un cambio en un sitio**: se cambia el plan en el tenant-service y el gateway lo aplica sin despliegue propio.
- El endpoint interno que el gateway ya consume **crece en lugar de multiplicarse**: no hay un contrato nuevo que mantener.

**Negativas.**

- El `tenant-service` queda en el **camino crítico de la cuota del borde** —que ya lo estaba, por el estado del taller—, así que su disponibilidad importa más: por eso el gateway **falla hacia abajo** cuando los límites no llegan, y por eso el cambio 6.31 lo pone en modo cerrado.
- El **orden de despliegue importa**: el tenant-service primero. En el orden inverso, el gateway funciona con el límite de BASICO hasta que el tenant-service esté desplegado, que es el comportamiento seguro.

**Neutrales.**

- **No hay cambio de comportamiento observable**: los valores son los de hoy.
- La decisión se revisa si los planes dejan de ser un conjunto fijo —acuerdos por taller, cuotas negociadas—: en ese caso los límites podrían tener que vivir en el **taller** y no en el plan.

## Condiciones de revisión

Esta decisión se revisa si:

1. Los planes dejan de ser un catálogo fijo y pasan a ser **límites negociados por taller**: entonces el límite probablemente deba vivir en la fila del taller, no en el plan.
2. Aparece un **consumo de las cuotas que no pase por el gateway** (por ejemplo, un consumidor directo del tenant-service): entonces habría que decidir si el contador también se centraliza, que es una decisión distinta y mayor.
