# Examen Final — Automatización de Pruebas (IPLACEX)

Proyecto Maven que implementa un flujo completo de automatización de pruebas y
despliegue continuo, desarrollado como Examen Final de la asignatura
Automatización de Pruebas. El proyecto articula control de versiones con
flujo de ramas, integración continua con pruebas automatizadas en distintos
niveles, y un pipeline de despliegue con estrategia Blue-Green y rollback
automático.

Repositorio: https://github.com/Esteban-Dominguez-Ch/examen-final-automatizacion

## Descripción del proyecto

El sistema simula un dominio simple de gestión de usuarios, con las
siguientes clases principales en `src/main/java/com/examen/`:

- **Calculadora**: operaciones aritméticas básicas, usada para las pruebas
  unitarias.
- **UsuarioRepository**: almacenamiento en memoria de usuarios registrados.
- **UsuarioService**: lógica de negocio para registrar usuarios y consultar
  su información, apoyándose en `UsuarioRepository`. Esta dependencia entre
  clases es la que permite justificar pruebas de integración reales, más
  allá de una simple prueba unitaria aislada.

## Flujo de ramas

El proyecto usa **Trunk-Based Development**:

- `main`: rama principal, siempre desplegable.
- `feature/estructura-base`: rama de corta duración usada para desarrollar
  la estructura inicial del proyecto antes de integrarla a `main`.

## Estrategia de pruebas

El proyecto contempla tres niveles de prueba, cada uno con su propio
propósito y ubicados en paquetes separados dentro de `src/test/java/com/examen/`:

| Nivel | Paquete | Sufijo | Herramienta |
|---|---|---|---|
| Unitarias | `unit` | `*Test` | Surefire (Maven) |
| Integración | `integration` | `*IT` | Failsafe |
| Aceptación | `acceptance` | `*AT` | Failsafe |

**Pruebas unitarias** (`CalculadoraTest`): validan una unidad de código de
forma aislada, sin dependencias externas.

**Pruebas de integración** (`UsuarioServiceIT`): validan que `UsuarioService`
y `UsuarioRepository` funcionen correctamente en conjunto — registro de
usuarios, consulta de datos y manejo de duplicados.

**Pruebas de aceptación** (`UsuarioAceptacionAT`): validan el comportamiento
desde la perspectiva del negocio, de punta a punta: que un usuario recién
registrado quede disponible para consulta. Es el criterio que se exige antes
de autorizar un despliegue.

## Pipeline de CI (GitHub Actions)

Definido en `.github/workflows/ci.yml`, con dos jobs:

### Job `build-and-test`

Se ejecuta en cada push a `main` o a cualquier rama `feature/**`, y en cada
pull request hacia `main`:

1. Checkout del código.
2. Configuración de JDK 17.
3. Compilación del proyecto.
4. Ejecución de pruebas unitarias.
5. Ejecución de pruebas de integración.
6. Publicación de los reportes de Surefire/Failsafe como artefacto.

### Job `deploy`

Se ejecuta solo si `build-and-test` termina exitosamente y el push fue a
`main`:

1. Compilación de las clases de prueba.
2. Ejecución de las pruebas de aceptación.
3. Empaquetado del proyecto (`.jar`).
4. Determinación del slot activo e inactivo (estrategia Blue-Green).
5. Despliegue del artefacto al slot inactivo (ambiente de prueba).
6. Health check del nuevo slot.
7. Si el health check es exitoso: switch de tráfico al nuevo slot.
8. Si el health check falla: rollback automático, manteniendo activo el
   slot anterior sin interrupción de servicio.

## Estrategia de despliegue: Blue-Green con rollback

El pipeline mantiene dos slots (`blue` y `green`). El slot activo actual
queda registrado en `deploy/active-color.txt`. Cada nuevo despliegue va al
slot inactivo, nunca al que está sirviendo tráfico en ese momento. Solo se
promueve el nuevo slot (se actualiza `active-color.txt`) si el health check
es exitoso.

Para probar el mecanismo de rollback sin depender de una falla real, el
workflow incluye un disparador manual (`workflow_dispatch`) con la opción
**"Simular fallo del health check para probar el rollback"**, que fuerza el
resultado del health check a fallido y permite verificar que el pipeline
mantiene el slot anterior activo en vez de promover el nuevo.

## Instrucciones de ejecución local

Requisitos: JDK 17 y Maven.

```bash
# Compilar el proyecto
mvn compile

# Ejecutar pruebas unitarias
mvn test

# Ejecutar pruebas de integración
mvn failsafe:integration-test failsafe:verify -Dit.test=*IT

# Ejecutar pruebas de aceptación
mvn test-compile
mvn failsafe:integration-test failsafe:verify -Dit.test=*AT

# Empaquetar el proyecto
mvn package
```

## Evidencias

Las capturas de ejecución del pipeline (build, tests, despliegue exitoso y
rollback simulado) se encuentran en la carpeta `docs/screenshots/` y en el
documento de entrega `Esteban_Dominguez.docx`.