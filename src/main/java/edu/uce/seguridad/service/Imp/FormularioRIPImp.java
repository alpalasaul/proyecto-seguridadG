package edu.uce.seguridad.service.Imp;

import edu.uce.seguridad.exception.EliminacionException;
import edu.uce.seguridad.exception.NoEncontradoExcepcion;
import edu.uce.seguridad.model.Estimacion;
import edu.uce.seguridad.model.EstimacionDano;
import edu.uce.seguridad.model.FormularioRIP;
import edu.uce.seguridad.repository.FormularioRIPRepository;
import edu.uce.seguridad.service.service.EstimacionDanoService;
import edu.uce.seguridad.service.service.FormularioRIPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Service
public class FormularioRIPImp implements FormularioRIPService {

    @Autowired
    private FormularioRIPRepository formularioRIPRepository;

    @Autowired
    private EstimacionDanoService estimacionDanoService;


    @Override
    @Transactional(readOnly = true)
    public List<FormularioRIP> buscarTodos() throws NoEncontradoExcepcion {
        List<FormularioRIP> rips = this.formularioRIPRepository.findAll();
        if (rips.isEmpty()) {
            throw new NoEncontradoExcepcion("respuesta", "No se han encontrado registros");
        }
        return rips;
    }

    @Override
    @Transactional
    public FormularioRIP agregar(FormularioRIP pojo) throws DataAccessException {
        EstimacionDano estimacionDano = new EstimacionDano();
        estimacionDano.setUsuario(pojo.getUser());

        estimacionDano.setImpacto(pojo.getImpacto()); // Los seteo para este nuevo formulario
        estimacionDano.setRiesgo(pojo.getNombreRiesgo());
        estimacionDano.setProbabilidad(pojo.getProbabilidad());

        // Falta la definicion de las actividades prioritarias que es el 3.1, solo setear los tipos en la lista, el resto solo deberia ir en 0
        // Ej:
//        HashMap<String, List<Estimacion>> estimaciones = new HashMap<>();

//        estimaciones.put("Recursos Internos", Arrays.asList(
//                estimacionDano.definirEstimacion("Inmuebles", 0, 0, true),
//                estimacionDano.definirEstimacion("Equipos", 0, 0, true)
//        ));

//        Cada vez que se cree un riesgo tuyo crear un form de eestimacion de resigo
//        estimacionDano.setRecursosNecesarios(estimaciones);
//        this.estimacionDanoService.agregar(estimacionDano);

        return this.formularioRIPRepository.insert(pojo);
    }

    @Override
    @Transactional
    public FormularioRIP actualizar(FormularioRIP pojo) throws DataAccessException {
        this.buscaPorId(pojo.get_id());
        return this.formularioRIPRepository.save(pojo);
    }

    @Override
    @Transactional(readOnly = true)
    public FormularioRIP buscaPorId(String identificador) throws NoEncontradoExcepcion {
        FormularioRIP rip = this.formularioRIPRepository.findById(identificador).orElse(null);
        if (rip == null) {
            throw new NoEncontradoExcepcion("respuesta", "No se han encontrado registros de: ".concat(identificador));
        }
        return rip;
    }

    @Override
    @Transactional
    public void eliminarDocumento(String identificador) throws EliminacionException {
        FormularioRIP rip = this.buscaPorId(identificador);
        if (rip == null) {
            throw new NoEncontradoExcepcion("respuesta", "No se han encontrado registros de: ".concat(identificador));
        }
        this.formularioRIPRepository.delete(rip);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FormularioRIP> buscarPorUserFiltradoPorPrioridad(String user) throws NoEncontradoExcepcion {
        List<FormularioRIP> rips = this.formularioRIPRepository.findByUserOrderByPrioridadAsc(user);
        if (rips.isEmpty()) {
            throw new NoEncontradoExcepcion("respuesta", "No se han encontrado registros para: ".concat(user));
        }
        return rips;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FormularioRIP> buscarPorUser(String user) throws NoEncontradoExcepcion {
        List<FormularioRIP> rips = this.formularioRIPRepository.findByUser(user);
        if (rips.isEmpty()) {
            throw new NoEncontradoExcepcion("respuesta", "No se han encontrado registros para: ".concat(user));
        }
        return rips;
    }

    @Override
    @Transactional
    public void eliminarPorUsusario(String user) {
        this.formularioRIPRepository.deleteByUser(user);
    }

    @Override // No trans porque delego ese proceso a otro metodo
    public FormularioRIP getMayorPrioridad(String user) {
        FormularioRIP formularioRIP = this.buscarPorUserFiltradoPorPrioridad(user).get(0);
        return formularioRIP;
    }
}
