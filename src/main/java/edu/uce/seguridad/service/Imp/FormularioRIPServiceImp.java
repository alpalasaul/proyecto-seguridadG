package edu.uce.seguridad.service.Imp;

import edu.uce.seguridad.exception.EliminacionException;
import edu.uce.seguridad.exception.NoEncontradoExcepcion;
import edu.uce.seguridad.model.Estimacion;
import edu.uce.seguridad.model.EstimacionDano;
import edu.uce.seguridad.model.FormularioRIP;
import edu.uce.seguridad.model.Recurso;
import edu.uce.seguridad.repository.FormularioRIPRepository;
import edu.uce.seguridad.service.service.EstimacionDanoService;
import edu.uce.seguridad.service.service.FormularioRIPService;
import edu.uce.seguridad.service.service.RecursoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class FormularioRIPServiceImp implements FormularioRIPService {

    @Autowired
    private FormularioRIPRepository formularioRIPRepository;

    @Autowired
    private EstimacionDanoService estimacionDanoService;

    @Autowired
    private RecursoService recursoService;


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

        // puede tener no creado Recursos (se le crearán solo las categorías con "data": [empty])
        Recurso recurso1 = this.recursoService.buscarRecursoPorUsuario(pojo.getUser());

        HashMap<String, List<Estimacion>> estimaciones = new HashMap<>();


        // TODO: Reconozco que este código es una basura pero cumple su trabajo, se aceptan mejoras XD

        List<Estimacion> estimacionDanosList1 = new ArrayList<>();
        List<Estimacion> estimacionDanosList2 = new ArrayList<>();
        List<Estimacion> estimacionDanosList3 = new ArrayList<>();

        // obtener un array de las llaves para ubicarlo con index
        String categoria = "RecursosInternos";
        String categoria1 = "ServiciosEscenciales";
        String categoria2 = "SociosNegocios";

        estimaciones.put(categoria, llenarListaRecurso(categoria, recurso1, estimacionDano, estimacionDanosList1));
        estimaciones.put(categoria1, llenarListaRecurso(categoria1, recurso1, estimacionDano, estimacionDanosList2));
        estimaciones.put(categoria2, llenarListaRecurso(categoria2, recurso1, estimacionDano, estimacionDanosList3));

        estimacionDano.setRecursosNecesarios(estimaciones);
        this.estimacionDanoService.agregar(estimacionDano);

        return this.formularioRIPRepository.insert(pojo);
    }

    public List<Estimacion> llenarListaRecurso(String categoria,
                                               Recurso recurso,
                                               EstimacionDano estimacionDano,
                                               List<Estimacion> lista) {
        try {
            for (int i = 0; i < recurso.getRecursos().get(categoria).size(); i++) {
                lista.add(estimacionDano.definirEstimacion(
                        recurso.getRecursos().get(categoria).get(i).getNombre(),
                        0,
                        0,
                        0,
                        true));
            }
        } catch (NullPointerException e) {
            // manejar la excepcion
        }
        return lista;
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
