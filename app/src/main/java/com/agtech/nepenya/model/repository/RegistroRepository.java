package com.agtech.nepenya.model.repository;

import com.agtech.nepenya.model.dao.RegistroDao;
import com.agtech.nepenya.model.entity.Registro;

import java.util.List;

/**
 * Repository para operaciones de Registro.
 * Actua como fuente unica de verdad entre Controller y DAO.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class RegistroRepository {

    private final RegistroDao registroDao;

    /**
     * Constructor con inyeccion de DAO.
     *
     * @param registroDao DAO de registro
     */
    public RegistroRepository(RegistroDao registroDao) {
        this.registroDao = registroDao;
    }

    /**
     * Inserta un nuevo registro.
     *
     * @param registro Registro a insertar
     * @return ID generado
     */
    public long insertar(Registro registro) {
        return registroDao.insertar(registro);
    }

    /**
     * Actualiza un registro existente.
     *
     * @param registro Registro a actualizar
     */
    public void actualizar(Registro registro) {
        registroDao.actualizar(registro);
    }

    /**
     * Elimina un registro.
     *
     * @param registro Registro a eliminar
     */
    public void eliminar(Registro registro) {
        registroDao.eliminar(registro);
    }

    /**
     * Elimina un registro por ID.
     *
     * @param id ID del registro
     */
    public void eliminarPorId(int id) {
        Registro registro = registroDao.obtenerPorId(id);
        if (registro != null) {
            registroDao.eliminar(registro);
        }
    }

    /**
     * Obtiene todos los registros.
     *
     * @return Lista de registros
     */
    public List<Registro> obtenerTodos(int userId) {
        return registroDao.obtenerTodos(userId);
    }

    /**
     * Obtiene registros por parcela.
     *
     * @param parcelaId ID de la parcela
     * @return Lista de registros
     */
    public List<Registro> obtenerPorParcela(int parcelaId) {
        return registroDao.obtenerPorParcela(parcelaId);
    }

    /**
     * Obtiene registros por tipo.
     *
     * @param tipo Tipo de registro (GASTO/INGRESO)
     * @return Lista de registros
     */
    public List<Registro> obtenerPorTipo(int userId, String tipo) {
        return registroDao.obtenerPorTipo(userId, tipo);
    }

    /**
     * Obtiene registros por parcela y tipo.
     *
     * @param parcelaId ID de la parcela
     * @param tipo      Tipo de registro
     * @return Lista de registros filtrados
     */
    public List<Registro> obtenerPorParcelaYTipo(int parcelaId, String tipo) {
        return registroDao.obtenerPorParcelaYTipo(parcelaId, tipo);
    }

    /**
     * Obtiene un registro por ID.
     *
     * @param id Identificador del registro
     * @return Registro encontrado o null
     */
    public Registro obtenerPorId(int id) {
        return registroDao.obtenerPorId(id);
    }

    /**
     * Obtiene registros pendientes de sincronizacion.
     *
     * @return Lista de registros pendientes
     */
    public List<Registro> obtenerPendientesSync() {
        return registroDao.obtenerPendientesSync();
    }

    /**
     * Cuenta registros pendientes de sincronizacion.
     *
     * @return Cantidad de registros pendientes
     */
    public int contarPendientes() {
        return registroDao.contarPendientes();
    }

    /**
     * Actualiza estado de sincronizacion.
     *
     * @param id         ID del registro
     * @param syncStatus Nuevo estado
     * @param remoteId   ID remoto
     */
    public void actualizarSyncStatus(int id, String syncStatus, Integer remoteId) {
        registroDao.actualizarSyncStatus(id, syncStatus, remoteId);
    }

    /**
     * Obtiene registros por año.
     *
     * @param anio Anio en formato yyyy
     * @return Lista de registros
     */
    public List<Registro> obtenerPorAnio(int userId, String anio) {
        String inicio = anio + "-01-01";
        String fin = anio + "-12-31";
        return registroDao.obtenerPorAnio(userId, inicio, fin);
    }

    /**
     * Obtiene total de gastos por año.
     *
     * @param anio Anio en formato yyyy
     * @return Total de gastos
     */
    public double obtenerTotalGastosPorAnio(int userId, String anio) {
        String inicio = anio + "-01-01";
        String fin = anio + "-12-31";
        Double total = registroDao.obtenerTotalGastosPorAnio(userId, inicio, fin);
        return total != null ? total : 0.0;
    }

    /**
     * Obtiene total de ingresos por año.
     *
     * @param anio Anio en formato yyyy
     * @return Total de ingresos
     */
    public double obtenerTotalIngresosPorAnio(int userId, String anio) {
        String inicio = anio + "-01-01";
        String fin = anio + "-12-31";
        Double total = registroDao.obtenerTotalIngresosPorAnio(userId, inicio, fin);
        return total != null ? total : 0.0;
    }

    /**
     * Obtiene categorias de gastos.
     *
     * @return Lista de categorias
     */
    public List<String> obtenerCategoriasGasto(int userId) {
        return registroDao.obtenerCategoriasGasto(userId);
    }

    /**
     * Obtiene categorias de ingresos.
     *
     * @return Lista de categorias
     */
    public List<String> obtenerCategoriasIngreso(int userId) {
        return registroDao.obtenerCategoriasIngreso(userId);
    }

    /**
     * Obtiene gastos por categoria y año.
     *
     * @param anio      Anio en formato yyyy
     * @param categoria Categoria
     * @return Suma de gastos
     */
    public double obtenerGastosPorCategoriaYAnio(int userId, String anio, String categoria) {
        String inicio = anio + "-01-01";
        String fin = anio + "-12-31";
        Double total = registroDao.obtenerGastosPorCategoriaYAnio(userId, inicio, fin, categoria);
        return total != null ? total : 0.0;
    }

    /**
     * Obtiene ingresos por categoria y año.
     *
     * @param anio      Anio en formato yyyy
     * @param categoria Categoria
     * @return Suma de ingresos
     */
    public double obtenerIngresosPorCategoriaYAnio(int userId, String anio, String categoria) {
        String inicio = anio + "-01-01";
        String fin = anio + "-12-31";
        Double total = registroDao.obtenerIngresosPorCategoriaYAnio(userId, inicio, fin, categoria);
        return total != null ? total : 0.0;
    }

    /**
     * Obtiene registros por año y mes.
     */
    public List<Registro> obtenerPorAnioYMes(int userId, String anio, String mes) {
        String inicio = anio + "-" + mes + "-01";
        String fin = anio + "-" + mes + "-31";
        return registroDao.obtenerPorAnioYMes(userId, inicio, fin);
    }

    /**
     * Obtiene registros por fecha exacta.
     */
    public List<Registro> obtenerPorFecha(int userId, String fecha) {
        return registroDao.obtenerPorFecha(userId, fecha);
    }

    /**
     * Obtiene años con registros.
     */
    public List<String> obtenerAniosConRegistros(int userId) {
        return registroDao.obtenerAniosConRegistros(userId);
    }

    /**
     * Obtiene meses con registros en un año.
     */
    public List<String> obtenerMesesConRegistros(int userId, String anio) {
        String inicio = anio + "-01-01";
        String fin = anio + "-12-31";
        return registroDao.obtenerMesesConRegistros(userId, inicio, fin);
    }
}
