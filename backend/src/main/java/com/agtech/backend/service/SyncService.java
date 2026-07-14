package com.agtech.backend.service;

import com.agtech.backend.model.InventarioItem;
import com.agtech.backend.model.InventarioMovimiento;
import com.agtech.backend.model.Parcela;
import com.agtech.backend.model.Registro;
import com.agtech.backend.model.Usuario;
import com.agtech.backend.repository.InventarioMovimientoRepository;
import com.agtech.backend.repository.InventarioRepository;
import com.agtech.backend.repository.ParcelaRepository;
import com.agtech.backend.repository.RegistroRepository;
import com.agtech.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service para sincronizacion de datos.
 * Procesa datos recibidos desde la app movil y devuelve idLocal + remoteId.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
@Service
public class SyncService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ParcelaRepository parcelaRepository;

    @Autowired
    private RegistroRepository registroRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private InventarioMovimientoRepository inventarioMovimientoRepository;

    @Transactional
    public List<Map<String, Object>> syncUsuarios(List<Map<String, Object>> usuarios) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Map<String, Object> data : usuarios) {
            int idLocal = Integer.parseInt(data.get("idLocal").toString());
            
            // Validación de datos
            if (!data.containsKey("nombre") || data.get("nombre") == null || data.get("nombre").toString().trim().isEmpty()) {
                throw new IllegalArgumentException("Nombre es requerido para el usuario local ID: " + idLocal);
            }

            String uuid = (String) data.get("uuid");
            String firebaseUid = (String) data.get("firebaseUid");

            java.util.Optional<Usuario> existing = java.util.Optional.empty();
            if (uuid != null) {
                existing = usuarioRepository.findByUuid(uuid);
            }
            if (!existing.isPresent() && firebaseUid != null) {
                existing = usuarioRepository.findByFirebaseUid(firebaseUid);
            }

            Usuario saved;
            if (existing.isPresent()) {
                saved = existing.get();
                if (firebaseUid != null && saved.getFirebaseUid() == null) {
                    saved.setFirebaseUid(firebaseUid);
                    saved = usuarioRepository.save(saved);
                }
            } else {
                Usuario usuario = new Usuario();
                usuario.setNombre((String) data.get("nombre"));
                usuario.setTelefono((String) data.get("telefono"));
                if (uuid != null) {
                    usuario.setUuid(uuid);
                }
                if (firebaseUid != null) {
                    usuario.setFirebaseUid(firebaseUid);
                }
                saved = usuarioRepository.save(usuario);
            }

            Map<String, Object> item = new HashMap<>();
            item.put("idLocal", idLocal);
            item.put("remoteId", saved.getId());
            resultado.add(item);
        }
        return resultado;
    }

    public java.util.Optional<Usuario> getUsuarioByFirebaseUid(String firebaseUid) {
        return usuarioRepository.findByFirebaseUid(firebaseUid);
    }

    @Transactional
    public List<Map<String, Object>> syncParcelas(List<Map<String, Object>> parcelas) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Map<String, Object> data : parcelas) {
            int idLocal = Integer.parseInt(data.get("idLocal").toString());
            
            // Validación de datos
            if (!data.containsKey("nombre") || data.get("nombre") == null || data.get("nombre").toString().trim().isEmpty()) {
                throw new IllegalArgumentException("Nombre es requerido para la parcela local ID: " + idLocal);
            }
            if (!data.containsKey("cultivo") || data.get("cultivo") == null || data.get("cultivo").toString().trim().isEmpty()) {
                throw new IllegalArgumentException("Cultivo es requerido para la parcela local ID: " + idLocal);
            }
            if (!data.containsKey("usuarioId") || data.get("usuarioId") == null) {
                throw new IllegalArgumentException("Propietario (usuarioId) es requerido para la parcela local ID: " + idLocal);
            }

            String uuid = (String) data.get("uuid");
            java.util.Optional<Parcela> existing = (uuid != null) ? parcelaRepository.findByUuid(uuid) : java.util.Optional.empty();
            Parcela saved;
            if (existing.isPresent()) {
                saved = existing.get();
                if (data.containsKey("estado")) {
                    saved.setEstado((String) data.get("estado"));
                }
                saved = parcelaRepository.save(saved);
            } else {
                Parcela parcela = new Parcela();

                Long usuarioId = Long.valueOf(data.get("usuarioId").toString());
                Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
                if (usuario == null) {
                    throw new IllegalArgumentException("El usuario propietario con ID " + usuarioId + " no existe");
                }
                parcela.setUsuario(usuario);

                parcela.setNombre((String) data.get("nombre"));
                parcela.setCultivo((String) data.get("cultivo"));
                parcela.setHectareas(Double.valueOf(data.get("hectareas").toString()));
                parcela.setUbicacion(data.get("ubicacion") != null ? (String) data.get("ubicacion") : "");
                if (data.containsKey("estado")) {
                    parcela.setEstado((String) data.get("estado"));
                }
                if (uuid != null) {
                    parcela.setUuid(uuid);
                }
                saved = parcelaRepository.save(parcela);
            }

            Map<String, Object> item = new HashMap<>();
            item.put("idLocal", idLocal);
            item.put("remoteId", saved.getId());
            resultado.add(item);
        }
        return resultado;
    }

    @Transactional
    public List<Map<String, Object>> syncRegistros(List<Map<String, Object>> registros) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Map<String, Object> data : registros) {
            int idLocal = Integer.parseInt(data.get("idLocal").toString());

            // Validación de datos
            if (!data.containsKey("tipo") || data.get("tipo") == null || data.get("tipo").toString().trim().isEmpty()) {
                throw new IllegalArgumentException("Tipo es requerido para el registro local ID: " + idLocal);
            }
            if (!data.containsKey("monto") || data.get("monto") == null) {
                throw new IllegalArgumentException("Monto es requerido para el registro local ID: " + idLocal);
            }
            if (!data.containsKey("fecha") || data.get("fecha") == null || data.get("fecha").toString().trim().isEmpty()) {
                throw new IllegalArgumentException("Fecha es requerido para el registro local ID: " + idLocal);
            }
            if (!data.containsKey("parcelaId") || data.get("parcelaId") == null) {
                throw new IllegalArgumentException("Parcela ID es requerido para el registro local ID: " + idLocal);
            }

            String uuid = (String) data.get("uuid");
            java.util.Optional<Registro> existing = (uuid != null) ? registroRepository.findByUuid(uuid) : java.util.Optional.empty();
            Registro saved;
            if (existing.isPresent()) {
                saved = existing.get();
            } else {
                Registro registro = new Registro();

                Long parcelaId = Long.valueOf(data.get("parcelaId").toString());
                Parcela parcela = parcelaRepository.findById(parcelaId).orElse(null);
                if (parcela == null) {
                    throw new IllegalArgumentException("La parcela con ID " + parcelaId + " no existe");
                }
                registro.setParcela(parcela);

                registro.setTipo((String) data.get("tipo"));
                registro.setCategoria((String) data.get("categoria"));
                registro.setMonto(Double.valueOf(data.get("monto").toString()));
                registro.setDescripcion(data.get("descripcion") != null ? (String) data.get("descripcion") : "");
                registro.setFecha(LocalDate.parse((String) data.get("fecha")));
                if (uuid != null) {
                    registro.setUuid(uuid);
                }
                saved = registroRepository.save(registro);
            }

            Map<String, Object> item = new HashMap<>();
            item.put("idLocal", idLocal);
            item.put("remoteId", saved.getId());
            resultado.add(item);
        }
        return resultado;
    }

    @Transactional
    public List<Map<String, Object>> syncInventario(List<Map<String, Object>> items) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Map<String, Object> data : items) {
            int idLocal = Integer.parseInt(data.get("idLocal").toString());

            // Validación de datos
            if (!data.containsKey("nombre") || data.get("nombre") == null || data.get("nombre").toString().trim().isEmpty()) {
                throw new IllegalArgumentException("Nombre es requerido para el insumo local ID: " + idLocal);
            }
            if (!data.containsKey("categoria") || data.get("categoria") == null || data.get("categoria").toString().trim().isEmpty()) {
                throw new IllegalArgumentException("Categoría es requerido para el insumo local ID: " + idLocal);
            }
            if (!data.containsKey("parcelaId") || data.get("parcelaId") == null) {
                throw new IllegalArgumentException("Parcela ID es requerido para el insumo local ID: " + idLocal);
            }

            String uuid = (String) data.get("uuid");
            java.util.Optional<InventarioItem> existing = (uuid != null) ? inventarioRepository.findByUuid(uuid) : java.util.Optional.empty();
            InventarioItem saved;
            if (existing.isPresent()) {
                saved = existing.get();
            } else {
                InventarioItem invItem = new InventarioItem();

                invItem.setNombre((String) data.get("nombre"));
                invItem.setCategoria((String) data.get("categoria"));
                invItem.setCantidad(data.get("cantidad") != null ? Double.valueOf(data.get("cantidad").toString()) : 0.0);
                invItem.setUnidad((String) data.get("unidad"));
                invItem.setCostoUnitario(data.get("costoUnitario") != null ? Double.valueOf(data.get("costoUnitario").toString()) : 0.0);
                invItem.setFechaIngreso((String) data.get("fechaIngreso"));
                invItem.setDescripcion(data.get("descripcion") != null ? (String) data.get("descripcion") : "");

                // Relacionar parcela
                Long parcelaId = Long.valueOf(data.get("parcelaId").toString());
                Parcela parcela = parcelaRepository.findById(parcelaId).orElse(null);
                if (parcela == null) {
                    throw new IllegalArgumentException("La parcela con ID " + parcelaId + " no existe para asociar al inventario");
                }
                invItem.setParcela(parcela);
                if (uuid != null) {
                    invItem.setUuid(uuid);
                }
                saved = inventarioRepository.save(invItem);
            }

            Map<String, Object> item = new HashMap<>();
            item.put("idLocal", idLocal);
            item.put("remoteId", saved.getId());
            resultado.add(item);
        }
        return resultado;
    }

    @Transactional
    public List<Map<String, Object>> syncMovimientos(List<Map<String, Object>> movimientos) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Map<String, Object> data : movimientos) {
            int idLocal = Integer.parseInt(data.get("idLocal").toString());

            // Validación de datos
            if (!data.containsKey("itemId") || data.get("itemId") == null) {
                throw new IllegalArgumentException("Item ID es requerido para el movimiento local ID: " + idLocal);
            }
            if (!data.containsKey("tipo") || data.get("tipo") == null || data.get("tipo").toString().trim().isEmpty()) {
                throw new IllegalArgumentException("Tipo es requerido para el movimiento local ID: " + idLocal);
            }
            if (!data.containsKey("cantidad") || data.get("cantidad") == null) {
                throw new IllegalArgumentException("Cantidad es requerido para el movimiento local ID: " + idLocal);
            }

            String uuid = (String) data.get("uuid");
            java.util.Optional<InventarioMovimiento> existing = (uuid != null) ? inventarioMovimientoRepository.findByUuid(uuid) : java.util.Optional.empty();
            InventarioMovimiento saved;
            if (existing.isPresent()) {
                saved = existing.get();
            } else {
                InventarioMovimiento mov = new InventarioMovimiento();

                Long itemId = Long.valueOf(data.get("itemId").toString());
                InventarioItem item = inventarioRepository.findById(itemId).orElse(null);
                if (item == null) {
                    throw new IllegalArgumentException("El insumo de inventario con ID " + itemId + " no existe");
                }
                mov.setInventarioItem(item);

                mov.setTipo((String) data.get("tipo"));
                mov.setCantidad(Double.valueOf(data.get("cantidad").toString()));
                mov.setUnidad((String) data.get("unidad"));
                mov.setCostoTotal(data.get("costoTotal") != null ? Double.valueOf(data.get("costoTotal").toString()) : 0.0);
                mov.setFecha((String) data.get("fecha"));
                mov.setDescripcion(data.get("descripcion") != null ? (String) data.get("descripcion") : "");

                if (data.containsKey("registroId") && data.get("registroId") != null) {
                    Long registroId = Long.valueOf(data.get("registroId").toString());
                    Registro registro = registroRepository.findById(registroId).orElse(null);
                    mov.setRegistro(registro);
                }

                if (uuid != null) {
                    mov.setUuid(uuid);
                }
                saved = inventarioMovimientoRepository.save(mov);
            }

            Map<String, Object> item = new HashMap<>();
            item.put("idLocal", idLocal);
            item.put("remoteId", saved.getId());
            resultado.add(item);
        }
        return resultado;
    }
}
