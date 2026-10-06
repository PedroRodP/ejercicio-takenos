# Investment Position API

Servidor Quarkus (Java 21 + Gradle) que simula compra, venta y posición de un usuario sobre una cuenta de inversión.
Todo el estado vive en memoria: al reiniciar la aplicación se pierde.

Estado actual: **compra (`buy`)**, **venta (`sell`)** y **posición (`position`)**.

## Requisitos

- Java 21
- No requiere Gradle instalado (se usa `./gradlew`)

## Cómo levantarlo

```bash
./gradlew quarkusDev
```

La API queda en `http://localhost:8080`. Swagger UI en `http://localhost:8080/q/swagger-ui`.

## Tests

```bash
./gradlew test            # unitarios (Spock)
./gradlew acceptanceTest  # integración (@QuarkusTest + RestAssured sobre los endpoints)
```

- Unitarios: `src/test/groovy` (Spock, sufijo `Test`).
- Integración: `src/acceptanceTest/groovy` (sufijo `TestCase`, agrupados por resource). `CommonTestCase` limpia el
  repositorio en memoria después de cada test.

## Endpoints

### Compra — `POST /v1/users/{userId}/purchases`

Request:

```json
{
  "amount_usd": 100,
  "price_per_unit": 25,
  "reference": "payin:abc123"
}
```

Response `201 Created`:

```json
{
  "id": "6f1c2a7e-...",
  "user_id": "user-1",
  "amount_usd": 100,
  "units": 4.00000000,
  "remaining_units": 4.00000000,
  "price_per_unit": 25,
  "reference": "payin:abc123",
  "created": "2026-10-06T12:00:00Z"
}
```

Errores de validación devuelven `400` con un `ErrorTO` (`title`, `status`, `detail`, `sub_errors`).

### Venta — `POST /v1/users/{userId}/sales`

Request:

```json
{
  "units": 12,
  "price_per_unit": 25
}
```

Response `201 Created` (habiendo comprado antes 10 units @ 10 y 5 units @ 20):

```json
{
  "id": "0b8e1d52-...",
  "user_id": "user-1",
  "units": 12,
  "price_per_unit": 25,
  "proceeds_usd": 300,
  "cost_usd": 140.00000000,
  "realized_profit_usd": 160.00000000,
  "consumed_purchases": [
    { "purchase_id": "<compra 1>", "units": 10.00000000, "price_per_unit": 10, "cost_usd": 100.00000000 },
    { "purchase_id": "<compra 2>", "units": 2.00000000, "price_per_unit": 20, "cost_usd": 40.00000000 }
  ],
  "created": "2026-10-06T12:05:00Z"
}
```

Si el usuario no tiene suficientes units devuelve `422` con un `ErrorTO` y no consume nada.

### Posición — `GET /v1/users/{userId}/position`

Response `200 OK` (después del escenario FIFO de abajo: compras 10 @ 10 y 5 @ 20, venta 12 @ 25):

```json
{
  "user_id": "user-2",
  "units": 3.00000000,
  "cost_usd": 60.00000000,
  "realized_profit_usd": 160.00000000
}
```

- `units`: units que el usuario tiene ahora.
- `cost_usd`: cuánto le costaron esas units.
- `realized_profit_usd`: cuánto ganó (positivo) o perdió (negativo) con lo que ya vendió.

## Comandos curl

```bash
# Compra válida: 100 USD a 25 USD/unit -> 4 units
curl -i -X POST http://localhost:8080/v1/users/user-1/purchases \
  -H 'Content-Type: application/json' \
  -d '{"amount_usd": 100, "price_per_unit": 25, "reference": "payin:abc123"}'

# Compra con división no exacta: 100 USD a 3 USD/unit -> 33.33333333 units
curl -i -X POST http://localhost:8080/v1/users/user-1/purchases \
  -H 'Content-Type: application/json' \
  -d '{"amount_usd": 100, "price_per_unit": 3, "reference": "transfer:9f2"}'

# Reintento con la misma reference (se registra otra compra, ver suposiciones)
curl -i -X POST http://localhost:8080/v1/users/user-1/purchases \
  -H 'Content-Type: application/json' \
  -d '{"amount_usd": 100, "price_per_unit": 25, "reference": "payin:abc123"}'

# Request inválido -> 400
curl -i -X POST http://localhost:8080/v1/users/user-1/purchases \
  -H 'Content-Type: application/json' \
  -d '{"amount_usd": 0, "price_per_unit": -1, "reference": ""}'
```

### Escenario FIFO completo

```bash
# 1. Compra 10 units @ 10
curl -i -X POST http://localhost:8080/v1/users/user-2/purchases \
  -H 'Content-Type: application/json' \
  -d '{"amount_usd": 100, "price_per_unit": 10, "reference": "payin:fifo-1"}'

# 2. Compra 5 units @ 20
curl -i -X POST http://localhost:8080/v1/users/user-2/purchases \
  -H 'Content-Type: application/json' \
  -d '{"amount_usd": 100, "price_per_unit": 20, "reference": "payin:fifo-2"}'

# 3. Vende 12 units @ 25 -> consume 10 de la compra 1 y 2 de la compra 2; ganancia 300 - 140 = 160
curl -i -X POST http://localhost:8080/v1/users/user-2/sales \
  -H 'Content-Type: application/json' \
  -d '{"units": 12, "price_per_unit": 25}'

# 4. Vende 4 units más -> solo quedan 3 -> 422
curl -i -X POST http://localhost:8080/v1/users/user-2/sales \
  -H 'Content-Type: application/json' \
  -d '{"units": 4, "price_per_unit": 25}'

# 5. Posición -> 3 units (de la compra 2) con costo 60 y ganancia realizada 160
curl -i http://localhost:8080/v1/users/user-2/position
```

## Suposiciones

### Generales

- **Persistencia en memoria**: las operaciones se guardan en estructuras de datos del dominio
  (`ConcurrentHashMap<userId, List<Purchase>>`). No hay base de datos ni conexiones externas.
- **Un único activo**: la cuenta de inversión tiene un solo instrumento, por eso las operaciones no indican qué activo
  se compra/vende.
- **Usuarios**: no existe un registro de usuarios. Cualquier `userId` en el path es válido y su cuenta se crea
  implícitamente con la primera compra.
- **Precisión numérica**: se usa `BigDecimal` en todos los montos, precios y units (nunca `double`).
- **Diseño REST**: las operaciones cuelgan del usuario (`/v1/users/{userId}/...`). El `userId` viaja en el path y
  el resto de los parámetros en el body.
- **JSON en snake_case**, siguiendo las convenciones de Utoppia.

### Compra

- **Cálculo de units**: `units = amount_usd / price_per_unit`, con **8 decimales** y redondeo **hacia abajo**
  (`RoundingMode.DOWN`), para nunca asignar al usuario más units de las que pagó. El `amount_usd` se guarda tal cual
  llegó, de modo que el costo de la compra es exactamente lo invertido.
- **Validaciones**: `amount_usd` y `price_per_unit` son obligatorios y deben ser mayores a 0; `reference` es
  obligatoria y no puede estar vacía.
- **Cada compra se registra como un `Purchase` independiente** (id, userId, amountUsd, units, pricePerUnit,
  reference, created, remainingUnits) y se mantiene el orden cronológico por usuario, que es el que usa la venta
  para aplicar FIFO.
- **Idempotencia por `reference`**: conceptualmente la `reference` identifica el hecho que originó la compra y es la
  clave para validar idempotencia ante reintentos del sistema que nos llama (una segunda llamada con la misma
  reference debería devolver la compra original sin registrar una nueva). **Para este ejercicio NO se implementa ese
  chequeo**: cada llamada registra una compra nueva aunque la reference se repita.
- **Cada compra es un lote** con `remaining_units`, que arranca igual a `units` y baja a medida que las ventas lo
  consumen. La respuesta de la compra lo incluye.

### Venta

- **FIFO**: una venta consume primero las compras más antiguas del usuario (orden de llegada). Una venta puede
  consumir varias compras y dejar la última consumida parcialmente; las compras ya agotadas se saltean.
- **Costo de lo vendido**: por cada compra consumida, `cost_usd = units consumidas × price_per_unit de esa compra`.
  Por el redondeo de units en la compra, el costo puede diferir en fracciones de centavo del `amount_usd` original.
- **Ganancia realizada por venta**: `realized_profit_usd = proceeds_usd - cost_usd`, donde
  `proceeds_usd = units × price_per_unit` de la venta. Puede ser negativa (pérdida). Se guarda en cada `Sale`
  junto con el detalle de compras consumidas (`consumed_purchases`) para trazabilidad.
- **Sin comisiones ni impuestos**: el producido de la venta es exactamente `units × price_per_unit`.
- **Todo o nada**: si el usuario no tiene suficientes units, la venta se rechaza (`422`) sin consumir ninguna compra.
  No se permiten ventas en corto.
- **Concurrencia**: `SaleService.sell` es `synchronized` para que dos ventas simultáneas no consuman las mismas units.
  Es un lock global, suficiente para el ejercicio (en un sistema real sería por usuario o transaccional en la base).
- **Sin idempotencia en ventas**: el enunciado no define una `reference` para `sell`, así que un reintento registra
  otra venta.
- **Validaciones mínimas**: `units` y `price_per_unit` obligatorios y mayores a 0; no se cubren otros casos de error.
- **JSON**: los `BigDecimal` se serializan en notación plana (`0.00000000` en vez de `0E-8`).

### Posición

- **Se calcula al vuelo** a partir de lo guardado en purchases y sales; no se persiste una entidad `Position`.
- **Units actuales**: suma de `remaining_units` de todas las compras del usuario (lo que FIFO todavía no consumió).
- **Costo**: suma de `remaining_units × price_per_unit` de cada compra, con el mismo criterio de costo que usa la
  venta. Así, `costo de las compras = costo actual + costo de lo vendido`.
- **Ganancia/pérdida**: suma de `realized_profit_usd` de todas las ventas del usuario. Es solo la ganancia
  **realizada**; no se calcula ganancia no realizada porque no hay un precio de mercado actual.
- **Usuario sin operaciones**: devuelve `200` con todo en 0 (no `404`), consistente con que los usuarios no se
  registran explícitamente.
- **Consistencia**: la lectura no toma el lock de la venta, así que una consulta concurrente con una venta podría ver
  un estado intermedio. Aceptable para el ejercicio.
