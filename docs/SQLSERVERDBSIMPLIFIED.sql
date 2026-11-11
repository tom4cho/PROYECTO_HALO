CREATE TABLE [RAMAS] (
    [id_rama]     INT IDENTITY(1,1) NOT NULL,
    [nombre]      NVARCHAR(60) NOT NULL,
    [descripcion] NVARCHAR(MAX) NULL,
    CONSTRAINT [PK_RAMAS] PRIMARY KEY ([id_rama])
);
GO

CREATE TABLE [DIVISIONES] (
    [id_division] INT IDENTITY(1,1) NOT NULL,
    [id_rama]     INT NULL,
    [nombre]      NVARCHAR(60) NOT NULL,
    [descripcion] NVARCHAR(MAX) NULL,
    CONSTRAINT [PK_DIVISIONES] PRIMARY KEY ([id_division]),
    CONSTRAINT [FK_ramas_divisiones] FOREIGN KEY ([id_rama])
        REFERENCES [RAMAS]([id_rama])
);
GO

CREATE TABLE [DIVISIONES_ESPECIALES] (
    [id_division_especial] INT IDENTITY(1,1) NOT NULL,
    [nombre]               NVARCHAR(60) NOT NULL,
    [descripcion]          NVARCHAR(MAX) NULL,
    [id_division]          INT NULL,
    CONSTRAINT [PK_DIVISIONES_ESPECIALES] PRIMARY KEY ([id_division_especial]),
    CONSTRAINT [FK_DIVISIONES_DIVISIONES_ESPECIALES] FOREIGN KEY ([id_division])
        REFERENCES [DIVISIONES]([id_division])
);
GO

CREATE TABLE [RECLUTAS] (
    [id_recluta] INT IDENTITY(1,1) NOT NULL,
    [nombre]     NVARCHAR(60) NOT NULL,
    [apellido]   NVARCHAR(60) NOT NULL,
    [peso]       DECIMAL(6,3) NOT NULL,
    [altura]     INT NOT NULL,
    [id_rama]    INT NOT NULL,
    CONSTRAINT [PK_RECLUTAS] PRIMARY KEY ([id_recluta]),
    CONSTRAINT [FK_RECLUTAS_RAMAS] FOREIGN KEY ([id_rama])
        REFERENCES [RAMAS]([id_rama])
);
GO

CREATE TABLE [APTOS_DIVISION_ESPECIAL] (
    [id_apto]              INT IDENTITY(1,1) NOT NULL,
    [id_recluta]           INT NOT NULL,
    [id_division_especial] INT NOT NULL,
    [apto_servicio]        BIT NOT NULL,
    [fecha_evaluacion]     DATE NOT NULL,
    [puntaje]              INT NOT NULL,
    CONSTRAINT [PK_APTOS_DIVISION_ESPECIAL] PRIMARY KEY ([id_apto]),
    CONSTRAINT [UQ_APTOS_id_recluta] UNIQUE ([id_recluta]),  -- un recluta, máximo un apto
    CONSTRAINT [FK_DIVISIONES_APTOS] FOREIGN KEY ([id_division_especial])
        REFERENCES [DIVISIONES_ESPECIALES]([id_division_especial]),
    CONSTRAINT [FK_RECLUTAS_APTOS] FOREIGN KEY ([id_recluta])
        REFERENCES [RECLUTAS]([id_recluta])
);
GO

-- =====================================================
-- 1. CREACIÓN DE TABLAS
-- =====================================================

-- Categorías de armamento (fusil, pistola, etc.)
CREATE TABLE [CATEGORIAS_ARMAMENTO] (
    [id_categoria] INT IDENTITY(1,1) NOT NULL,
    [nombre]       NVARCHAR(60) NOT NULL,
    [descripcion]  NVARCHAR(MAX) NULL,
    CONSTRAINT [PK_CATEGORIAS_ARMAMENTO] PRIMARY KEY ([id_categoria]),
    CONSTRAINT [UQ_CATEGORIAS_ARMAMENTO_nombre] UNIQUE ([nombre])
);
GO

-- Armamento disponible por rama
CREATE TABLE [ARMAMENTO] (
    [id_armamento] INT IDENTITY(1,1) NOT NULL,
    [nombre]       NVARCHAR(60) NOT NULL,
    [id_categoria] INT NOT NULL,
    [id_rama]      INT NOT NULL,
    [stock]        INT NOT NULL,  -- máximo de reclutas a los que se les puede asignar
    CONSTRAINT [PK_ARMAMENTO] PRIMARY KEY ([id_armamento]),
    CONSTRAINT [CK_ARMAMENTO_stock] CHECK ([stock] >= 0),
    -- Necesaria como destino de la FK compuesta desde RECLUTAS
    CONSTRAINT [UQ_ARMAMENTO_id_armamento_rama] UNIQUE ([id_armamento], [id_rama])
);
GO

-- Campo nuevo en RECLUTAS (NULL = sin armamento asignado)
ALTER TABLE [RECLUTAS] ADD [id_armamento] INT NULL;
GO

-- =====================================================
-- 2. CLAVES FORÁNEAS
-- =====================================================

ALTER TABLE [ARMAMENTO] ADD CONSTRAINT [FK_ARMAMENTO_CATEGORIAS]
    FOREIGN KEY ([id_categoria])
    REFERENCES [CATEGORIAS_ARMAMENTO]([id_categoria]);
GO

ALTER TABLE [ARMAMENTO] ADD CONSTRAINT [FK_ARMAMENTO_RAMAS]
    FOREIGN KEY ([id_rama])
    REFERENCES [RAMAS]([id_rama]);
GO

-- FK compuesta: el armamento debe pertenecer a la misma rama del recluta
ALTER TABLE [RECLUTAS] ADD CONSTRAINT [FK_RECLUTAS_ARMAMENTO]
    FOREIGN KEY ([id_armamento], [id_rama])
    REFERENCES [ARMAMENTO]([id_armamento], [id_rama]);
GO

-- =====================================================
-- 3. ÍNDICE Y CONTROL DE STOCK
-- =====================================================

CREATE INDEX [IX_RECLUTAS_id_armamento] ON [RECLUTAS]([id_armamento]);
GO

-- Un CHECK no puede contar filas de otra tabla, por eso se usa un trigger
CREATE TRIGGER [TR_RECLUTAS_valida_stock] ON [RECLUTAS]
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM [ARMAMENTO] a
        WHERE a.[id_armamento] IN (SELECT [id_armamento] FROM inserted WHERE [id_armamento] IS NOT NULL)
          AND (SELECT COUNT(*) FROM [RECLUTAS] r WHERE r.[id_armamento] = a.[id_armamento]) > a.[stock]
    )
    BEGIN
        ROLLBACK TRANSACTION;
        THROW 50001, 'Se superó el stock de asignaciones de este armamento.', 1;
    END
END;
GO