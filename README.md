# Auditoria

```bash
docker login container-registry.oracle.com
docker run -d --name oracle_free -p 1521:1521 -e ORACLE_PWD=OracleFree.2025 -v ./oracle_data:/opt/oracle/oradata container-registry.oracle.com/database/free:latest
```
