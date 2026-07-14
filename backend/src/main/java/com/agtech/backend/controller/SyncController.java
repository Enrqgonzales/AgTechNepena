package com.agtech.backend.controller;

import com.agtech.backend.service.SyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller para sincronizacion de datos.
 * Recibe datos desde la app movil y devuelve idLocal + remoteId asignado.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/sync")
@CrossOrigin(origins = "*")
public class SyncController {

    @Autowired
    private SyncService syncService;

    @Value("${app.api-key:agtech_secret_key_2026}")
    private String apiKey;

    private boolean isNotAuthorized(String clientKey) {
        return clientKey == null || !clientKey.equals(apiKey);
    }

    @PostMapping("/usuarios")
    public ResponseEntity<?> syncUsuarios(
            @RequestHeader(value = "X-API-Key", required = false) String clientKey,
            @RequestBody List<Map<String, Object>> usuarios) {
        if (isNotAuthorized(clientKey)) {
            return ResponseEntity.status(401).body("{\"error\":\"No autorizado\"}");
        }
        try {
            List<Map<String, Object>> resultado = syncService.syncUsuarios(usuarios);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/usuarios/firebase/{firebaseUid}")
    public ResponseEntity<?> getUsuarioByFirebaseUid(
            @RequestHeader(value = "X-API-Key", required = false) String clientKey,
            @PathVariable String firebaseUid) {
        if (isNotAuthorized(clientKey)) {
            return ResponseEntity.status(401).body("{\"error\":\"No autorizado\"}");
        }
        return syncService.getUsuarioByFirebaseUid(firebaseUid)
                .map(usuario -> ResponseEntity.ok(usuario))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/descargar/{firebaseUid}")
    public ResponseEntity<?> descargarDatos(
            @RequestHeader(value = "X-API-Key", required = false) String clientKey,
            @PathVariable String firebaseUid) {
        if (isNotAuthorized(clientKey)) {
            return ResponseEntity.status(401).body("{\"error\":\"No autorizado\"}");
        }
        try {
            Map<String, Object> datos = syncService.getDatosSincronizacion(firebaseUid);
            return ResponseEntity.ok(datos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/parcelas")
    public ResponseEntity<?> syncParcelas(
            @RequestHeader(value = "X-API-Key", required = false) String clientKey,
            @RequestBody List<Map<String, Object>> parcelas) {
        if (isNotAuthorized(clientKey)) {
            return ResponseEntity.status(401).body("{\"error\":\"No autorizado\"}");
        }
        try {
            List<Map<String, Object>> resultado = syncService.syncParcelas(parcelas);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/registros")
    public ResponseEntity<?> syncRegistros(
            @RequestHeader(value = "X-API-Key", required = false) String clientKey,
            @RequestBody List<Map<String, Object>> registros) {
        if (isNotAuthorized(clientKey)) {
            return ResponseEntity.status(401).body("{\"error\":\"No autorizado\"}");
        }
        try {
            List<Map<String, Object>> resultado = syncService.syncRegistros(registros);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/inventario")
    public ResponseEntity<?> syncInventario(
            @RequestHeader(value = "X-API-Key", required = false) String clientKey,
            @RequestBody List<Map<String, Object>> items) {
        if (isNotAuthorized(clientKey)) {
            return ResponseEntity.status(401).body("{\"error\":\"No autorizado\"}");
        }
        try {
            List<Map<String, Object>> resultado = syncService.syncInventario(items);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/movimientos")
    public ResponseEntity<?> syncMovimientos(
            @RequestHeader(value = "X-API-Key", required = false) String clientKey,
            @RequestBody List<Map<String, Object>> movimientos) {
        if (isNotAuthorized(clientKey)) {
            return ResponseEntity.status(401).body("{\"error\":\"No autorizado\"}");
        }
        try {
            List<Map<String, Object>> resultado = syncService.syncMovimientos(movimientos);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
