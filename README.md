# Auditoria

# Prerrequisitos
- 100 GB de espacio en Disco Duro
- 16 GB de RAM
- Docker 29.0.0 o superior para crear un contenedor con una base de datos Oracle.
- JDK 21
- Node 22.15.0

## Levantar el ambiente
1. Iniciar el contenedor de base de datos
```bash
docker login container-registry.oracle.com
docker run -d --name oracle_free -p 1521:1521 -e ORACLE_PWD=OracleFree.2025 -v ./oracle_data:/opt/oracle/oradata container-registry.oracle.com/database/free:latest
```

Datos de conexión a la base de datos de prueba:
usuario: system
password: OracleFree.2025
host: localhost
puerto: 1521
SID: free


2. Iniciar la aplicación
Desde el directorio raiz del proyecto ejecutar la sentencia:
```bash
./mvnw
```


Datos del usuario administrador:
Usuario: admin
Password: admin


Ejemplo encuesta Para capturarse en el modelo Encuesta

```json
{
    "titulo": "Encuesta 2025",
    "preguntas": [
        {
            "indice": 1,
            "enunciado": "Pregunta 1",
            "opciones": [
                {
                    "indice": 1,
                    "enunciado": "opcion 1 de pregunta 1",
                    "siguiente": 2
                },
                {
                    "indice": 2,
                    "enunciado": "opcion 2 de pregunta 1",
                    "siguiente": 2
                },
                {
                    "indice": 3,
                    "enunciado": "opcion 3 de pregunta 1",
                    "siguiente": 2
                },
                {
                    "indice": 4,
                    "enunciado": "opcion 4 de pregunta 1",
                    "siguiente": 3
                }
            ]
        },
        {
            "indice": 2,
            "enunciado": "Pregunta 2",
            "opciones": [
                {
                    "indice": 1,
                    "enunciado": "opcion 1 de pregunta 2",
                    "siguiente": 3
                },
                {
                    "indice": 2,
                    "enunciado": "opcion 2 de pregunta 2",
                    "siguiente": 3
                },
                {
                    "indice": 3,
                    "enunciado": "opcion 3 de pregunta 2",
                    "siguiente": 3
                },
                {
                    "indice": 4,
                    "enunciado": "opcion 4 de pregunta 2",
                    "siguiente": 3
                }
            ]
        },
        {
            "indice": 3,
            "enunciado": "Pregunta 3",
            "opciones": [
                {
                    "indice": 1,
                    "enunciado": "opcion 1 de pregunta 3",
                    "siguiente": 4
                },
                {
                    "indice": 2,
                    "enunciado": "opcion 2 de pregunta 3",
                    "siguiente": 4
                },
                {
                    "indice": 3,
                    "enunciado": "opcion 3 de pregunta 3",
                    "siguiente": 4
                },
                {
                    "indice": 4,
                    "enunciado": "opcion 4 de pregunta 3",
                    "abierta": 1,
                    "siguiente": 4
                }
            ]
        },
        {
            "indice": 4,
            "enunciado": "Pregunta 4",
            "opciones": [
                {
                    "indice": 1,
                    "enunciado": "opcion 1 de pregunta 4"
                },
                {
                    "indice": 2,
                    "enunciado": "opcion 2 de pregunta 4"
                },
                {
                    "indice": 3,
                    "enunciado": "opcion 3 de pregunta 4"
                },
                {
                    "indice": 4,
                    "enunciado": "opcion 4 de pregunta 4"
                }
            ]
        }
    ]
}
```