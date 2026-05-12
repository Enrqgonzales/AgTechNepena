package com.agtech.backend.service;

import com.agtech.backend.model.InventarioItem;
import com.agtech.backend.model.Parcela;
import com.agtech.backend.model.Registro;
import com.agtech.backend.model.Usuario;
import com.agtech.backend.repository.InventarioRepository;
import com.agtech.backend.repository.ParcelaRepository;
import com.agtech.backend.repository.RegistroRepository;
import com.agtech.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    @Transactional
    public List<Map<String, Object>> syncUsuarios(List<Map<String, Object>> usuarios) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Map<String, Object> data : usuarios) {
            int idLocal = Integer.parseInt(data.get("idLocal").toString());
            Usuario usuario = new Usuario();
            usuario.setNombre((String) data.get("nombre"));
            usuario.setTelefono((String) data.get("telefono"));
            Usuario saved = usuarioRepository.save(usuario);

            Map<String, Object> item = new HashMap<>();
            item.put("idLocal", idLocal);
            item.put("remoteId", saved.getId());
            resultado.add(item);
        }
        return resultado;
    }

    @Transactional
    public List<Map<String, Object>> syncParcelas(List<Map<String, Object>> parcelas) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Map<String, Object> data : parcelas) {
            int idLocal = Integer.parseInt(data.get("idLocal").toString());
            Parcela parcela = new Parcela();

            Long usuarioId = Long.valueOf(data.get("usuarioId").toString());
            Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
            parcela.setUsuario(usuario);

            parcela.setNombre((String) data.get("nombre"));
            parcela.setCultivo((String) data.get("cultivo"));
            parcela.setHectareas(Double.valueOf(data.get("hectareas").toString()));
            parcela.setUbicacion(data.get("ubicacion") != null ? (String) data.get("ubicacion") : "");
            Parcela saved = parcelaRepository.save(parcela);

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
            Registro registro = new Registro();

            Long parcelaId = Long.valueOf(data.get("parcelaId").toString());
            Parcela parcela = parcelaRepository.findById(parcelaId).orElse(null);
            registro.setParcela(parcela);

            registro.setTipo((String) data.get("tipo"));
            registro.setCategoria((String) data.get("categoria"));
            registro.setMonto(Double.valueOf(data.get("monto").toString()));
            registro.setDescripcion(data.get("descripcion") != null ? (String) data.get("descripcion") : "");
            registro.setFecha(LocalDate.parse((String) data.get("fecha")));
            Registro saved = registroRepository.save(registro);

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
            InventarioItem invItem = new InventarioItem();

            invItem.setNombre((String) data.get("nombre"));
            invItem.setCategoria((String) data.get("categoria"));
            invItem.setCantidad(data.get("cantidad") != null ? Double.valueOf(data.get("cantidad").toString()) : 0.0);
            invItem.setUnidad((String) data.get("unidad"));
            invItem.setCostoUnitario(data.get("costoUnitario") != null ? Double.valueOf(data.get("costoUnitario").toString()) : 0.0);
            invItem.setFechaIngreso((String) data.get("fechaIngreso"));
            invItem.setDescripcion(data.get("descripcion") != null ? (String) data.get("descripcion") : "");
            InventarioItem saved = inventarioRepository.save(invItem);

            Map<String, Object> item = new HashMap<>();
            item.put("idLocal", idLocal);
            item.put("remoteId", saved.getId());
            resultado.add(item);
        }
        return resultado;
    }
}
