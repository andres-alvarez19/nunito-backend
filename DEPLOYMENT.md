# Nunito Backend - Guía de Despliegue para Testing

## Opción 1: Ejecución Local (Desarrollo)

### Requisitos
- Java 21
- PostgreSQL 16 (o usar H2 en memoria)

### Pasos
```bash
# 1. Ejecutar la aplicación
./gradlew bootRun

# 2. La aplicación estará disponible en:
# http://localhost:8080

# 3. Los archivos subidos se guardarán en:
# ./uploads/
```

---

## Opción 2: Docker Compose (Recomendado para Testing)

### Requisitos
- Docker
- Docker Compose

### Pasos
```bash
# 1. Construir y levantar los servicios
docker-compose up -d

# 2. Ver logs
docker-compose logs -f backend

# 3. La aplicación estará disponible en:
# http://localhost:8080

# 4. Detener los servicios
docker-compose down

# 5. Detener y eliminar volúmenes (limpieza completa)
docker-compose down -v
```

### Características
- ✅ PostgreSQL incluido
- ✅ Volumen persistente para uploads
- ✅ Volumen persistente para base de datos
- ✅ Red aislada entre servicios

---

## Opción 3: Docker Solo (Sin PostgreSQL)

```bash
# 1. Construir imagen
docker build -t nunito-backend .

# 2. Ejecutar contenedor
docker run -d \
  -p 8080:8080 \
  -v $(pwd)/uploads:/app/uploads \
  -e SPRING_DATASOURCE_URL=jdbc:h2:mem:testdb \
  -e SPRING_JPA_HIBERNATE_DDL_AUTO=create-drop \
  --name nunito-backend \
  nunito-backend

# 3. Ver logs
docker logs -f nunito-backend

# 4. Detener y eliminar
docker stop nunito-backend && docker rm nunito-backend
```

---

## Verificar que Funciona

### 1. Health Check
```bash
curl http://localhost:8080/actuator/health
```

### 2. Subir una imagen
```bash
curl -X POST http://localhost:8080/api/upload/image \
  -F "file=@/ruta/a/tu/imagen.jpg"
```

**Respuesta esperada:**
```json
{
  "url": "/uploads/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg",
  "filename": "a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg"
}
```

### 3. Acceder a la imagen
```bash
# En el navegador o con curl
curl http://localhost:8080/uploads/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg
```

---

## Notas Importantes

### Almacenamiento de Archivos
- **Directorio:** `uploads/` en la raíz del proyecto
- **Persistencia:** Los archivos se mantienen entre reinicios gracias al volumen Docker
- **Acceso:** Servidos directamente por Spring en `/uploads/**`

### Limitaciones para Testing
- ⚠️ Solo funciona con una instancia del servidor
- ⚠️ No hay CDN (los archivos se sirven desde el backend)
- ⚠️ No hay transformación de imágenes
- ✅ Perfecto para demos y testing

### Para Producción
Considera migrar a:
- AWS S3 + CloudFront
- Cloudinary
- MinIO (self-hosted)

---

## Troubleshooting

### Error: "Could not initialize upload folder"
```bash
# Crear el directorio manualmente
mkdir -p uploads
chmod 755 uploads
```

### Error: Puerto 8080 en uso
```bash
# Cambiar el puerto en docker-compose.yml
ports:
  - "8081:8080"  # Usar 8081 en lugar de 8080
```

### Ver archivos en el volumen Docker
```bash
# Listar volúmenes
docker volume ls

# Inspeccionar volumen de uploads
docker volume inspect nunito-backend_uploads_data
```
