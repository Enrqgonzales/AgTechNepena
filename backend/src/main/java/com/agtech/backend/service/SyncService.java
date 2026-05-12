package com.agtech.backend.service;

import com.agtech.backend.model.Parcela;
import com.agtech.backend.model.Registro;
import com.agtech.backend.model.Usuario;
import com.agtech.backend.repository.ParcelaRepository;
import com.agtech.backend.repository.RegistroRepository;
import com.agtech.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service para sincronizacion de datos.
 * Procesa datos recibidos desde la app movil.
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

    @Transactional
    public void syncUsuarios(List<Map<String, Object>> usuarios) {
        for (Map<String, Object> data : usuarios) {
            Usuario usuario = new Usuario();
            usuario.setNombre((String) data.get("nombre"));
            usuario.setTelefono((String) data.get("telefono"));
            usuarioRepository.save(usuario);
        }
    }

    @Transactional
    public void syncParcelas(List<Map<String, Object>> parcelas) {
        for (Map<String, Object> data : parcelas) {
            Parcela parcela = new Parcela();

            Long usuarioId = Long.valueOf(data.get("usuarioId").toString());
            Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
            parcela.setUsuario(usuario);

            parcela.setNombre((String) data.get("nombre"));
            parcela.setCultivo((String) data.get("cultivo"));
            parcela.setHectareas(Double.valueOf(data.get("hectareas").toString()));
            parcela.setUbicacion((String) data.get("ubicacion"));
            parcelaRepository.save(parcela);
        }
    }

    @Transactional
    public void syncRegistros(List<Map<String, Object>> registros) {
        for (Map<String, Object> data : registros) {
            Registro registro = new Registro();

            Long parcelaId = Long.valueOf(data.get("parcelaId").toString());
            Parcela parcela = parcelaRepository.findById(parcelaId).orElse(null);
            registro.setParcela(parcela);

            registro.setTipo((String) data.get("tipo"));
            registro.setCategoria((String) data.get("categoria"));
            registro.setMonto(Double.valueOf(data.get("monto").toString()));
            registro.setDescripcion((String) data.get("descripcion"));
            registro.setFecha(LocalDate.parse((String) data.get("fecha")));
            registroRepository.save(registro);
        }
    }
}
