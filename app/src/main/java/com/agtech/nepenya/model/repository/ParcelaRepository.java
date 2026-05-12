package com.agtech.nepenya.model.repository;

import com.agtech.nepenya.model.dao.ParcelaDao;
import com.agtech.nepenya.model.entity.Parcela;

import java.util.List;

/**
 * Repository para operaciones de Parcela.
 * Actua como fuente unica de verdad entre Controller y DAO.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class ParcelaRepository {

    private final ParcelaDao parcelaDao;

    /**
     * Constructor con inyeccion de DAO.
     *
     * @param parcelaDao DAO de parcela
     */
    public ParcelaRepository(ParcelaDao parcelaDao) {
        this.parcelaDao = parcelaDao;
    }

    /**
     * Inserta una nueva parcela.
     *
     * @param parcela Parcela a insertar
     * @return ID generado
     */
    public long insertar(Parcela parcela) {
        return parcelaDao.insertar(parcela);
    }

    /**
     * Actualiza una parcela existente.
     *
     * @param parcela Parcela a actualizar
     */
    public void actualizar(Parcela parcela) {
        parcelaDao.actualizar(parcela);
    }

    /**
     * Elimina una parcela.
     *
     * @param parcela Parcela a eliminar
     */
    public void eliminar(Parcela parcela) {
        parcelaDao.eliminar(parcela);
    }

    /**
     * Obtiene todas las parcelas.
     *
     * @return Lista de parcelas
     */
    public List<Parcela> obtenerTodas() {
        return parcelaDao.obtenerTodas();
    }

    /**
     * Obtiene parcelas por usuario.
     *
     * @param usuarioId ID del usuario propietario
     * @return Lista de parcelas
     */
    public List<Parcela> obtenerPorUsuario(int usuarioId) {
        return parcelaDao.obtenerPorUsuario(usuarioId);
    }

    /**
     * Obtiene una parcela por ID.
     *
     * @param id Identificador de la parcela
     * @return Parcela encontrada o null
     */
    public Parcela obtenerPorId(int id) {
        return parcelaDao.obtenerPorId(id);
    }

    /**
     * Obtiene parcelas pendientes de sincronizacion.
     *
     * @return Lista de parcelas pendientes
     */
    public List<Parcela> obtenerPendientesSync() {
        return parcelaDao.obtenerPendientesSync();
    }

    /**
     * Cuenta parcelas pendientes de sincronizacion.
     *
     * @return Cantidad de parcelas pendientes
     */
    public int contarPendientes() {
        return parcelaDao.contarPendientes();
    }

    /**
     * Actualiza estado de sincronizacion.
     *
     * @param id         ID de la parcela
     * @param syncStatus Nuevo estado
     * @param remoteId   ID remoto
     */
    public void actualizarSyncStatus(int id, String syncStatus, Integer remoteId) {
        parcelaDao.actualizarSyncStatus(id, syncStatus, remoteId);
    }

    /**
     * Obtiene nombre de parcela por ID.
     *
     * @param id Identificador de la parcela
     * @return Nombre de la parcela
     */
    public String obtenerNombrePorId(int id) {
        return parcelaDao.obtenerNombrePorId(id);
    }
}
