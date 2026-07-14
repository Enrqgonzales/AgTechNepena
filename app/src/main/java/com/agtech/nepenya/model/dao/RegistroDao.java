package com.agtech.nepenya.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.agtech.nepenya.model.entity.Registro;

import java.util.List;

/**
 * Data Access Object para la entidad Registro.
 * Define operaciones CRUD y consultas especificas.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
@Dao
public interface RegistroDao {

    /**
     * Inserta un nuevo registro.
     *
     * @param registro Registro a insertar
     * @return ID generado
     */
    @Insert
    long insertar(Registro registro);

    /**
     * Actualiza un registro existente.
     *
     * @param registro Registro a actualizar
     */
    @Update
    void actualizar(Registro registro);

    /**
     * Elimina un registro.
     *
     * @param registro Registro a eliminar
     */
    @Delete
    void eliminar(Registro registro);

    /**
     * Obtiene todos los registros ordenados por fecha descendente.
     *
     * @return Lista de registros
     */
    @Query("SELECT * FROM registros ORDER BY fecha DESC, id DESC")
    List<Registro> obtenerTodos();

    /**
     * Obtiene registros por parcela.
     *
     * @param parcelaId ID de la parcela
     * @return Lista de registros
     */
    @Query("SELECT * FROM registros WHERE parcela_id = :parcelaId ORDER BY fecha DESC")
    List<Registro> obtenerPorParcela(int parcelaId);

    /**
     * Obtiene registros por tipo (GASTO o INGRESO).
     *
     * @param tipo Tipo de registro
     * @return Lista de registros filtrados
     */
    @Query("SELECT * FROM registros WHERE tipo = :tipo ORDER BY fecha DESC")
    List<Registro> obtenerPorTipo(String tipo);

    /**
     * Obtiene registros por parcela y tipo.
     *
     * @param parcelaId ID de la parcela
     * @param tipo      Tipo de registro
     * @return Lista de registros filtrados
     */
    @Query("SELECT * FROM registros WHERE parcela_id = :parcelaId AND tipo = :tipo ORDER BY fecha DESC")
    List<Registro> obtenerPorParcelaYTipo(int parcelaId, String tipo);

    /**
     * Obtiene un registro por su ID.
     *
     * @param id Identificador del registro
     * @return Registro encontrado o null
     */
    @Query("SELECT * FROM registros WHERE id = :id LIMIT 1")
    Registro obtenerPorId(int id);

    /**
     * Obtiene registros pendientes de sincronizacion.
     *
     * @return Lista de registros con syncStatus = 'PENDING'
     */
    @Query("SELECT * FROM registros WHERE sync_status = 'PENDING'")
    List<Registro> obtenerPendientesSync();

    /**
     * Obtiene el conteo de registros pendientes.
     *
     * @return Cantidad de registros pendientes
     */
    @Query("SELECT COUNT(*) FROM registros WHERE sync_status = 'PENDING'")
    int contarPendientes();

    @Query("SELECT COUNT(*) FROM registros WHERE sync_status = 'PENDING'")
    androidx.lifecycle.LiveData<Integer> contarPendientesLiveData();

    /**
     * Actualiza el estado de sincronizacion.
     *
     * @param id         ID del registro
     * @param syncStatus Nuevo estado
     * @param remoteId   ID remoto asignado
     */
    @Query("UPDATE registros SET sync_status = :syncStatus, remote_id = :remoteId WHERE id = :id")
    void actualizarSyncStatus(int id, String syncStatus, Integer remoteId);

    /**
     * Obtiene registros por año.
     *
     * @param anio Anio en formato yyyy
     * @return Lista de registros del año
     */
    @Query("SELECT * FROM registros WHERE fecha BETWEEN :inicio AND :fin ORDER BY fecha DESC")
    List<Registro> obtenerPorAnio(String inicio, String fin);

    /**
     * Obtiene suma de gastos por categoria en un año.
     *
     * @param anio      Anio en formato yyyy
     * @param categoria Categoria a filtrar
     * @return Suma de montos
     */
    @Query("SELECT SUM(monto) FROM registros WHERE fecha BETWEEN :inicio AND :fin AND tipo = 'GASTO' AND categoria = :categoria")
    Double obtenerGastosPorCategoriaYAnio(String inicio, String fin, String categoria);

    /**
     * Obtiene suma de ingresos por categoria en un año.
     *
     * @param anio      Anio en formato yyyy
     * @param categoria Categoria a filtrar
     * @return Suma de montos
     */
    @Query("SELECT SUM(monto) FROM registros WHERE fecha BETWEEN :inicio AND :fin AND tipo = 'INGRESO' AND categoria = :categoria")
    Double obtenerIngresosPorCategoriaYAnio(String inicio, String fin, String categoria);

    /**
     * Obtiene total de gastos en un año.
     *
     * @param anio Anio en formato yyyy
     * @return Total de gastos
     */
    @Query("SELECT SUM(monto) FROM registros WHERE fecha BETWEEN :inicio AND :fin AND tipo = 'GASTO'")
    Double obtenerTotalGastosPorAnio(String inicio, String fin);

    /**
     * Obtiene total de ingresos en un año.
     *
     * @param anio Anio en formato yyyy
     * @return Total de ingresos
     */
    @Query("SELECT SUM(monto) FROM registros WHERE fecha BETWEEN :inicio AND :fin AND tipo = 'INGRESO'")
    Double obtenerTotalIngresosPorAnio(String inicio, String fin);

    /**
     * Obtiene categorias de gastos distintas.
     *
     * @return Lista de categorias
     */
    @Query("SELECT DISTINCT categoria FROM registros WHERE tipo = 'GASTO' ORDER BY categoria")
    List<String> obtenerCategoriasGasto();

    /**
     * Obtiene categorias de ingresos distintas.
     *
     * @return Lista de categorias
     */
    @Query("SELECT DISTINCT categoria FROM registros WHERE tipo = 'INGRESO' ORDER BY categoria")
    List<String> obtenerCategoriasIngreso();

    /**
     * Obtiene registros por año y mes.
     *
     * @param anio Anio en formato yyyy
     * @param mes  Mes en formato MM
     * @return Lista de registros del mes
     */
    @Query("SELECT * FROM registros WHERE fecha BETWEEN :inicio AND :fin ORDER BY fecha DESC")
    List<Registro> obtenerPorAnioYMes(String inicio, String fin);

    /**
     * Obtiene registros por fecha exacta.
     *
     * @param fecha Fecha en formato yyyy-MM-dd
     * @return Lista de registros de esa fecha
     */
    @Query("SELECT * FROM registros WHERE fecha = :fecha ORDER BY id DESC")
    List<Registro> obtenerPorFecha(String fecha);

    /**
     * Obtiene registros por rango de fechas.
     *
     * @param fechaInicio Fecha inicio en formato yyyy-MM-dd
     * @param fechaFin    Fecha fin en formato yyyy-MM-dd
     * @return Lista de registros en el rango
     */
    @Query("SELECT * FROM registros WHERE fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY fecha DESC")
    List<Registro> obtenerPorRangoFechas(String fechaInicio, String fechaFin);

    /**
     * Obtiene años distintos con registros.
     *
     * @return Lista de años
     */
    @Query("SELECT DISTINCT strftime('%Y', fecha) FROM registros ORDER BY strftime('%Y', fecha) DESC")
    List<String> obtenerAniosConRegistros();

    /**
     * Obtiene meses distintos con registros en un año.
     *
     * @param anio Año en formato yyyy
     * @return Lista de meses (01-12)
     */
    @Query("SELECT DISTINCT strftime('%m', fecha) FROM registros WHERE fecha BETWEEN :inicio AND :fin ORDER BY strftime('%m', fecha)")
    List<String> obtenerMesesConRegistros(String inicio, String fin);
}
