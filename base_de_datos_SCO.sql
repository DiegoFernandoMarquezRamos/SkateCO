-- 1. Activar extensiones necesarias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "postgis";

-- 2. Tabla de Usuarios (Identidad pública y rol)
CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(30) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    rol VARCHAR(20) NOT NULL DEFAULT 'USER' CHECK (rol IN ('USER', 'ADMIN')),
    fecha_registro TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 3. Tabla de Credenciales (Aislamiento 1:1 por seguridad en JPA)
CREATE TABLE credenciales_usuario (
    usuario_id UUID PRIMARY KEY REFERENCES usuarios(id) ON DELETE CASCADE,
    password_hash VARCHAR(255) NOT NULL,
    intentos_fallidos INT NOT NULL DEFAULT 0,
    bloqueado_hasta TIMESTAMPTZ NULL
);

-- 4. Tabla Principal de Spots (Núcleo geoespacial)
CREATE TABLE spots (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    creador_id UUID REFERENCES usuarios(id) ON DELETE SET NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    estado_suelo VARCHAR(30) CHECK (estado_suelo IN ('LISO', 'RUGOSO', 'AGRIETADO')),
    geometria GEOGRAPHY(Point, 4326) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PUBLICADO' CHECK (estado IN ('PENDIENTE', 'PUBLICADO', 'ARCHIVADO')),
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Índice espacial GiST para consultas rápidas por Bounding Box y ST_DWithin
CREATE INDEX idx_spots_geometria ON spots USING GIST (geometria);

-- 5. Catálogo Inmutable de Tipos de Obstáculo
CREATE TABLE tipos_obstaculo (
    id SMALLSERIAL PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    nombre_es VARCHAR(50) NOT NULL
);

-- 6. Obstáculos Físicos de cada Spot (Relación 1:N: 1 Spot tiene N obstáculos físicos)
CREATE TABLE obstaculos_spot (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    spot_id UUID NOT NULL REFERENCES spots(id) ON DELETE CASCADE,
    tipo_id SMALLINT NOT NULL REFERENCES tipos_obstaculo(id) ON DELETE RESTRICT,
    detalles VARCHAR(100) -- Ej: "8 escalones con caída limpia", "Bordillo encerado"
);

CREATE INDEX idx_obstaculos_spot_id ON obstaculos_spot(spot_id);
CREATE INDEX idx_obstaculos_tipo_id ON obstaculos_spot(tipo_id);

-- 7. Fotos del Spot almacenadas en Cloud Object Storage (S3 / IBM / Azure)
CREATE TABLE fotos_spot (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    spot_id UUID NOT NULL REFERENCES spots(id) ON DELETE CASCADE,
    object_key VARCHAR(255) NOT NULL UNIQUE,
    formato VARCHAR(15) NOT NULL DEFAULT 'image/webp',
    peso_bytes INT NOT NULL CHECK (peso_bytes > 0),
    fecha_subida TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 8. Validaciones Comunitarias (1 voto por usuario y spot, verificado por GPS)
CREATE TABLE validaciones_spot (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    spot_id UUID NOT NULL REFERENCES spots(id) ON DELETE CASCADE,
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    voto_positivo BOOLEAN NOT NULL,
    fecha_voto TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_validacion_usuario_spot UNIQUE (spot_id, usuario_id)
);

-- Datos semilla iniciales para el catálogo de obstáculos
INSERT INTO tipos_obstaculo (codigo, nombre_es) VALUES
    ('STAIRS', 'Escaleras'),
    ('HANDRAIL', 'Barandilla'),
    ('LEDGE', 'Bordillo / Cajón'),
    ('MANUAL_PAD', 'Manual Pad'),
    ('BANK', 'Plano Inclinado'),
    ('GAP', 'Gap / Salto'),
    ('BOWL', 'Bowl / Transición');

-- Inserción de 2 spots de prueba en Madrid (SRID 4326: Longitud Latitud)
INSERT INTO spots (nombre, descripcion, estado_suelo, geometria, estado) VALUES
    (
        'Plaza de Colón',
        'Mítico spot de street en el centro de Madrid con bordillos de granito y suelo pulido.',
        'LISO',
        ST_SetSRID(ST_MakePoint(-3.6900, 40.4250), 4326)::geography,
        'PUBLICADO'
    ),
    (
        'Skatepark Madrid Río',
        'Skatepark completo en Arganzuela con bowl, transiciones, planos inclinados y zona de street.',
        'LISO',
        ST_SetSRID(ST_MakePoint(-3.7042, 40.3955), 4326)::geography,
        'PUBLICADO'
    );

-- Obstáculos de prueba asociados a los spots
INSERT INTO obstaculos_spot (spot_id, tipo_id, detalles)
SELECT s.id, t.id, 'Bordillos de granito encerados y manual pad'
FROM spots s, tipos_obstaculo t
WHERE s.nombre = 'Plaza de Colón' AND t.codigo = 'LEDGE';

INSERT INTO obstaculos_spot (spot_id, tipo_id, detalles)
SELECT s.id, t.id, 'Bowl con diferentes alturas y curvas rápidas'
FROM spots s, tipos_obstaculo t
WHERE s.nombre = 'Skatepark Madrid Río' AND t.codigo = 'BOWL';
