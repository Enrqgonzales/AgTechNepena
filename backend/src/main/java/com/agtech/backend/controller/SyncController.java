package com.agtech.backend.controller;

import com.agtech.backend.service.SyncService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping("/usuarios")
    public ResponseEntity<?> syncUsuarios(@RequestBody List<Map<String, Object>> usuarios) {
        try {
            List<Map<String, Object>> resultado = syncService.syncUsuarios(usuarios);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/parcelas")
    public ResponseEntity<?> syncParcelas(@RequestBody List<Map<String, Object>> parcelas) {
        try {
            List<Map<String, Object>> resultado = syncService.syncParcelas(parcelas);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/registros")
    public ResponseEntity<?> syncRegistros(@RequestBody List<Map<String, Object>> registros) {
        try {
            List<Map<String, Object>> resultado = syncService.syncRegistros(registros);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/inventario")
    public ResponseEntity<?> syncInventario(@RequestBody List<Map<String, Object>> items) {
        try {
            List<Map<String, Object>> resultado = syncService.syncInventario(items);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
