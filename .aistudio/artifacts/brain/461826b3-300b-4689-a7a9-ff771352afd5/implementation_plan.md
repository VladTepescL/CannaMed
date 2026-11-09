# CannaMed: Control Medicinal de Cannabis

Aplicación nativa para Android diseñada para el registro, seguimiento clínico y control terapéutico del uso de cannabis medicinal. Permite a los pacientes registrar con precisión sus tomas en cualquier formato de administración, controlar porcentajes de cannabinoides y terpenos en aceites y cartuchos, adjuntar recetas y documentos médicos de forma 100% privada y local, consultar literatura de cepas tipo Leafly, visualizar métricas de alivio y generar reportes clínicos para su médico tratante.

## User Review & Critical Decisions

> [!IMPORTANT]
> Decisiones confirmadas por el usuario:
> - **Almacenamiento y Privacidad**: 100% Local en el dispositivo mediante base de datos SQLite con Room. Los datos médicos y prescripciones no se envían a servidores externos.
> - **Documentos y Recetas**: Selector de archivos integrado para fotos y documentos PDF (mediante Storage Access Framework / Photo Picker seguro de Android) con visor en la propia app y almacenamiento local en sandbox privado.
> - **Estilo Visual**: Modo oscuro terapéutico con paleta esmeralda (`#0D1512`, `#131F1B`, acentos `#10B981` y `#34D399`), tipografía legible y controles con objetivos táctiles amplios (≥48dp).

---

## 1. Overview & Core Concept

- **Propósito**: Facilitar a pacientes en tratamiento con cannabis medicinal el control cuantitativo y cualitativo de su terapia: dosis exactas, métodos de administración, composición química (cannabinoides y terpenos), notas de alivio de síntomas, control de consumibles (pods/cartuchos) y documentación clínica.
- **Público Objetivo**: Pacientes de cannabis medicinal, personas con dolor crónico, insomnio, ansiedad u otras condiciones clínicas prescritas, y sus médicos especialistas que requieren datos fidedignos de la respuesta terapéutica.
- **Valor Diferencial**:
  - Soporte para **todos los formatos**: Vaporizado de flor seca, gotas/tinturas sublinguales, vaporizado concentrado/cartucho (510/pod), aceite clear (con cálculo de mg y % por ml), comestibles/cápsulas, fumado (pipa, papel/joint, bong), tópicos, y dabbing.
  - Para aceites y cartuchos: seguimiento de terpenos principales (Mirceno, Limoneno, Cariofileno, Pineno, etc.), cantidad de caladas acumuladas, fecha de cambio de cápsula y cálculo de consumo.
  - Generador de **Reporte Médico Clínico** exportable e imprimible para la consulta con el especialista.
  - Biblioteca de cepas con perfiles de cannabinoides, efectos, aromas, terpenos y enlaces directos de literatura a Leafly.com.

---

## 2. User Experience & Visual Design

### Navegación y Vistas Principales (Navigation Bar M3)
1. **Diario de Dosis (Dashboard & Registro)**:
   - Resumen del día: dosis tomadas, última toma, método preferido, nivel medio de alivio.
   - Botón de Registro Rápido con selector visual de formato y cantidad.
   - Historial cronológico con tarjetas detalladas (formato, cepa, caladas/gotas, notas subjetivas, calificación de alivio 1-10).
2. **Formatos & Consumibles (Cartuchos & Aceite Clear)**:
   - Panel de control para pods/cartuchos activos: contador de caladas, fecha de instalación/cambio de cápsula, capacidad restante estimada.
   - Ficha técnica de formulación: % y mg/ml de THC, CBD, CBN, CBG y desglose de terpenos.
3. **Literatura & Cepas (Leafly Knowledge Base)**:
   - Catálogo interactivo de variedades (Indica, Sativa, Híbrida) con perfiles de efectos, condiciones médicas sugeridas, terpenos dominantes y botón "Ver en Leafly.com".
   - Filtro por síntoma (ej. Dolor, Insomnio, Ansiedad, Espasmos) y tipo de terpeno.
4. **Recetas & Documentación Médica**:
   - Gestor de prescripciones médicas oficiales, recetas y certificados de análisis de laboratorio (COA).
   - Visor de imágenes y documentos, datos del médico tratante, vigencia y dosificación recomendada.
5. **Analíticas & Reporte Médico**:
   - Gráficos interactivos en Canvas: evolución del alivio de síntomas en el tiempo, frecuencia por formato y correlación dosis-efecto.
   - Generador y exportador de Reporte Clínico en texto formateado e imprimible (compatible con PDF y ShareSheet para enviar a médico).

### Identidad Visual: Modo Oscuro Terapéutico Esmeralda
- **Fondo Base (`background`)**: `#0D1512` (Verde noche profundo, relajante y orgánico).
- **Superficie de Tarjetas (`surface`)**: `#13221C` con bordes sutiles `#1C352B`.
- **Color Primario (`primary`)**: `#10B981` (Verde esmeralda médico vivo).
- **Acento Secundario (`secondary`)**: `#34D399` (Menta suave para métricas positivas y estados de alivio).
- **Acento Terciario (`tertiary`)**: `#F59E0B` (Ámbar dorado para cannabinoides y niveles de advertencia).
- **Texto Principal**: `#F1F5F9`, **Texto Secundario**: `#94A3B8`.
- **Componentes**: Tarjetas redondeadas (`16.dp`), chips de terpenos codificados por color, barras de progreso fluidas y botones de acción de 48dp mínimos.

---

## 3. Key Product Decisions & Trade-Offs

- **Almacenamiento Local con Room**:
  - *Decisión*: Room (SQLite) local en el dispositivo con entidades normalizadas para tomas, cartuchos, cepas y recetas.
  - *Razón*: Privacidad médica estricta elegida por el usuario; funciona 100% sin conexión a internet y no requiere registro ni credenciales.
- **Gestión Segura de Archivos**:
  - *Decisión*: Copia de imágenes y documentos seleccionados a la carpeta interna privada `context.filesDir/medical_docs/` usando URI de Android Photo Picker y Storage Access Framework.
  - *Razón*: Cumple con las políticas de Google Play sin solicitar permisos invasivos de almacenamiento global (`READ_EXTERNAL_STORAGE`).
- **Exportación e Impresión del Reporte**:
  - *Decisión*: Generador de reporte clínico estructurado con opción de compartir vía `Intent.ACTION_SEND` e impresión directa con `PrintManager` (que genera PDF en cualquier teléfono Android).
  - *Razón*: Máxima compatibilidad en cualquier dispositivo sin añadir librerías externas pesadas.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        CannaMed User Interface                         │
│  ┌─────────────────┬──────────────────┬─────────────────┬───────────┐  │
│  │   Dosis & Toma  │  Formatos & Pods │ Literatura/Leaf │ Analítica │  │
│  │   (Quick Log)   │  (Clear Oil)     │ (Strains & Terp)│ & Reporte │  │
│  └────────┬────────┴────────┬─────────┴────────┬────────┴─────┬─────┘  │
│           │                 │                  │              │        │
│           ▼                 ▼                  ▼              ▼        │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                     CannaMedViewModel                            │  │
│  │  - dosageLogs: StateFlow<List<DosageLog>>                        │  │
│  │  - activeCartridges: StateFlow<List<Cartridge>>                  │  │
│  │  - prescriptions: StateFlow<List<Prescription>>                  │  │
│  │  - strainLibrary: StateFlow<List<StrainInfo>>                    │  │
│  │  - analyticsSummary: StateFlow<AnalyticsReport>                  │  │
│  └──────────────────────────────┬───────────────────────────────────┘  │
└─────────────────────────────────┼──────────────────────────────────────┘
                                  ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        Data & Repository Layer                         │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                     CannaMedRepository                           │  │
│  │  - Reactive Flow queries, suspend CRUD operations                │  │
│  │  - Document file management in internal storage                  │  │
│  │  - Leafly catalog pre-population & search queries                │  │
│  └──────────────────────────────┬───────────────────────────────────┘  │
│                                 ▼                                      │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │              Room Database (AppDatabase v1)                      │  │
│  │  - DosageLogDao: logs con dosis, formato, cepa, caladas, alivio  │  │
│  │  - CartridgeDao: cartuchos/clear oil, fecha cambio, % terps/mg   │  │
│  │  - PrescriptionDao: recetas, médico, fecha vigencia, archivo     │  │
│  │  - StrainDao: catálogo precargado y cepas personalizadas         │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

### Entidades de Datos Clave
1. **`DosageLog`**: `id`, `timestamp`, `format` (Vaporizado flor, gotas/tintura, vaporizado concentrado, aceite clear, edible, pipa, papel/joint, bong, etc.), `strainName`, `strainType`, `dosageValue`, `dosageUnit` (gotas, mg, puffs/caladas, ml, gr), `puffsCount`, `reliefScore` (1-10), `treatedSymptoms` (lista/cadena), `moodEffect`, `sideEffects`, `subjectiveNotes`, `cartridgeId` (opcional).
2. **`Cartridge`**: `id`, `brandName`, `varietyName`, `cannabinoidsMgPerMl` (THC, CBD, CBN, CBG), `terpenesPercentage` (Mirceno, Limoneno, etc.), `totalPuffs`, `installedDate`, `isFinished`, `finishedDate`, `notes`.
3. **`Prescription`**: `id`, `doctorName`, `licenseNumber`, `issueDate`, `expiryDate`, `prescribedDosage`, `notes`, `localFilePath`, `fileMimeType`.
4. **`StrainInfo`**: `id`, `name`, `type` (Indica, Sativa, Hybrid), `thcRange`, `cbdRange`, `dominantTerpenes`, `medicalBenefits`, `leaflyUrl`.

---

## Verificación y Próximos Pasos
- Implementar los modelos de Room con convertidores de tipos.
- Construir los componentes de UI en Jetpack Compose con tema modo oscuro esmeralda.
- Crear el diálogo y pantalla de registro detallado con todos los formatos solicitados.
- Añadir el visor y selector de recetas/documentos.
- Integrar la biblioteca con enlaces a Leafly y perfiles de terpenos.
- Probar la compilación completa mediante `compile_applet`.
